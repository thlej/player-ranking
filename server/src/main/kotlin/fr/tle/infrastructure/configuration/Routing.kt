package fr.tle.infrastructure.configuration


import fr.tle.interfaces.rest.registerPlayerRoutes
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureRouting() {

    routing {
        static("/") {
            resources("static")
            defaultResource("index.html", "static")
        }
        registerPlayerRoutes()
    }

}
