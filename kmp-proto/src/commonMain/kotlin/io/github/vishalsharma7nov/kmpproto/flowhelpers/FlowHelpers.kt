package io.github.vishalsharma7nov.kmpproto.flowhelpers

import io.github.vishalsharma7nov.kmpproto.HeaderMap
import io.github.vishalsharma7nov.kmpproto.ProtoClient
import io.github.vishalsharma7nov.kmpproto.RpcMethod
import io.github.vishalsharma7nov.kmpproto.client.call
import io.github.vishalsharma7nov.kmpproto.errors.ProtoClientError
import io.github.vishalsharma7nov.kmpproto.errors.ProtoErrorCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Optional coroutine helpers (KMP analog of React Query).
 * Not required — apps can call [ProtoClient] methods directly.
 */
public fun ProtoClient.asFlow(
    serviceCamel: String,
    methodCamel: String,
    request: Map<String, Any?> = emptyMap(),
    headers: HeaderMap = emptyMap(),
): Flow<Map<String, Any?>> = flow {
    emit(call(serviceCamel, methodCamel, request, headers))
}

public fun ProtoClient.streamAsFlow(
    serviceCamel: String,
    methodCamel: String,
    request: Map<String, Any?> = emptyMap(),
    headers: HeaderMap = emptyMap(),
): Flow<Map<String, Any?>> {
    val method = services[serviceCamel]?.get(methodCamel)
        ?: throw ProtoClientError(
            code = ProtoErrorCode.INVALID_ARGUMENT,
            message = "Unknown method $serviceCamel.$methodCamel",
        )
    return when (method) {
        is RpcMethod.Stream -> method(request, headers)
        is RpcMethod.Unary -> flow { emit(method(request, headers)) }
    }
}
