package io.github.vishalsharma7nov.kmpproto.examples

import io.github.vishalsharma7nov.kmpproto.ProtoClientConfig
import io.github.vishalsharma7nov.kmpproto.errors.isProtoClientError
import io.github.vishalsharma7nov.kmpproto.generated.createApi
import io.github.vishalsharma7nov.kmpproto.mockserver.startMockUserServer
import kotlinx.coroutines.runBlocking

/**
 * JVM / desktop sample — analog of the RN web example.
 * Starts a mock protobuf server, calls getUser, prints the result.
 */
fun main() = runBlocking {
    val port = 18081
    val server = startMockUserServer(port = port)
    try {
        val config = ProtoClientConfig(baseUrl = "http://127.0.0.1:$port")
        val api = createApi(config)
        val user = api.userService.getUser(mapOf("id" to "1"))
        println("getUser => $user")
    } catch (e: Throwable) {
        if (isProtoClientError(e)) {
            println("ProtoClientError: $e")
        } else {
            e.printStackTrace()
        }
    } finally {
        server.stop(200, 500)
    }
}
