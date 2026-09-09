package io.github.vishalsharma7nov.kmpproto.transport

import io.github.vishalsharma7nov.kmpproto.CodecRegistry
import io.github.vishalsharma7nov.kmpproto.HeaderMap
import io.github.vishalsharma7nov.kmpproto.MethodMapEntry
import io.github.vishalsharma7nov.kmpproto.ProtoClientConfig
import io.github.vishalsharma7nov.kmpproto.TransportKind
import io.github.vishalsharma7nov.kmpproto.UnaryCallInput
import io.github.vishalsharma7nov.kmpproto.connect.tryParseConnectError
import io.github.vishalsharma7nov.kmpproto.errors.ProtoClientError
import io.github.vishalsharma7nov.kmpproto.errors.ProtoErrorCode
import io.github.vishalsharma7nov.kmpproto.errors.wrapUnknownError
import io.github.vishalsharma7nov.kmpproto.interceptors.CallContext
import io.github.vishalsharma7nov.kmpproto.interceptors.Interceptor
import io.github.vishalsharma7nov.kmpproto.interceptors.composeInterceptors
import io.github.vishalsharma7nov.kmpproto.nativegrpc.nativeGrpcUnary
import io.github.vishalsharma7nov.kmpproto.path.createPathForMethod
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.ContentType
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

private const val DEFAULT_TIMEOUT_MS = 30_000L
private const val DEFAULT_MAX_RESPONSE_BYTES = 2 * 1024 * 1024
private const val MAX_HEADER_ENTRIES = 64

public data class ResolvedTransportConfig(
    val baseUrl: String,
    val timeoutMs: Long,
    val maxResponseBytes: Int,
    val transport: TransportKind,
    val getHeaders: (suspend () -> HeaderMap)?,
    val httpClient: HttpClient,
    val onError: ((ProtoClientError) -> Unit)?,
    val pathForMethod: (MethodMapEntry) -> String,
    val interceptors: List<Interceptor>,
    val retryOnUnauthorized: Boolean,
    val ownsHttpClient: Boolean,
)

public fun resolveConfig(config: ProtoClientConfig): ResolvedTransportConfig {
    val baseUrl = config.baseUrl.trim()
    if (baseUrl.isEmpty()) {
        throw ProtoClientError(
            code = ProtoErrorCode.INVALID_CONFIG,
            message = "baseUrl is required and must be a non-empty string",
        )
    }
    if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
        throw ProtoClientError(
            code = ProtoErrorCode.INVALID_CONFIG,
            message = "baseUrl must be an absolute URL, received: $baseUrl",
        )
    }

    val timeoutMs = when {
        config.timeoutMs > 0 -> minOf(config.timeoutMs, 600_000L)
        else -> DEFAULT_TIMEOUT_MS
    }
    val maxResponseBytes = when {
        config.maxResponseBytes > 0 -> minOf(config.maxResponseBytes, 50 * 1024 * 1024)
        else -> DEFAULT_MAX_RESPONSE_BYTES
    }

    val ownsClient = config.httpClient == null
    val httpClient = config.httpClient ?: createDefaultHttpClient()

    val pathForMethod = config.pathForMethod ?: createPathForMethod(config.pathPreset)

    return ResolvedTransportConfig(
        baseUrl = baseUrl.trimEnd('/'),
        timeoutMs = timeoutMs,
        maxResponseBytes = maxResponseBytes,
        transport = config.transport,
        getHeaders = config.getHeaders,
        httpClient = httpClient,
        onError = config.onError,
        pathForMethod = pathForMethod,
        interceptors = config.interceptors,
        retryOnUnauthorized = config.retryOnUnauthorized,
        ownsHttpClient = ownsClient,
    )
}

public suspend fun buildHeaders(
    config: ResolvedTransportConfig,
    contentType: String,
    rpcPath: String,
    extra: HeaderMap = emptyMap(),
): Map<String, String> {
    val headers = linkedMapOf(
        "Content-Type" to contentType,
        "Accept" to contentType,
    )

    fun merge(source: HeaderMap?) {
        if (source == null) return
        if (source.size > MAX_HEADER_ENTRIES) {
            throw ProtoClientError(
                code = ProtoErrorCode.INVALID_ARGUMENT,
                message = "Too many headers (max $MAX_HEADER_ENTRIES)",
                rpcPath = rpcPath,
            )
        }
        source.forEach { (k, v) -> headers[k] = v }
    }

    if (config.getHeaders != null) {
        val fromConfig = try {
            config.getHeaders.invoke()
        } catch (cause: Throwable) {
            throw ProtoClientError(
                code = ProtoErrorCode.INVALID_ARGUMENT,
                message = "getHeaders() failed",
                rpcPath = rpcPath,
                cause = cause,
            )
        }
        merge(fromConfig)
    }
    merge(extra)
    return headers
}

private fun mapHttpStatus(status: Int, rpcPath: String, bodyText: String): ProtoClientError {
    if (bodyText.isNotEmpty()) {
        tryParseConnectError(bodyText, rpcPath, status)?.let { return it }
    }
    val retryable = status == 408 || status == 429 || status >= 500
    return ProtoClientError(
        code = ProtoErrorCode.HTTP,
        message = "HTTP $status for $rpcPath${if (bodyText.isNotEmpty()) ": ${bodyText.take(200)}" else ""}",
        status = status,
        rpcPath = rpcPath,
        retryable = retryable,
    )
}

