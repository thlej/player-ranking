package fr.tle

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import fr.tle.domain.PlayerRepository
import fr.tle.domain.PlayerService
import fr.tle.infrastructure.configuration.configureRouting
import fr.tle.infrastructure.configuration.configureSerialization
import fr.tle.infrastructure.persistence.mongo.MongoPlayerRepository
import fr.tle.infrastructure.persistence.mongo.PlayerDocument
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import org.slf4j.event.Level
import java.lang.IllegalArgumentException

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false, koinModules: List<Module> = listOf(appModule)) {
    configureRouting()
    configureSerialization()

    install(Koin) {
//        slf4jLogger() FIXME crash startup...
        modules(koinModules)
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(StatusPages) {
        exception<IllegalArgumentException> { e ->
            call.respondText(
                text = e.localizedMessage,
                status = HttpStatusCode.BadRequest
            )
        }
    }
}

val appModule = module {
    single<MongoDatabase> { KMongo.createClient().getDatabase("player-ranking") }
    single<MongoCollection<PlayerDocument>> { // FIXME do better? (~config class)
        get<MongoDatabase>().getCollection<PlayerDocument>("players")
    }
    single<PlayerRepository> { MongoPlayerRepository(get()) }
    single { PlayerService(get()) }
}