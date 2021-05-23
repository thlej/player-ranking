package fr.tle.interfaces.rest

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
                    Json.encodeToString(RankedPlayerResponse("bob", 0, 1))
                assertThat(response.content).isEqualTo(expected)
            }
        }
    }

    @Test
    fun `should not add player when body is empty`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            handleRequest(HttpMethod.Post, baseUrl) {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody("{}")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).isEqualTo("Malformed or missing body")
            }
        }
    }

    @Test
    fun `should not add player with null pseudo`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            handleRequest(HttpMethod.Post, baseUrl) {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody("""{"pseudo": null, "points": 1}""")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).isEqualTo("Malformed or missing body")
            }
        }
    }

    @Test
    fun `should not add player with blank pseudo`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            handleRequest(HttpMethod.Post, baseUrl) {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody("""{"pseudo": "  ", "points": 1}""")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).isEqualTo("PlayerCreateRequest.pseudo should not be empty or blank")
            }
        }
    }

    @Test
    fun `should not add player with null points`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            handleRequest(HttpMethod.Post, baseUrl) {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody("""{"pseudo": "bill", "points": null}""")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).isEqualTo("Malformed or missing body")
            }
        }
    }

    @Test
    fun `should not add player with negative points`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            handleRequest(HttpMethod.Post, baseUrl) {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody("""{"pseudo": "bill", "points": -10}""")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).isEqualTo("PlayerCreateRequest.points must be positive")
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
                assertThat(response.content).isEqualTo(
                    Json.encodeToString(
                        RankedPlayerResponse("bill", 20, 1)
                    )
                )
            }
        }
    }

    @Test
    fun `should not update player when body is empty`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            insertTestPlayers()
            handleRequest(HttpMethod.Put, "$baseUrl/bill") {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody("{}")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).isEqualTo("Malformed or missing body")
            }
        }
    }

    @Test
    fun `should not update player with null points`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            insertTestPlayers()
            handleRequest(HttpMethod.Put, "$baseUrl/bill") {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody("""{"points": null}""")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).isEqualTo("Malformed or missing body")
            }
        }
    }

    @Test
    fun `should not update player with negative points`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            insertTestPlayers()
            handleRequest(HttpMethod.Put, "$baseUrl/bill") {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody("""{"points": -10}""")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).isEqualTo("PlayerUpdateRequest.points must be positive")
            }
        }
    }

    @Test
    fun `should not update unknown player`() {
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
    fun `should list all players sorted by rank`() {
        withTestApplication({
            module(testing = true, listOf(testModule))
        }) {
            insertTestPlayers()
            val expectedPlayers = listOf(
                RankedPlayerResponse("john", 10, 1),
                RankedPlayerResponse("bob", 5, 2),
                RankedPlayerResponse("bill", 1, 3)
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
            val expected = RankedPlayerResponse("john", 10, 1)

            handleRequest(HttpMethod.Get, "$baseUrl/john").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isEqualTo(Json.encodeToString(expected))
            }
        }
    }

    @Test
    fun `should not get an unknown player`() {
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