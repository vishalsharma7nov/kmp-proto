package io.github.vishalsharma7nov.kmpproto.interceptors

import io.github.vishalsharma7nov.kmpproto.HeaderMap
import io.github.vishalsharma7nov.kmpproto.MethodMapEntry

public data class CallContext(
    val entry: MethodMapEntry,
    val request: Map<String, Any?>,
    val headers: HeaderMap,
)

public typealias UnaryHandler = suspend (CallContext) -> Map<String, Any?>

/**
 * Interceptor around a unary RPC. Call [next] to continue the chain.
 */
public fun interface Interceptor {
    public suspend fun intercept(ctx: CallContext, next: UnaryHandler): Map<String, Any?>
}

/**
 * Compose interceptors left-to-outer (first runs first on the way in).
 */
public fun composeInterceptors(
    interceptors: List<Interceptor>,
    core: UnaryHandler,
): UnaryHandler {
    return interceptors.foldRight(core) { interceptor, next ->
        { ctx -> interceptor.intercept(ctx, next) }
    }
}
