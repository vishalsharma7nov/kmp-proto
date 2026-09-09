package io.github.vishalsharma7nov.kmpproto.auth

import io.github.vishalsharma7nov.kmpproto.errors.ProtoClientError
import io.github.vishalsharma7nov.kmpproto.interceptors.Interceptor

public data class AuthInterceptorOptions(
    val getToken: suspend () -> String?,
    val refreshToken: (suspend () -> String?)? = null,
    val headerName: String = "Authorization",
    val scheme: String = "Bearer",
)

/** Builds `Authorization: Bearer <token>` style headers. */
public suspend fun createBearerTokenHeaders(
    getToken: suspend () -> String?,
    headerName: String = "Authorization",
    scheme: String = "Bearer",
): Map<String, String> {
    val token = getToken() ?: return emptyMap()
    val value = if (scheme.isEmpty()) token else "$scheme $token"
    return mapOf(headerName to value)
}

/**
 * Unary interceptor that attaches a bearer token and optionally refreshes once on HTTP 401.
 */
public fun createAuthInterceptor(options: AuthInterceptorOptions): Interceptor =
    Interceptor { ctx, next ->
        val token = options.getToken()
        val headers = if (token.isNullOrEmpty()) {
            ctx.headers
        } else {
            val value = if (options.scheme.isEmpty()) token else "${options.scheme} $token"
            ctx.headers + (options.headerName to value)
        }
        try {
            next(ctx.copy(headers = headers))
        } catch (error: ProtoClientError) {
            if (error.status == 401 && options.refreshToken != null) {
                val refreshed = options.refreshToken.invoke()
                if (!refreshed.isNullOrEmpty()) {
                    val value =
                        if (options.scheme.isEmpty()) refreshed
                        else "${options.scheme} $refreshed"
                    return@Interceptor next(
                        ctx.copy(headers = ctx.headers + (options.headerName to value)),
                    )
                }
            }
            throw error
        }
    }
