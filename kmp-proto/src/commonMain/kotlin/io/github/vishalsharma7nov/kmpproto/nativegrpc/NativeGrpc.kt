package io.github.vishalsharma7nov.kmpproto.nativegrpc

import io.github.vishalsharma7nov.kmpproto.CodecRegistry
import io.github.vishalsharma7nov.kmpproto.StreamCallInput
import io.github.vishalsharma7nov.kmpproto.UnaryCallInput
import io.github.vishalsharma7nov.kmpproto.errors.ProtoClientError
import io.github.vishalsharma7nov.kmpproto.errors.ProtoErrorCode
import io.github.vishalsharma7nov.kmpproto.transport.ResolvedTransportConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Platform bridge for true gRPC over HTTP/2.
 * Apps that do not need native stay on HTTP / Connect.
 */
public interface NativeGrpcBridge {
    public suspend fun unary(
        baseUrl: String,
        rpcPath: String,
        requestBytes: ByteArray,
        headers: Map<String, String>,
        timeoutMs: Long,
    ): ByteArray

    public fun stream(
        baseUrl: String,
        rpcPath: String,
        requestBytes: ByteArray,
        headers: Map<String, String>,
        timeoutMs: Long,
    ): Flow<ByteArray>? = null
}

/** Optional host-installed bridge (tests / app wiring). */
public var installedNativeGrpcBridge: NativeGrpcBridge? = null

public expect fun platformNativeGrpcBridge(): NativeGrpcBridge?

public fun isNativeGrpcAvailable(): Boolean =
    installedNativeGrpcBridge != null || platformNativeGrpcBridge() != null

public fun resolveNativeGrpcBridge(): NativeGrpcBridge =
    installedNativeGrpcBridge
        ?: platformNativeGrpcBridge()
        ?: throw ProtoClientError(
            code = ProtoErrorCode.UNSUPPORTED,
            message = "Native gRPC bridge is not installed. Link the native module or use transport Http / Connect.",
        )

public suspend fun nativeGrpcUnary(
    config: ResolvedTransportConfig,
    codecs: CodecRegistry,
    input: UnaryCallInput,
): Map<String, Any?> {
    val bridge = resolveNativeGrpcBridge()
    val entry = input.entry
    val requestCodec = codecs.get(entry.requestType)
    val responseCodec = codecs.get(entry.responseType)
    if (requestCodec == null || responseCodec == null) {
        throw ProtoClientError(
            code = ProtoErrorCode.INVALID_ARGUMENT,
            message = "Missing codec for ${entry.requestType} or ${entry.responseType}",
            rpcPath = entry.rpcPath,
        )
    }

    val requestBytes = try {
        requestCodec.encode(input.request)
    } catch (cause: Throwable) {
        throw ProtoClientError(
            code = ProtoErrorCode.INVALID_ARGUMENT,
            message = "Failed to encode request for ${entry.rpcPath}",
            rpcPath = entry.rpcPath,
            cause = cause,
        )
    }

    val headers = try {
        config.getHeaders?.invoke() ?: emptyMap()
    } catch (cause: Throwable) {
        throw ProtoClientError(
            code = ProtoErrorCode.INVALID_ARGUMENT,
            message = "getHeaders() failed",
            rpcPath = entry.rpcPath,
            cause = cause,
        )
    }

    val responseBytes = try {
        bridge.unary(
            baseUrl = config.baseUrl,
            rpcPath = entry.rpcPath,
            requestBytes = requestBytes,
            headers = headers,
            timeoutMs = config.timeoutMs,
        )
    } catch (cause: Throwable) {
        throw ProtoClientError(
            code = ProtoErrorCode.RPC,
            message = "Native gRPC call failed for ${entry.rpcPath}",
            rpcPath = entry.rpcPath,
            cause = cause,
            retryable = true,
        )
    }

    if (responseBytes.size > config.maxResponseBytes) {
        throw ProtoClientError(
            code = ProtoErrorCode.PAYLOAD_TOO_LARGE,
            message = "Native response exceeds maxResponseBytes ${config.maxResponseBytes}",
            rpcPath = entry.rpcPath,
        )
    }

    return try {
        responseCodec.decode(responseBytes)
    } catch (cause: Throwable) {
        throw ProtoClientError(
            code = ProtoErrorCode.DECODE,
            message = "Failed to decode native gRPC response for ${entry.rpcPath}",
            rpcPath = entry.rpcPath,
            cause = cause,
        )
    }
}

public fun nativeGrpcStream(
    config: ResolvedTransportConfig,
    codecs: CodecRegistry,
    input: StreamCallInput,
): Flow<Map<String, Any?>> = flow {
    val bridge = resolveNativeGrpcBridge()
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

    val frames = bridge.stream(
        baseUrl = config.baseUrl,
        rpcPath = entry.rpcPath,
        requestBytes = requestCodec.encode(input.request),
        headers = config.getHeaders?.invoke() ?: emptyMap(),
        timeoutMs = config.timeoutMs,
    ) ?: throw ProtoClientError(
        code = ProtoErrorCode.UNSUPPORTED,
        message = "Native gRPC bridge does not implement streaming",
        rpcPath = entry.rpcPath,
    )

    frames.collect { frame ->
        if (frame.size > config.maxResponseBytes) {
            throw ProtoClientError(
                code = ProtoErrorCode.PAYLOAD_TOO_LARGE,
                message = "Native stream frame exceeds maxResponseBytes",
                rpcPath = entry.rpcPath,
            )
        }
        val message = responseCodec.decode(frame)
        input.onMessage?.invoke(message)
        emit(message)
    }
}
