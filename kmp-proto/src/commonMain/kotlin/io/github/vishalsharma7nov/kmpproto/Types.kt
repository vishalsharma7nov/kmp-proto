package io.github.vishalsharma7nov.kmpproto

import io.github.vishalsharma7nov.kmpproto.errors.ProtoClientError
import io.github.vishalsharma7nov.kmpproto.interceptors.Interceptor
import io.github.vishalsharma7nov.kmpproto.path.PathTemplatePreset
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

/** How calls are sent to the server. */
public enum class TransportKind {
    Http,
    Connect,
    NativeGrpc,
}

public typealias HeaderMap = Map<String, String>

/**
 * App-level runtime configuration. Keep this outside the published library
 * (for example `kmp-proto.config.kt` in your app).
 */
public data class ProtoClientConfig(
    /** API origin, for example https://api.example.com */
    val baseUrl: String,
    /**
     * Where to load `.proto` files for generate/sync.
     *
     * Use [ProtoSource.local], [ProtoSource.github], or [ProtoSource.buf].
     * When null, generate fetches `.proto` files from the project directory
     * (`protos/`, `vendor/protos/`, or the current directory).
     */
    val protoSource: ProtoSource? = null,
    /** Optional per-request headers (auth tokens, app version, etc.) */
    val getHeaders: (suspend () -> HeaderMap)? = null,
    /** Request timeout in milliseconds (default 30000) */
    val timeoutMs: Long = 30_000L,
    /** Maximum accepted response body size in bytes (default 2 MiB) */
    val maxResponseBytes: Int = 2 * 1024 * 1024,
    /** How calls are sent to the server */
    val transport: TransportKind = TransportKind.Http,
    /** Optional Ktor client override (tests / custom engines) */
    val httpClient: HttpClient? = null,
    /** Optional logging hook — errors are still thrown */
    val onError: ((ProtoClientError) -> Unit)? = null,
    /**
     * Builds the HTTP path for a service method.
     * Default: /{package}.{Service}/{Method}
     */
    val pathForMethod: ((MethodMapEntry) -> String)? = null,
    /** Shortcut for built-in path presets (ignored if pathForMethod is set) */
    val pathPreset: PathTemplatePreset = PathTemplatePreset.Connect,
    /** Unary interceptor pipeline (auth, logging, etc.) */
    val interceptors: List<Interceptor> = emptyList(),
    /**
     * When true (default), allow a single transport-level retry on HTTP 401
     * when no auth interceptor is present.
     */
    val retryOnUnauthorized: Boolean = true,
)

public enum class MethodKind {
    Unary,
    ServerStreaming,
    ClientStreaming,
    Bidi,
}

public data class MethodMapEntry(
    val serviceName: String,
    val methodName: String,
    val packageName: String,
    val rpcPath: String,
    val requestType: String,
    val responseType: String,
    val kind: MethodKind = MethodKind.Unary,
)

public typealias MethodMap = List<MethodMapEntry>

public interface MessageCodec {
    public fun encode(message: Map<String, Any?>): ByteArray
    public fun decode(bytes: ByteArray): Map<String, Any?>
}

public interface CodecRegistry {
    public fun get(typeName: String): MessageCodec?
    public fun has(typeName: String): Boolean
}

public data class UnaryCallInput(
    val entry: MethodMapEntry,
    val request: Map<String, Any?> = emptyMap(),
    val headers: HeaderMap = emptyMap(),
)

public data class StreamCallInput(
    val entry: MethodMapEntry,
    val request: Map<String, Any?> = emptyMap(),
    val headers: HeaderMap = emptyMap(),
    val requests: List<Map<String, Any?>>? = null,
    val onMessage: ((Map<String, Any?>) -> Unit)? = null,
)

/**
 * Dynamic client surface: `api.userService.getUser(mapOf("id" to "1"))`.
 * Streaming methods return [Flow].
 */
public interface ProtoClient {
    public val services: Map<String, Map<String, RpcMethod>>
    public val config: ProtoClientConfig
    public operator fun get(serviceName: String): Map<String, RpcMethod>? = services[serviceName]
}

public sealed interface RpcMethod {
    public data class Unary(
        val handler: suspend (request: Map<String, Any?>, headers: HeaderMap) -> Map<String, Any?>,
    ) : RpcMethod {
        public suspend operator fun invoke(
            request: Map<String, Any?> = emptyMap(),
            headers: HeaderMap = emptyMap(),
        ): Map<String, Any?> = handler(request, headers)
    }

    public data class Stream(
        val handler: (request: Map<String, Any?>, headers: HeaderMap) -> Flow<Map<String, Any?>>,
    ) : RpcMethod {
        public operator fun invoke(
            request: Map<String, Any?> = emptyMap(),
            headers: HeaderMap = emptyMap(),
        ): Flow<Map<String, Any?>> = handler(request, headers)
    }
}

/** Convenience accessors matching RN-style `api.userService.getUser`. */
public val ProtoClient.userService: Map<String, RpcMethod>
    get() = services["userService"]
        ?: error("userService not found in method map — regenerate from your .proto files")
