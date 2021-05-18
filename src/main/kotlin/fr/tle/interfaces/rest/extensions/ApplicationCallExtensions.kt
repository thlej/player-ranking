package fr.tle.interfaces.rest.extensions

import io.ktor.application.*
import io.ktor.request.*
import kotlinx.serialization.SerializationException

suspend inline fun <reified T : Any> ApplicationCall.receiveBodyOrNull(): T? = try {
    receive()
} catch (e: SerializationException) {
    null
}