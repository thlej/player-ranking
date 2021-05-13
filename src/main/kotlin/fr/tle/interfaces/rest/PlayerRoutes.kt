package fr.tle.interfaces.rest

import fr.tle.domain.Player
import fr.tle.domain.PlayerService
import fr.tle.infrastructure.persistence.memory.InMemoryPlayerRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

fun Route.playerRouting(playerService: PlayerService){
    route("/players"){
        get{
            call.respond(playerService.allSortedByRank())
        }
        get("{pseudo}"){
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
            call.respondText("All players were successfully deleted", status = HttpStatusCode.NoContent) // FIXME or HttpStatusCode.Accepted?
        }
    }
}

fun Route.listAllPlayers(playerService: PlayerService){
    get("/players"){
        call.respond(playerService.allSortedByRank())
    }
}

fun Route.getPlayer(playerService: PlayerService){
    get("/players/{pseudo}"){
        val pseudo = call.parameters["pseudo"]
            ?: return@get call.respondText("Missing 'pseudo' parameter", status = HttpStatusCode.BadRequest)
        val player = playerService.by(pseudo)
            ?: return@get call.respondText("No player found for pseudo '$pseudo'", status = HttpStatusCode.NotFound)
        call.respond(player)
    }
}

fun Route.addPlayer(playerService: PlayerService){
    post("/players") {
        val player = call.receive<Player>()
        call.respond(HttpStatusCode.Created, playerService.add(player))
    }
}

fun Route.updatePlayer(playerService: PlayerService){
    put("/players/{pseudo}"){
        val pseudo = call.parameters["pseudo"]
            ?: return@put call.respondText("Missing 'pseudo' parameter", status = HttpStatusCode.BadRequest)

        val points = call.receiveBodyOrNull<PlayerUpdateRequest>()?.points
            ?: return@put call.respondText("Malformed or missing body", status = HttpStatusCode.BadRequest)

        val player = playerService.update(Player(pseudo, points))
            ?: return@put call.respondText("No player found for pseudo '$pseudo'", status = HttpStatusCode.NotFound)
        call.respond(player)
    }
}

fun Route.deleteAllPlayers(playerService: PlayerService){
    delete("/players") {
        playerService.deleteAll()
        call.respondText("All players were successfully deleted", status = HttpStatusCode.NoContent) // FIXME or HttpStatusCode.Accepted?
    }
}

fun Application.registerPlayerRoutes(){
    val playerService = PlayerService(InMemoryPlayerRepository())
    routing {
//        playerRouting(playerService)
        addPlayer(playerService)
        updatePlayer(playerService)
        listAllPlayers(playerService)
        getPlayer(playerService)
        deleteAllPlayers(playerService)
    }
}

// FIXME move it elsewhere
suspend inline fun <reified T : Any> ApplicationCall.receiveBodyOrNull(): T? = try {
    receive()
} catch (e: SerializationException) {
    null
}

@Serializable
data class PlayerUpdateRequest(val points: Int)