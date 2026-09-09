package io.github.vishalsharma7nov.kmpproto.client

import io.github.vishalsharma7nov.kmpproto.CodecRegistry
import io.github.vishalsharma7nov.kmpproto.HeaderMap
import io.github.vishalsharma7nov.kmpproto.MethodKind
import io.github.vishalsharma7nov.kmpproto.MethodMap
import io.github.vishalsharma7nov.kmpproto.ProtoClient
import io.github.vishalsharma7nov.kmpproto.ProtoClientConfig
import io.github.vishalsharma7nov.kmpproto.RpcMethod
import io.github.vishalsharma7nov.kmpproto.StreamCallInput
import io.github.vishalsharma7nov.kmpproto.UnaryCallInput
import io.github.vishalsharma7nov.kmpproto.errors.ProtoClientError
import io.github.vishalsharma7nov.kmpproto.errors.ProtoErrorCode
import io.github.vishalsharma7nov.kmpproto.streaming.streamCall
import io.github.vishalsharma7nov.kmpproto.transport.resolveConfig
import io.github.vishalsharma7nov.kmpproto.transport.unaryCall

private const val MAX_METHODS = 2_000

private fun toCamelServiceName(serviceName: String): String =
    if (serviceName.isEmpty()) serviceName
    else serviceName.replaceFirstChar { it.lowercase() }

private fun toCamelMethodName(methodName: String): String =
    if (methodName.isEmpty()) methodName
    else methodName.replaceFirstChar { it.lowercase() }

private fun assertMethodMap(methodMap: MethodMap) {
    if (methodMap.isEmpty()) {
        throw ProtoClientError(
            code = ProtoErrorCode.INVALID_CONFIG,
            message = "methodMap is empty — generate from your .proto files first",
        )
    }
    if (methodMap.size > MAX_METHODS) {
        throw ProtoClientError(
            code = ProtoErrorCode.INVALID_CONFIG,
            message = "methodMap exceeds max methods ($MAX_METHODS)",
        )
    }
}

private class ProtoClientImpl(
    override val services: Map<String, Map<String, RpcMethod>>,
    override val config: ProtoClientConfig,
) : ProtoClient

/**
 * Builds a dynamic client from a generated method map and message codecs.
 * Apps typically call [createClient] which uses the package defaults.
 */
public fun createClientFromMap(
    config: ProtoClientConfig,
    methodMap: MethodMap,
    codecs: CodecRegistry,
): ProtoClient {
    val resolved = resolveConfig(config)
    assertMethodMap(methodMap)

    val services = linkedMapOf<String, MutableMap<String, RpcMethod>>()
    for (entry in methodMap) {
        val serviceKey = toCamelServiceName(entry.serviceName)
        val methodKey = toCamelMethodName(entry.methodName)
        val bucket = services.getOrPut(serviceKey) { linkedMapOf() }
        bucket[methodKey] = if (entry.kind == MethodKind.Unary) {
            RpcMethod.Unary { request, headers ->
                unaryCall(
                    resolved,
                    codecs,
                    UnaryCallInput(entry = entry, request = request, headers = headers),
                )
            }
        } else {
            RpcMethod.Stream { request, headers ->
                streamCall(
                    resolved,
                    codecs,
                    StreamCallInput(entry = entry, request = request, headers = headers),
                )
            }
        }
    }

    return ProtoClientImpl(
        services = services.mapValues { it.value.toMap() },
        config = config.copy(
            baseUrl = resolved.baseUrl,
            timeoutMs = resolved.timeoutMs,
            maxResponseBytes = resolved.maxResponseBytes,
            transport = resolved.transport,
        ),
    )
}

/** Shortcut to invoke a unary method by camel service + method names. */
public suspend fun ProtoClient.call(
    serviceCamel: String,
    methodCamel: String,
    request: Map<String, Any?> = emptyMap(),
    headers: HeaderMap = emptyMap(),
): Map<String, Any?> {
    val method = services[serviceCamel]?.get(methodCamel)
        ?: throw ProtoClientError(
            code = ProtoErrorCode.INVALID_ARGUMENT,
            message = "Unknown method $serviceCamel.$methodCamel",
        )
    return when (method) {
        is RpcMethod.Unary -> method(request, headers)
        is RpcMethod.Stream -> throw ProtoClientError(
            code = ProtoErrorCode.INVALID_ARGUMENT,
            message = "$serviceCamel.$methodCamel is a streaming method — use stream()",
        )
    }
}
