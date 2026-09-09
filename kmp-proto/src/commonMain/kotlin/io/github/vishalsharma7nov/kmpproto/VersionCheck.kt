package io.github.vishalsharma7nov.kmpproto

import io.github.vishalsharma7nov.kmpproto.errors.ProtoClientError
import io.github.vishalsharma7nov.kmpproto.errors.ProtoErrorCode
import io.github.vishalsharma7nov.kmpproto.interceptors.Interceptor

public const val CLIENT_VERSION_HEADER: String = "X-Client-Version"
public const val SERVER_MIN_CLIENT_HEADER: String = "X-Min-Client-Version"
public const val METHOD_MAP_HASH_HEADER: String = "X-Method-Map-Hash"

public data class VersionCheckOptions(
    val clientVersion: String,
    val methodMapHash: String? = null,
    val onServerMinClient: ((String) -> Unit)? = null,
)

public fun compareDottedVersions(a: String, b: String): Int {
    val pa = a.split('.').map { it.toIntOrNull() ?: 0 }
    val pb = b.split('.').map { it.toIntOrNull() ?: 0 }
    val n = maxOf(pa.size, pb.size)
    for (i in 0 until n) {
        val av = pa.getOrElse(i) { 0 }
        val bv = pb.getOrElse(i) { 0 }
        if (av != bv) return av.compareTo(bv)
    }
    return 0
}

public fun versionHeaders(options: VersionCheckOptions): Map<String, String> {
    val headers = linkedMapOf(CLIENT_VERSION_HEADER to options.clientVersion)
    if (!options.methodMapHash.isNullOrEmpty()) {
        headers[METHOD_MAP_HASH_HEADER] = options.methodMapHash
    }
    return headers
}

public fun assertServerMinClient(serverMin: String, clientVersion: String) {
    if (compareDottedVersions(clientVersion, serverMin) < 0) {
        throw ProtoClientError(
            code = ProtoErrorCode.UNSUPPORTED,
            message = "Client version $clientVersion is below server minimum $serverMin",
        )
    }
}

public fun createVersionInterceptor(options: VersionCheckOptions): Interceptor =
    Interceptor { ctx, next ->
        next(ctx.copy(headers = ctx.headers + versionHeaders(options)))
    }
