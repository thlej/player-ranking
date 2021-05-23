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
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

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
        val pseudo = call.parameters["pseudo"]!!
        val rankedPlayerResponse = playerService.by(pseudo)?.toRankedPlayerResponse()
            ?: return@get call.respondText("No player found for pseudo '$pseudo'", status = HttpStatusCode.NotFound)
        call.respond(rankedPlayerResponse)
    }
}

fun Route.addPlayer() {
    val playerService by inject<PlayerService>()

    post {
        val request = call.receiveBodyOrNull<PlayerCreateRequest>()
            ?: return@post call.respondText("Malformed or missing body", status = HttpStatusCode.BadRequest)
        try {
            val rankedPlayerResponse = playerService.add(request.toPlayer()).toRankedPlayerResponse()
            call.respond(HttpStatusCode.Created, rankedPlayerResponse)
        } catch (e: PlayerAlreadyExistsException) {
            call.respondText(e.localizedMessage, status = HttpStatusCode.Conflict)
        } catch (e: IllegalStateException){
            call.respondText("Player creation failed, please retry later")
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
        )
    }
}

fun Application.registerPlayerRoutes() {
    routing {
        route("api/v1/players") {
            addPlayer()
            updatePlayer()
            listAllPlayers()
            getPlayer()
            deleteAllPlayers()
        }
    }
}