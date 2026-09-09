package io.github.vishalsharma7nov.kmpproto.connect

import io.github.vishalsharma7nov.kmpproto.errors.ProtoClientError
import io.github.vishalsharma7nov.kmpproto.errors.ProtoErrorCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

public const val CONNECT_FLAG_END_STREAM: Int = 0x02

public data class ConnectEnvelope(
    val flags: Int,
    val data: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConnectEnvelope) return false
        return flags == other.flags && data.contentEquals(other.data)
    }

    override fun hashCode(): Int = 31 * flags + data.contentHashCode()
}

public fun encodeConnectEnvelope(data: ByteArray, flags: Int = 0): ByteArray {
    val out = ByteArray(5 + data.size)
    out[0] = flags.toByte()
    val len = data.size
    out[1] = ((len ushr 24) and 0xFF).toByte()
    out[2] = ((len ushr 16) and 0xFF).toByte()
    out[3] = ((len ushr 8) and 0xFF).toByte()
    out[4] = (len and 0xFF).toByte()
    data.copyInto(out, 5)
    return out
}

public fun decodeConnectEnvelopes(bytes: ByteArray): List<ConnectEnvelope> {
    val envelopes = mutableListOf<ConnectEnvelope>()
    var offset = 0
    while (offset + 5 <= bytes.size) {
        val flags = bytes[offset].toInt() and 0xFF
        val len =
            ((bytes[offset + 1].toInt() and 0xFF) shl 24) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 3].toInt() and 0xFF) shl 8) or
                (bytes[offset + 4].toInt() and 0xFF)
        offset += 5
        if (offset + len > bytes.size) break
        val data = bytes.copyOfRange(offset, offset + len)
        offset += len
        envelopes.add(ConnectEnvelope(flags, data))
    }
    return envelopes
}

private val json = Json { ignoreUnknownKeys = true }

public fun tryParseConnectError(
    bodyText: String,
    rpcPath: String,
    status: Int?,
): ProtoClientError? {
    if (bodyText.isBlank()) return null
    return try {
        val root = json.parseToJsonElement(bodyText)
        val obj = root.jsonObject
        val err = obj["error"]?.jsonObject ?: obj
        val code = err["code"]?.jsonPrimitive?.contentOrNull
        val message = err["message"]?.jsonPrimitive?.contentOrNull
        if (code == null && message == null) null
        else connectErrorFromJson(code, message, rpcPath, status)
    } catch (_: Exception) {
        null
    }
}

public fun connectErrorFromJson(
    code: String?,
    message: String?,
    rpcPath: String,
    status: Int?,
): ProtoClientError {
    val retryable = code == "unavailable" || code == "resource_exhausted" || code == "aborted"
    return ProtoClientError(
        code = ProtoErrorCode.RPC,
        message = message ?: "Connect RPC error${code?.let { " ($it)" } ?: ""}",
        status = status,
        rpcPath = rpcPath,
        retryable = retryable,
    )
}

public fun connectErrorFromJsonObject(
    err: JsonObject,
    rpcPath: String,
    status: Int?,
): ProtoClientError {
    val code = err["code"]?.jsonPrimitive?.contentOrNull
    val message = err["message"]?.jsonPrimitive?.contentOrNull
    return connectErrorFromJson(code, message, rpcPath, status)
}
