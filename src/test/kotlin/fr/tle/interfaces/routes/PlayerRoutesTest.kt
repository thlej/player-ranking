package fr.tle.interfaces.routes

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import fr.tle.domain.Player
import fr.tle.domain.PlayerRepository
import fr.tle.domain.PlayerService
import fr.tle.extensions.Database
import fr.tle.infrastructure.persistence.mongo.MongoPlayerRepository
import fr.tle.infrastructure.persistence.mongo.PlayerDocument
import fr.tle.infrastructure.persistence.mongo.toPlayerDocument
import fr.tle.interfaces.rest.dto.PlayerCreateRequest
import fr.tle.interfaces.rest.dto.PlayerResponse
import fr.tle.interfaces.rest.dto.PlayerUpdateRequest
import fr.tle.interfaces.rest.dto.RankedPlayerResponse
import fr.tle.interfaces.rest.mappers.toPlayer
import fr.tle.interfaces.rest.mappers.toPlayerResponse
import fr.tle.module
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.litote.kmongo.getCollection

@Database
class PlayerRoutesTest(mongoDatabase: MongoDatabase) : WithAssertions {

    private val baseUrl = "/v1/players"

    private val playersCollection = mongoDatabase.getCollection<PlayerDocument>("players") // FIXME do better (inject?)

    private val testModule = module {
        single<MongoDatabase> { mongoDatabase }
        single<MongoCollection<PlayerDocument>> {
            get<MongoDatabase>().getCollection<PlayerDocument>("players")
        }
        single<PlayerRepository> { MongoPlayerRepository(get()) }
        single { PlayerService(get()) }
    }

    @Test
    fun `should add a new player`() {
        withTestApplication({ // TODO BaseApplicationTest extract
            module(testing = true, listOf(testModule))
        }) {
            val playerCreateRequest = PlayerCreateRequest("bob", 0)

            handleRequest(HttpMethod.Post, baseUrl) {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody(Json.encodeToString(playerCreateRequest))
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Created)
                val expected =
                    Json.encodeToString(RankedPlayerResponse(playerCreateRequest.toPlayer().toPlayerResponse(), 1))
                assertThat(response.content).isEqualTo(expected)
            }
        }
    }

    @Test
    fun `should not add player if pseudo already exists`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            insertTestPlayers()
            val playerCreateRequest = PlayerCreateRequest("bill", 0)

            handleRequest(HttpMethod.Post, baseUrl) {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody(Json.encodeToString(playerCreateRequest))
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Conflict)
                assertThat(response.content).isEqualTo("Player already exists")
            }
        }
    }

    @Test
    fun `should update given player points`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            insertTestPlayers()
            handleRequest(HttpMethod.Put, "$baseUrl/bill") {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody(Json.encodeToString(PlayerUpdateRequest(20)))
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isEqualTo(Json.encodeToString(RankedPlayerResponse(PlayerResponse("bill", 20), 1)))
            }
        }
    }

    @Test
    fun `should not update unknown player points`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            insertTestPlayers()
            handleRequest(HttpMethod.Put, "$baseUrl/foo") {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody(Json.encodeToString(PlayerUpdateRequest(20)))
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
                assertThat(response.content).isEqualTo("No player found for pseudo 'foo'")
            }
        }
    }

    @Test
    fun `should not update when body is missing`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            insertTestPlayers()
            handleRequest(HttpMethod.Put, "$baseUrl/bill") {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).isEqualTo("Malformed or missing body")
            }
        }
    }

    @Test
    fun `should list all players`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            insertTestPlayers()
            val expectedPlayers = listOf(
                RankedPlayerResponse(PlayerResponse("john", 10), 1),
                RankedPlayerResponse(PlayerResponse("bob", 5), 2),
                RankedPlayerResponse(PlayerResponse("bill", 1), 3)
            )

            handleRequest(HttpMethod.Get, baseUrl).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isEqualTo(Json.encodeToString(expectedPlayers))
            }
        }
    }

    @Test
    fun `should get a player`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            insertTestPlayers()
            val expected = RankedPlayerResponse(PlayerResponse("john", 10), 1)

            handleRequest(HttpMethod.Get, "$baseUrl/john").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isEqualTo(Json.encodeToString(expected))
            }
        }
    }

    @Test
    fun `should not find an unknown player`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            insertTestPlayers()
            handleRequest(HttpMethod.Get, "$baseUrl/foo").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
                assertThat(response.content).isEqualTo("No player found for pseudo 'foo'")
            }
        }
    }

    /*@Test
    fun `should respond BadRequest when pseudo is missing`() {
        withTestApplication({
            configureRouting()
            configureSerialization()
        }) {
            val expectedPlayer = Player("foobarbaz", 10, 1)

            handleRequest(HttpMethod.Get, "/players/null").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).isEqualTo("Missing 'pseudo' parameter")
            }
        }
    }*/

    @Test
    fun `should delete all players`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            insertTestPlayers()
            handleRequest(HttpMethod.Delete, baseUrl).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
                assertThat(response.content).isEqualTo("All players were successfully deleted")
            }
        }
    }

    private fun insertTestPlayers() {
        playersCollection.insertMany(
            listOf(
                Player("bill", 1).toPlayerDocument(),
                Player("bob", 5).toPlayerDocument(),
                Player("john", 10).toPlayerDocument(),
            )
        )
    }
}