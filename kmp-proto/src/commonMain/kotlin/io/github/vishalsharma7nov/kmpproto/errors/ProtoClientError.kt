package io.github.vishalsharma7nov.kmpproto.errors

/**
 * Structured error codes for all client failures.
 * Nothing is swallowed; unknown errors become [ProtoErrorCode.INTERNAL].
 */
public enum class ProtoErrorCode {
    INVALID_CONFIG,
    INVALID_ARGUMENT,
    TIMEOUT,
    NETWORK,
    HTTP,
    RPC,
    DECODE,
    PAYLOAD_TOO_LARGE,
    ABORTED,
    INTERNAL,
    UNSUPPORTED,
}

/**
 * Typed failure from config, encode, transport, decode, streaming, or native bridges.
 */
public class ProtoClientError(
    public val code: ProtoErrorCode,
    override val message: String,
    public val status: Int? = null,
    public val rpcPath: String? = null,
    override val cause: Throwable? = null,
    public val retryable: Boolean = false,
) : Exception(message, cause) {
    override fun toString(): String =
        "ProtoClientError(code=$code, message=$message, status=$status, rpcPath=$rpcPath, retryable=$retryable)"
}

/** Returns true when [value] is a [ProtoClientError]. */
public fun isProtoClientError(value: Any?): Boolean = value is ProtoClientError

/** Returns the error code when [value] is a [ProtoClientError], otherwise null. */
public fun getErrorCode(value: Any?): ProtoErrorCode? =
    (value as? ProtoClientError)?.code

/** Wraps any throwable as [ProtoClientError], preserving existing typed errors. */
public fun wrapUnknownError(value: Throwable, rpcPath: String? = null): ProtoClientError {
    if (value is ProtoClientError) {
        return value
    }
    if (value is kotlinx.coroutines.CancellationException) {
        return ProtoClientError(
            code = ProtoErrorCode.ABORTED,
            message = "Request was aborted",
            rpcPath = rpcPath,
            cause = value,
            retryable = false,
        )
    }
    return ProtoClientError(
        code = ProtoErrorCode.INTERNAL,
        message = value.message ?: "Unexpected client failure",
        rpcPath = rpcPath,
        cause = value,
        retryable = false,
    )
}
