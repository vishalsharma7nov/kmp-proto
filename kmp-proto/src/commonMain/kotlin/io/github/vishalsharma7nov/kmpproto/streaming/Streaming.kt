package io.github.vishalsharma7nov.kmpproto.streaming

import io.github.vishalsharma7nov.kmpproto.CodecRegistry
import io.github.vishalsharma7nov.kmpproto.MessageCodec
import io.github.vishalsharma7nov.kmpproto.MethodKind
import io.github.vishalsharma7nov.kmpproto.StreamCallInput
import io.github.vishalsharma7nov.kmpproto.TransportKind
import io.github.vishalsharma7nov.kmpproto.connect.CONNECT_FLAG_END_STREAM
import io.github.vishalsharma7nov.kmpproto.connect.decodeConnectEnvelopes
import io.github.vishalsharma7nov.kmpproto.connect.encodeConnectEnvelope
import io.github.vishalsharma7nov.kmpproto.connect.tryParseConnectError
import io.github.vishalsharma7nov.kmpproto.errors.ProtoClientError
import io.github.vishalsharma7nov.kmpproto.errors.ProtoErrorCode
import io.github.vishalsharma7nov.kmpproto.nativegrpc.nativeGrpcStream
import io.github.vishalsharma7nov.kmpproto.transport.ResolvedTransportConfig
import io.github.vishalsharma7nov.kmpproto.transport.buildHeaders
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

private fun collectRequests(input: StreamCallInput): List<Map<String, Any?>> =
    input.requests ?: listOf(input.request)

private fun buildConnectRequestBody(
    entryKind: MethodKind,
    requests: List<Map<String, Any?>>,
    requestCodec: MessageCodec,
): ByteArray {
    val useEnveloped =
        entryKind == MethodKind.ClientStreaming ||
            entryKind == MethodKind.Bidi ||
            requests.size > 1
    if (!useEnveloped) {
        return requestCodec.encode(requests.firstOrNull() ?: emptyMap())
    }
    val frames = mutableListOf<ByteArray>()
    for (req in requests) {
        frames.add(encodeConnectEnvelope(requestCodec.encode(req), 0))
    }
    if (entryKind == MethodKind.ClientStreaming || entryKind == MethodKind.Bidi) {
        frames.add(encodeConnectEnvelope(ByteArray(0), CONNECT_FLAG_END_STREAM))
    }
    val total = frames.sumOf { it.size }
    val body = ByteArray(total)
    var offset = 0
    for (frame in frames) {
        frame.copyInto(body, offset)
        offset += frame.size
    }
    return body
}

private fun throwIfEndStreamError(data: ByteArray, rpcPath: String, status: Int?) {
    if (data.isEmpty()) return
    val text = data.decodeToString()
    tryParseConnectError(text, rpcPath, status)?.let { throw it }
    val root = Json.parseToJsonElement(text).jsonObject
    val err = root["error"]?.jsonObject ?: root
    val code = err["code"]?.jsonPrimitive?.contentOrNull
    val message = err["message"]?.jsonPrimitive?.contentOrNull
    if (code != null || message != null) {
        throw ProtoClientError(
            code = ProtoErrorCode.RPC,
            message = message ?: "Connect stream error",
            status = status,
            rpcPath = rpcPath,
        )
    }
}

/**
 * Streaming RPC helper. Supports Connect envelopes and native gRPC stream bridges.
 * HTTP-only backends should use Connect transport for streaming.
 */
public fun streamCall(
    config: ResolvedTransportConfig,
    codecs: CodecRegistry,
    input: StreamCallInput,
): Flow<Map<String, Any?>> = flow {
    if (config.transport == TransportKind.NativeGrpc) {
        nativeGrpcStream(config, codecs, input).collect { emit(it) }
        return@flow
    }

    if (config.transport == TransportKind.Http) {
        throw ProtoClientError(
            code = ProtoErrorCode.UNSUPPORTED,
            message = "Streaming requires transport Connect or NativeGrpc (HTTP+protobuf unary only)",
            rpcPath = input.entry.rpcPath,
        )
    }

    val entry = input.entry
    val requestCodec = codecs.get(entry.requestType)
        ?: throw ProtoClientError(
            code = ProtoErrorCode.INVALID_ARGUMENT,
            message = "Missing codec for ${entry.requestType}",
            rpcPath = entry.rpcPath,
        )
    val responseCodec = codecs.get(entry.responseType)
        ?: throw ProtoClientError(
            code = ProtoErrorCode.INVALID_ARGUMENT,
            message = "Missing codec for ${entry.responseType}",
            rpcPath = entry.rpcPath,
        )

    val requests = collectRequests(input)
    val body = buildConnectRequestBody(entry.kind, requests, requestCodec)
    val path = config.pathForMethod(entry)
    val url = "${config.baseUrl}${if (path.startsWith("/")) path else "/$path"}"
    val headers = buildHeaders(config, "application/connect+proto", entry.rpcPath, input.headers)
        .toMutableMap()
    headers["Connect-Protocol-Version"] = "1"

    val response = config.httpClient.post(url) {
        headers.forEach { (k, v) -> header(k, v) }
        contentType(ContentType.parse("application/connect+proto"))
        setBody(body)
    }

    if (!response.status.isSuccess()) {
        val text = try {
            response.bodyAsText()
        } catch (_: Throwable) {
            ""
        }
        throw ProtoClientError(
            code = ProtoErrorCode.HTTP,
            message = "HTTP ${response.status.value} for ${entry.rpcPath}",
            status = response.status.value,
            rpcPath = entry.rpcPath,
            retryable = response.status.value >= 500,
        ).also {
            tryParseConnectError(text, entry.rpcPath, response.status.value)?.let { throw it }
        }
    }

    val bytes = response.bodyAsBytes()
    if (bytes.size > config.maxResponseBytes) {
        throw ProtoClientError(
            code = ProtoErrorCode.PAYLOAD_TOO_LARGE,
            message = "Stream response exceeds maxResponseBytes",
            rpcPath = entry.rpcPath,
        )
    }

    for (envelope in decodeConnectEnvelopes(bytes)) {
        if (envelope.flags and CONNECT_FLAG_END_STREAM != 0) {
            throwIfEndStreamError(envelope.data, entry.rpcPath, response.status.value)
            return@flow
        }
        val message = responseCodec.decode(envelope.data)
        input.onMessage?.invoke(message)
        emit(message)
    }
}
