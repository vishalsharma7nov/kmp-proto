package io.github.vishalsharma7nov.kmpproto.mockserver

import io.github.vishalsharma7nov.kmpproto.generated.AddUserRequest
import io.github.vishalsharma7nov.kmpproto.generated.GetUserRequest
import io.github.vishalsharma7nov.kmpproto.generated.User
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receive
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

/**
 * Tiny JVM mock server for local demos and tests.
 * Handles `demo.UserService/GetUser` with HTTP+protobuf.
 */
public fun startMockUserServer(
    host: String = "127.0.0.1",
    port: Int = 18080,
    demoUser: Map<String, Any?> = mapOf(
        "id" to "1",
        "name" to "Ada Lovelace",
        "email" to "ada@example.com",
        "age" to 36,
        "phone" to "",
        "created_at" to 0L,
        "updated_at" to 0L,
    ),
): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> {
    return embeddedServer(CIO, host = host, port = port) {
        routing {
            post("/demo.UserService/GetUser") {
                val bytes = call.receive<ByteArray>()
                val request = GetUserRequest.decode(bytes)
                val id = request["id"] as? String ?: "1"
                val user = demoUser.toMutableMap().also { it["id"] = id }
                call.respondBytes(
                    User.encode(user),
                    ContentType.parse("application/x-protobuf"),
                    HttpStatusCode.OK,
                )
            }
            post("/demo.UserService/AddUser") {
                val bytes = call.receive<ByteArray>()
                val decoded = try {
                    AddUserRequest.decode(bytes)
                } catch (_: Throwable) {
                    emptyMap()
                }
                val user = demoUser.toMutableMap().apply {
                    put("id", "new")
                    put("name", decoded["name"] ?: get("name"))
                    put("email", decoded["email"] ?: get("email"))
                    put("age", decoded["age"] ?: get("age"))
                    put("phone", decoded["phone"] ?: get("phone"))
                }
                call.respondBytes(
                    User.encode(user),
                    ContentType.parse("application/x-protobuf"),
                    HttpStatusCode.OK,
                )
            }
        }
    }.start(wait = false)
}
