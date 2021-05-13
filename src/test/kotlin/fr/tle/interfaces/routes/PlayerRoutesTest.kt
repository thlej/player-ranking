package fr.tle.interfaces.routes

import fr.tle.domain.Player
import fr.tle.domain.RankedPlayer
import fr.tle.interfaces.rest.PlayerUpdateRequest
import fr.tle.plugins.configureRouting
import fr.tle.plugins.configureSerialization
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test

class PlayerRoutesTest : WithAssertions {

    @Test
    fun `should add a new player`() {
        withTestApplication({ // TODO BaseApplicationTest extract
            configureRouting()
            configureSerialization()
        }) {
            val playerToAdd = Player("bob", 0)

            handleRequest(HttpMethod.Post, "/players") {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody(Json.encodeToString(playerToAdd))
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Created)
                assertThat(response.content).isEqualTo(Json.encodeToString(RankedPlayer(playerToAdd, 4)))
            }
        }
    }

    @Test
    fun `should update given player points`() {
        withTestApplication({
            configureRouting()
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Put, "/players/foo") {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody(Json.encodeToString(PlayerUpdateRequest(20)))
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isEqualTo(Json.encodeToString(RankedPlayer(Player("foo", 20), 1)))
            }
        }
    }

    @Test
    fun `should not update unknown player points`() {
        withTestApplication({
            configureRouting()
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Put, "/players/foobarbaz") {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                setBody(Json.encodeToString(PlayerUpdateRequest(20)))
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
                assertThat(response.content).isEqualTo("No player found for pseudo 'foobarbaz'")
            }
        }
    }

    @Test
    fun `should not update when body is missing`() {
        withTestApplication({
            configureRouting()
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Put, "/players/foobarbaz") {
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
            configureRouting()
            configureSerialization()
        }) {
            val expectedPlayers = listOf(
                RankedPlayer(Player("foo", 10), 1),
                RankedPlayer(Player("bar", 5), 2),
                RankedPlayer(Player("baz", 1), 3)
            )

            handleRequest(HttpMethod.Get, "/players").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isEqualTo(Json.encodeToString(expectedPlayers))
            }
        }
    }

    @Test
    fun `should get a player`() {
        withTestApplication({
            configureRouting()
            configureSerialization()
        }) {
            val expectedPlayer = RankedPlayer(Player("foo", 10), 1)

            handleRequest(HttpMethod.Get, "/players/foo").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isEqualTo(Json.encodeToString(expectedPlayer))
            }
        }
    }

    @Test
    fun `should not find an unknown player`() {
        withTestApplication({
            configureRouting()
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Get, "/players/foobarbaz").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
                assertThat(response.content).isEqualTo("No player found for pseudo 'foobarbaz'")
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
            configureRouting()
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Delete, "/players").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
                assertThat(response.content).isEqualTo("All players were successfully deleted")
            }
        }
    }
}