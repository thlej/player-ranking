package fr.tle.interfaces.rest

import fr.tle.domain.Player
import fr.tle.domain.PlayerService
import fr.tle.infrastructure.exception.PlayerAlreadyExistsException
import fr.tle.interfaces.rest.dto.PlayerCreateRequest
import fr.tle.interfaces.rest.dto.PlayerUpdateRequest
import fr.tle.interfaces.rest.extensions.receiveBodyOrNull
import fr.tle.interfaces.rest.mappers.toPlayer
import fr.tle.interfaces.rest.mappers.toRankedPlayerResponse
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Route.playerRouting() {
    val playerService by inject<PlayerService>()

    route("/players") {
        get {
            call.respond(playerService.allSortedByRank())
        }
        get("{pseudo}") {
            val pseudo = call.parameters["pseudo"] //FIXME can it even happen?
                ?: return@get call.respondText("Missing 'pseudo' parameter", status = HttpStatusCode.BadRequest)
            val player = playerService.by(pseudo)
                ?: return@get call.respondText("No player found for pseudo '$pseudo'", status = HttpStatusCode.NotFound)
            call.respond(player)
        }
        post {
            val player = call.receive<Player>()
            call.respond(HttpStatusCode.Created, playerService.add(player))
        }
        delete {
            playerService.deleteAll()
            call.respondText(
                "All players were successfully deleted",
                status = HttpStatusCode.NoContent
            ) // FIXME or HttpStatusCode.Accepted?
        }
    }
}

fun Route.listAllPlayers() {
    val playerService by inject<PlayerService>()

    get {
        val allRankedPlayerResponse = playerService.allSortedByRank().map { it.toRankedPlayerResponse() }
        call.respond(allRankedPlayerResponse)
    }
}

fun Route.getPlayer() {
    val playerService by inject<PlayerService>()

    get("/{pseudo}") {
        val pseudo = call.parameters["pseudo"]
            ?: return@get call.respondText("Missing 'pseudo' parameter", status = HttpStatusCode.BadRequest)
        val rankedPlayerResponse = playerService.by(pseudo)?.toRankedPlayerResponse()
            ?: return@get call.respondText("No player found for pseudo '$pseudo'", status = HttpStatusCode.NotFound)
        call.respond(rankedPlayerResponse)
    }
}

fun Route.addPlayer() {
    val playerService by inject<PlayerService>()

    post {
        val request = call.receive<PlayerCreateRequest>()
        try {
            val rankedPlayerResponse = playerService.add(request.toPlayer()).toRankedPlayerResponse()
            call.respond(HttpStatusCode.Created, rankedPlayerResponse)
        } catch (e: PlayerAlreadyExistsException) {
            call.respondText(e.localizedMessage, status = HttpStatusCode.Conflict)
        }
    }
}

fun Route.updatePlayer() {
    val playerService by inject<PlayerService>()

    put("/{pseudo}") {
        val pseudo = call.parameters["pseudo"]
            ?: return@put call.respondText("Missing 'pseudo' parameter", status = HttpStatusCode.BadRequest)

        val points = call.receiveBodyOrNull<PlayerUpdateRequest>()?.points
            ?: return@put call.respondText("Malformed or missing body", status = HttpStatusCode.BadRequest)

        val rankedPlayerResponse = playerService.update(Player(pseudo, points))?.toRankedPlayerResponse()
            ?: return@put call.respondText("No player found for pseudo '$pseudo'", status = HttpStatusCode.NotFound)
        call.respond(rankedPlayerResponse)
    }
}

fun Route.deleteAllPlayers() {
    val playerService by inject<PlayerService>()

    delete {
        playerService.deleteAll()
        call.respondText(
            "All players were successfully deleted",
            status = HttpStatusCode.NoContent
        ) // FIXME or HttpStatusCode.Accepted?
    }
}

fun Application.registerPlayerRoutes() {
    routing {
        route("/v1/players") {
            addPlayer()
            updatePlayer()
            listAllPlayers()
            getPlayer()
            deleteAllPlayers()
        }
    }
}