private suspend fun unaryCallCore(
    config: ResolvedTransportConfig,
    codecs: CodecRegistry,
    input: UnaryCallInput,
): Map<String, Any?> {
    if (config.transport == TransportKind.NativeGrpc) {
        return nativeGrpcUnary(config, codecs, input)
    }

    val entry = input.entry
    val rpcPath = entry.rpcPath
    val requestCodec = codecs.get(entry.requestType)
    val responseCodec = codecs.get(entry.responseType)
    if (requestCodec == null || responseCodec == null) {
        throw ProtoClientError(
            code = ProtoErrorCode.INVALID_ARGUMENT,
            message = "Missing codec for ${entry.requestType} or ${entry.responseType}",
            rpcPath = rpcPath,
        )
    }

    val body = try {
        requestCodec.encode(input.request)
    } catch (cause: Throwable) {
        throw ProtoClientError(
            code = ProtoErrorCode.INVALID_ARGUMENT,
            message = "Failed to encode request for $rpcPath",
            rpcPath = rpcPath,
            cause = cause,
        )
    }

    val path = config.pathForMethod(entry)
    val url = "${config.baseUrl}${if (path.startsWith("/")) path else "/$path"}"
    val contentType = if (config.transport == TransportKind.Connect) {
        "application/proto"
    } else {
        "application/x-protobuf"
    }

    return try {
        withTimeout(config.timeoutMs) {
            val headers = buildHeaders(config, contentType, rpcPath, input.headers).toMutableMap()
            if (config.transport == TransportKind.Connect) {
                headers["Connect-Protocol-Version"] = "1"
            }

            val response = try {
                config.httpClient.post(url) {
                    headers.forEach { (k, v) -> header(k, v) }
                    contentType(ContentType.parse(contentType))
                    setBody(body)
                }
            } catch (cause: Throwable) {
                throw ProtoClientError(
                    code = ProtoErrorCode.NETWORK,
                    message = "Network error calling $rpcPath",
                    rpcPath = rpcPath,
                    cause = cause,
                    retryable = true,
                )
            }

            if (!response.status.isSuccess()) {
                val text = try {
                    response.bodyAsText()
                } catch (_: Throwable) {
                    ""
                }
                throw mapHttpStatus(response.status.value, rpcPath, text)
            }

            val bytes = response.bodyAsBytes()
            if (bytes.size > config.maxResponseBytes) {
                throw ProtoClientError(
                    code = ProtoErrorCode.PAYLOAD_TOO_LARGE,
                    message = "Response size ${bytes.size} exceeds maxResponseBytes ${config.maxResponseBytes}",
                    rpcPath = rpcPath,
                    status = response.status.value,
                )
            }

            val contentTypeHeader = response.headers["Content-Type"].orEmpty()
            if (
                config.transport == TransportKind.Connect &&
                contentTypeHeader.contains("json", ignoreCase = true) &&
                bytes.isNotEmpty()
            ) {
                val text = bytes.decodeToString()
                tryParseConnectError(text, rpcPath, response.status.value)?.let { throw it }
            }

            try {
                responseCodec.decode(bytes)
            } catch (cause: Throwable) {
                throw ProtoClientError(
                    code = ProtoErrorCode.DECODE,
                    message = "Failed to decode response for $rpcPath",
                    rpcPath = rpcPath,
                    cause = cause,
                    status = response.status.value,
                )
            }
        }
    } catch (e: TimeoutCancellationException) {
        throw ProtoClientError(
            code = ProtoErrorCode.TIMEOUT,
            message = "Request timed out after ${config.timeoutMs}ms",
            rpcPath = rpcPath,
            cause = e,
            retryable = true,
        )
    }
}

private fun notifyOnError(config: ResolvedTransportConfig, error: ProtoClientError) {
    try {
        config.onError?.invoke(error)
    } catch (_: Throwable) {
        // Logging hooks must not replace throw behavior
    }
}

public suspend fun unaryCall(
    config: ResolvedTransportConfig,
    codecs: CodecRegistry,
    input: UnaryCallInput,
): Map<String, Any?> {
    val rpcPath = input.entry.rpcPath
    val core: suspend (CallContext) -> Map<String, Any?> = { ctx ->
        unaryCallCore(
            config,
            codecs,
            UnaryCallInput(entry = ctx.entry, request = ctx.request, headers = ctx.headers),
        )
    }
    val pipeline = if (config.interceptors.isNotEmpty()) {
        composeInterceptors(config.interceptors, core)
    } else {
        core
    }

    return try {
        pipeline(
            CallContext(
                entry = input.entry,
                request = input.request,
                headers = input.headers,
            ),
        )
    } catch (error: Throwable) {
        val wrapped = wrapUnknownError(error, rpcPath)
        if (
            config.retryOnUnauthorized &&
            config.interceptors.isEmpty() &&
            wrapped.status == HttpStatusCode.Unauthorized.value &&
            config.getHeaders != null
        ) {
            try {
                return unaryCallCore(config, codecs, input)
            } catch (retryError: Throwable) {
                val retryWrapped = wrapUnknownError(retryError, rpcPath)
                notifyOnError(config, retryWrapped)
                throw retryWrapped
            }
        }
        notifyOnError(config, wrapped)
        throw wrapped
    }
}
