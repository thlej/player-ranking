package fr.tle.extensions

import com.mongodb.client.MongoDatabase
import org.junit.jupiter.api.extension.*
import org.litote.kmongo.KMongo
import org.testcontainers.containers.GenericContainer

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(MongoExtension::class, DatabaseExtension::class)
annotation class Database()

class DatabaseExtension : AfterEachCallback, ParameterResolver {

    private val mongoDatabase : MongoDatabase

    init {
        val mongoClientURI = "mongodb://$DOCKER_DAEMON_HOST:${MongoExtension.getMappedPort(ORIGIN_MONGO_PORT)}"
        val client = KMongo.createClient(mongoClientURI)
        mongoDatabase = client.getDatabase("test")
    }

    override fun afterEach(extensionContext: ExtensionContext) {
        mongoDatabase.listCollectionNames().forEach { name -> mongoDatabase.getCollection(name).drop() }
    }

    @Throws(ParameterResolutionException::class)
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.type == MongoDatabase::class.java
    }

    @Throws(ParameterResolutionException::class)
    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? {
        return when {
            MongoDatabase::class.java == parameterContext.parameter.type -> mongoDatabase
            else -> throw IllegalArgumentException("Param of type ${parameterContext.parameter.type} is not supported by this extension")
        }
    }

}

class MongoContainer : GenericContainer<MongoContainer>("mongo")

val DOCKER_DAEMON_HOST = System.getenv("DOCKER_DAEMON_HOST") ?: "127.0.0.1"
const val ORIGIN_MONGO_PORT = 27017

class MongoExtension internal constructor() : BeforeAllCallback {

    init {
        if(genericContainer == null){
            genericContainer = MongoContainer().withExposedPorts(ORIGIN_MONGO_PORT)
            println("Starting MongoDB")
            genericContainer!!.start()
        }
    }

    override fun beforeAll(context: ExtensionContext?) = println("MongoDB is running")

    companion object {
        private var genericContainer: GenericContainer<*>? = null
        fun getMappedPort(originalPort: Int): Int {
            val mappedPort = genericContainer!!.getMappedPort(originalPort)!!
            println("Mapped port : $originalPort=$mappedPort")
            return mappedPort
        }
    }
}