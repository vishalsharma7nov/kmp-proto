package io.github.vishalsharma7nov.kmpproto.path

import io.github.vishalsharma7nov.kmpproto.MethodMapEntry

public enum class PathTemplatePreset {
    Connect,
    GrpcWeb,
    Envoy,
    GrpcGateway,
    Restish,
}

private fun packagePrefix(entry: MethodMapEntry): String =
    if (entry.packageName.isNotEmpty()) "${entry.packageName}." else ""

private fun lowerFirst(value: String): String =
    if (value.isEmpty()) value else value.replaceFirstChar { it.lowercase() }

/**
 * Built-in path builders for common gateways and proxies.
 *
 * - Connect / GrpcWeb / Envoy / GrpcGateway: `/{package}.{Service}/{Method}`
 * - Restish: `/v1/{service}/{method}` (lowercase camel segments)
 */
public fun createPathForMethod(
    preset: PathTemplatePreset = PathTemplatePreset.Connect,
): (MethodMapEntry) -> String {
    return when (preset) {
        PathTemplatePreset.Restish -> { entry ->
            "/v1/${lowerFirst(entry.serviceName)}/${lowerFirst(entry.methodName)}"
        }
        PathTemplatePreset.Connect,
        PathTemplatePreset.GrpcWeb,
        PathTemplatePreset.Envoy,
        PathTemplatePreset.GrpcGateway,
        -> { entry ->
            "/${packagePrefix(entry)}${entry.serviceName}/${entry.methodName}"
        }
    }
}

/**
 * Custom template. Placeholders:
 * `{package}` `{service}` `{method}` `{rpcPath}`
 * `{serviceCamel}` `{methodCamel}`
 */
public fun pathFromTemplate(template: String): (MethodMapEntry) -> String = { entry ->
    template
        .replace("{package}", entry.packageName)
        .replace("{service}", entry.serviceName)
        .replace("{method}", entry.methodName)
        .replace("{rpcPath}", entry.rpcPath)
        .replace("{serviceCamel}", lowerFirst(entry.serviceName))
        .replace("{methodCamel}", lowerFirst(entry.methodName))
}
