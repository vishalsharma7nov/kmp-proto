package io.github.vishalsharma7nov.kmpproto

import io.github.vishalsharma7nov.kmpproto.codecs.ProtoWire
import io.github.vishalsharma7nov.kmpproto.errors.ProtoClientError
import io.github.vishalsharma7nov.kmpproto.errors.ProtoErrorCode
import io.github.vishalsharma7nov.kmpproto.errors.getErrorCode
import io.github.vishalsharma7nov.kmpproto.errors.isProtoClientError
import io.github.vishalsharma7nov.kmpproto.errors.wrapUnknownError
import io.github.vishalsharma7nov.kmpproto.generated.GetUserRequest
import io.github.vishalsharma7nov.kmpproto.generated.User
import io.github.vishalsharma7nov.kmpproto.generated.defaultCodecs
import io.github.vishalsharma7nov.kmpproto.generated.methodMap
import io.github.vishalsharma7nov.kmpproto.path.PathTemplatePreset
import io.github.vishalsharma7nov.kmpproto.path.createPathForMethod
import io.github.vishalsharma7nov.kmpproto.transport.resolveConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import okio.Buffer

class ProtoClientErrorTest {
    @Test
    fun wrapsUnknownErrors() {
        val wrapped = wrapUnknownError(IllegalStateException("boom"), "demo.UserService/GetUser")
        assertEquals(ProtoErrorCode.INTERNAL, wrapped.code)
        assertTrue(isProtoClientError(wrapped))
        assertEquals(ProtoErrorCode.INTERNAL, getErrorCode(wrapped))
    }

    @Test
    fun preservesTypedErrors() {
        val original = ProtoClientError(ProtoErrorCode.TIMEOUT, "timed out", retryable = true)
        assertEquals(original, wrapUnknownError(original))
    }
}

class ConfigValidationTest {
    @Test
    fun rejectsEmptyBaseUrl() {
        val error = assertFailsWith<ProtoClientError> {
            resolveConfig(ProtoClientConfig(baseUrl = "  "))
        }
        assertEquals(ProtoErrorCode.INVALID_CONFIG, error.code)
    }

    @Test
    fun rejectsRelativeBaseUrl() {
        val error = assertFailsWith<ProtoClientError> {
            resolveConfig(ProtoClientConfig(baseUrl = "api.example.com"))
        }
        assertEquals(ProtoErrorCode.INVALID_CONFIG, error.code)
    }

    @Test
    fun acceptsAbsoluteUrl() {
        val resolved = resolveConfig(ProtoClientConfig(baseUrl = "https://api.example.com/"))
        assertEquals("https://api.example.com", resolved.baseUrl)
        resolved.httpClient.close()
    }

    @Test
    fun protoSourceDefaultsToNull() {
        val config = ProtoClientConfig(baseUrl = "https://api.example.com")
        assertEquals(null, config.protoSource)
    }

    @Test
    fun protoSourceLocalGithubAndBuf() {
        val local = ProtoSource.local("protos")
        val github = ProtoSource.github("https://github.com/you/protos.git", "v1.2.3", "protos")
        val buf = ProtoSource.buf("buf.build/acme/petapis", "1.0.0")
        assertEquals(ProtoSource.Local("protos"), local)
        assertEquals(
            ProtoSource.Github("https://github.com/you/protos.git", "v1.2.3", "protos"),
            github,
        )
        assertEquals(ProtoSource.Buf("buf.build/acme/petapis", "1.0.0"), buf)
        val config = ProtoClientConfig(
            baseUrl = "https://api.example.com",
            protoSource = local,
        )
        assertEquals(local, config.protoSource)
        assertEquals(github, config.copy(protoSource = github).protoSource)
    }
}

class CodecRoundTripTest {
    @Test
    fun userRoundTrip() {
        val user = mapOf(
            "id" to "1",
            "name" to "Ada Lovelace",
            "email" to "ada@example.com",
            "age" to 36,
            "phone" to "+1",
            "created_at" to 100L,
            "updated_at" to 200L,
        )
        val bytes = User.encode(user)
        val decoded = User.decode(bytes)
        assertEquals("1", decoded["id"])
        assertEquals("Ada Lovelace", decoded["name"])
        assertEquals(36, decoded["age"])
        assertEquals(100L, decoded["created_at"])
    }

    @Test
    fun getUserRequestRoundTrip() {
        val bytes = GetUserRequest.encode(mapOf("id" to "42"))
        assertEquals("42", GetUserRequest.decode(bytes)["id"])
    }
}

class PathTemplateTest {
    @Test
    fun connectPreset() {
        val entry = methodMap.first { it.methodName == "GetUser" }
        val path = createPathForMethod(PathTemplatePreset.Connect)(entry)
        assertEquals("/demo.UserService/GetUser", path)
    }

    @Test
    fun restishPreset() {
        val entry = methodMap.first { it.methodName == "GetUser" }
        val path = createPathForMethod(PathTemplatePreset.Restish)(entry)
        assertEquals("/v1/userService/getUser", path)
    }
}

class CreateClientTest {
    @Test
    fun buildsUserServiceMethods() = runTest {
        val client = createClient(ProtoClientConfig(baseUrl = "https://example.com"))
        assertTrue(client.services.containsKey("userService"))
        assertTrue(client.services["userService"]!!.containsKey("getUser"))
        assertEquals(7, methodMap.size)
        assertTrue(defaultCodecs.has("demo.User"))
    }

    @Test
    fun emptyMethodMapFails() {
        val error = assertFailsWith<ProtoClientError> {
            createClient(
                ProtoClientConfig(baseUrl = "https://example.com"),
                emptyList(),
                defaultCodecs,
            )
        }
        assertEquals(ProtoErrorCode.INVALID_CONFIG, error.code)
    }
}

class WireVarintTest {
    @Test
    fun writesAndReadsVarint() {
        val buffer = Buffer()
        ProtoWire.writeVarint32(buffer, 300)
        assertEquals(300, ProtoWire.readVarint32(buffer))
    }
}
