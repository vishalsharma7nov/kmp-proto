package io.github.vishalsharma7nov.kmpproto.logging

import io.github.vishalsharma7nov.kmpproto.interceptors.Interceptor

public data class LoggingOptions(
    val log: (String) -> Unit = { println(it) },
    val logBodies: Boolean = false,
)

/** Request/response logging interceptor (does not swallow errors). */
public fun createLoggingInterceptor(options: LoggingOptions = LoggingOptions()): Interceptor =
    Interceptor { ctx, next ->
        options.log("→ ${ctx.entry.rpcPath}")
        if (options.logBodies) {
            options.log("  request keys=${ctx.request.keys}")
        }
        try {
            val result = next(ctx)
            options.log("← ${ctx.entry.rpcPath} ok")
            if (options.logBodies) {
                options.log("  response keys=${result.keys}")
            }
            result
        } catch (error: Throwable) {
            options.log("← ${ctx.entry.rpcPath} error: ${error.message}")
            throw error
        }
    }
