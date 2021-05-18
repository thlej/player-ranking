package fr.tle.infrastructure.configuration


import fr.tle.interfaces.rest.registerPlayerRoutes
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        registerPlayerRoutes()
    }
}
