package io.github.vishalsharma7nov.kmpproto

import io.github.vishalsharma7nov.kmpproto.client.call
import io.github.vishalsharma7nov.kmpproto.generated.createApi
import io.github.vishalsharma7nov.kmpproto.mockserver.startMockUserServer
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MockServerIntegrationTest {
    private lateinit var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>
    private var port: Int = 0

    @BeforeTest
    fun setUp() {
        port = 18_000 + (Math.random() * 1000).toInt()
        server = startMockUserServer(port = port)
        Thread.sleep(400)
    }

    @AfterTest
    fun tearDown() {
        server.stop(100, 500)
    }

    @Test
    fun getUserViaCreateClient() = runBlocking {
        val client = createClient(ProtoClientConfig(baseUrl = "http://127.0.0.1:$port"))
        val user = client.call("userService", "getUser", mapOf("id" to "1"))
        assertEquals("1", user["id"])
        assertEquals("Ada Lovelace", user["name"])
        assertEquals(36, user["age"])
    }

    @Test
    fun getUserViaCreateApi() = runBlocking {
        val api = createApi(ProtoClientConfig(baseUrl = "http://127.0.0.1:$port"))
        val user = api.userService.getUser(mapOf("id" to "9"))
        assertEquals("9", user["id"])
    }
}
