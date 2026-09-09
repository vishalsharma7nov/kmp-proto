package io.github.vishalsharma7nov.kmpproto.example

import io.github.vishalsharma7nov.kmpproto.generated.GetUserRequest
import io.github.vishalsharma7nov.kmpproto.generated.User
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

/**
 * In-process mock that speaks the same protobuf wire format as [vendor/protos/user.proto].
 * Decodes [GetUserRequest] and encodes [User] via the generated codecs — no real network.
 */
fun createDemoMockHttpClient(
    demoUser: Map<String, Any?> = mapOf(
        "id" to "1",
        "name" to "Ada Lovelace",
        "email" to "ada@example.com",
        "age" to 36,
        "phone" to "",
        "created_at" to 0L,
        "updated_at" to 0L,
    ),
): HttpClient = HttpClient(MockEngine) {
    engine {
        addHandler { request ->
            val path = request.url.encodedPath
            if (!path.endsWith("/demo.UserService/GetUser") && !path.endsWith("demo.UserService/GetUser")) {
                return@addHandler respondError(HttpStatusCode.NotFound)
            }

            val requestBytes = when (val body = request.body) {
                is OutgoingContent.ByteArrayContent -> body.bytes()
                is OutgoingContent.ReadChannelContent -> {
                    val channel: ByteReadChannel = body.readFrom()
                    channel.readRemaining().readByteArray()
                }
                else -> ByteArray(0)
            }

            val decoded = GetUserRequest.decode(requestBytes)
            val id = (decoded["id"] as? String)?.takeIf { it.isNotBlank() } ?: "1"
            val user = demoUser.toMutableMap().also { it["id"] = id }

            respond(
                content = User.encode(user),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/x-protobuf"),
            )
        }
    }
}
