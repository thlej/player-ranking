package fr.tle

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import fr.tle.domain.Player
import fr.tle.domain.PlayerRepository
import fr.tle.domain.PlayerService
import fr.tle.infrastructure.configuration.configureRouting
import fr.tle.infrastructure.configuration.configureSerialization
import fr.tle.infrastructure.persistence.mongo.MongoPlayerRepository
import io.ktor.application.*
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

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
}

val appModule = module {
    single<MongoDatabase> { KMongo.createClient().getDatabase("player-ranking") }
    single<MongoCollection<Player>> { // FIXME do better? (~config class)
        get<MongoDatabase>().getCollection<Player>("players")
    }
    single<PlayerRepository> { MongoPlayerRepository(get()) }
    single { PlayerService(get()) }
}