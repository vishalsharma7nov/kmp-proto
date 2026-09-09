package io.github.vishalsharma7nov.kmpproto.gradle

import java.io.File

object CodeGenerator {
    fun writeAll(outDir: File, packageName: String, protos: List<ParsedProto>) {
        outDir.mkdirs()
        writeMethodMap(File(outDir, "MethodMap.kt"), packageName, protos)
        writeMessages(File(outDir, "Messages.kt"), packageName, protos)
        writeCreateApi(File(outDir, "CreateApi.kt"), packageName, protos)
    }

    private fun fq(packageName: String, type: String): String {
        if (type.contains('.')) return type
        return if (packageName.isEmpty()) type else "$packageName.$type"
    }

    private fun kindOf(method: ParsedMethod): String = when {
        method.clientStreaming && method.serverStreaming -> "Bidi"
        method.clientStreaming -> "ClientStreaming"
        method.serverStreaming -> "ServerStreaming"
        else -> "Unary"
    }

    private fun writeMethodMap(file: File, packageName: String, protos: List<ParsedProto>) {
        val entries = buildString {
            for (proto in protos) {
                for (service in proto.services) {
                    for (method in service.methods) {
                        appendLine(
                            """
                            |    MethodMapEntry(
                            |        serviceName = "${service.name}",
                            |        methodName = "${method.name}",
                            |        packageName = "${proto.packageName}",
                            |        rpcPath = "${proto.packageName}.${service.name}/${method.name}",
                            |        requestType = "${fq(proto.packageName, method.requestType)}",
                            |        responseType = "${fq(proto.packageName, method.responseType)}",
                            |        kind = MethodKind.${kindOf(method)},
                            |    ),
                            """.trimMargin(),
                        )
                    }
                }
            }
        }
        file.writeText(
            """
            |package $packageName
            |
            |import io.github.vishalsharma7nov.kmpproto.MethodKind
            |import io.github.vishalsharma7nov.kmpproto.MethodMap
            |import io.github.vishalsharma7nov.kmpproto.MethodMapEntry
            |
            |/** Auto-generated method map. Do not edit by hand. */
            |public val methodMap: MethodMap = listOf(
            |$entries
            |)
            """.trimMargin() + "\n",
        )
    }

    private fun isScalar(type: String): Boolean =
        type in setOf(
            "string", "bool", "bytes",
            "int32", "sint32", "uint32", "fixed32", "sfixed32",
            "int64", "sint64", "uint64", "fixed64", "sfixed64",
            "float", "double",
        )

    private fun simpleTypeName(type: String): String = type.substringAfterLast('.')

    private fun writeMessages(file: File, packageName: String, protos: List<ParsedProto>) {
        val allMessages = protos.flatMap { proto ->
            proto.messages.map { msg -> proto.packageName to msg }
        }
        val messageNames = allMessages.map { it.second.name }.toSet()

        val helpers = buildString {
            for ((pkg, message) in allMessages) {
                appendLine(generateEncodeFn(message, messageNames))
                appendLine(generateDecodeFn(message, messageNames))
                appendLine()
            }
        }

        val codecs = buildString {
            for ((pkg, message) in allMessages) {
                val typeName = fq(pkg, message.name)
                appendLine(
                    """
                    |public val ${message.name}: MessageCodec = mapCodec(
                    |    encode = { message, buffer -> encode${message.name}(message, buffer) },
                    |    decode = { buffer -> decode${message.name}(buffer) },
                    |)
                    """.trimMargin(),
                )
                appendLine()
            }
        }

        val codecEntries = allMessages.joinToString(",\n") { (pkg, message) ->
            "        \"${fq(pkg, message.name)}\" to ${message.name}"
        }

        file.writeText(
            """
            |package $packageName
            |
            |import io.github.vishalsharma7nov.kmpproto.CodecRegistry
            |import io.github.vishalsharma7nov.kmpproto.MessageCodec
            |import io.github.vishalsharma7nov.kmpproto.codecs.ProtoWire
            |import io.github.vishalsharma7nov.kmpproto.codecs.createCodecRegistry
            |import io.github.vishalsharma7nov.kmpproto.codecs.mapCodec
            |import okio.Buffer
            |
            |/** Auto-generated message codecs. Do not edit by hand. */
            |
            |$helpers
            |$codecs
            |public val defaultCodecs: CodecRegistry = createCodecRegistry(
            |    mapOf(
            |$codecEntries
            |    ),
            |)
            """.trimMargin() + "\n",
        )
    }

    private fun generateEncodeFn(message: ParsedMessage, messageNames: Set<String>): String {
        val body = message.fields.joinToString("\n") { field ->
            val type = simpleTypeName(field.type)
            when {
                field.repeated && messageNames.contains(type) ->
                    """
                    |    @Suppress("UNCHECKED_CAST")
                    |    val ${field.name}List = (message["${field.name}"] as? List<*>)?.mapNotNull { it as? Map<String, Any?> } ?: emptyList()
                    |    for (item in ${field.name}List) {
                    |        val nested = Buffer()
                    |        encode$type(item, nested)
                    |        ProtoWire.writeMessage(buffer, ${field.number}, nested.readByteArray())
                    |    }
                    """.trimMargin()
                field.repeated && field.type == "string" ->
                    """
                    |    @Suppress("UNCHECKED_CAST")
                    |    val ${field.name}Vals = (message["${field.name}"] as? List<*>) ?: emptyList()
                    |    for (item in ${field.name}Vals) {
                    |        ProtoWire.writeString(buffer, ${field.number}, (item as? String) ?: "")
                    |    }
                    """.trimMargin()
                messageNames.contains(type) ->
                    """
                    |    @Suppress("UNCHECKED_CAST")
                    |    val ${field.name}Msg = message["${field.name}"] as? Map<String, Any?>
                    |    if (${field.name}Msg != null) {
                    |        val nested = Buffer()
                    |        encode$type(${field.name}Msg, nested)
                    |        ProtoWire.writeMessage(buffer, ${field.number}, nested.readByteArray())
                    |    }
                    """.trimMargin()
                field.type == "string" ->
                    """    ProtoWire.writeString(buffer, ${field.number}, (message["${field.name}"] as? String) ?: "")"""
                field.type == "bool" ->
                    """    ProtoWire.writeBool(buffer, ${field.number}, message["${field.name}"] as? Boolean ?: false)"""
                field.type == "bytes" ->
                    """    ProtoWire.writeBytes(buffer, ${field.number}, (message["${field.name}"] as? ByteArray) ?: ByteArray(0))"""
                field.type == "int64" || field.type == "sint64" || field.type == "uint64" || field.type == "fixed64" || field.type == "sfixed64" ->
                    """    ProtoWire.writeInt64(buffer, ${field.number}, ((message["${field.name}"] as? Number)?.toLong()) ?: 0L)"""
                isScalar(field.type) ->
                    """    ProtoWire.writeInt32(buffer, ${field.number}, ((message["${field.name}"] as? Number)?.toInt()) ?: 0)"""
                else -> "    // skipped field ${field.name}"
            }
        }
        return """
            |private fun encode${message.name}(message: Map<String, Any?>, buffer: Buffer) {
            |$body
            |}
            """.trimMargin()
    }

    private fun generateDecodeFn(message: ParsedMessage, messageNames: Set<String>): String {
        val defaults = message.fields.joinToString(",\n        ") { field ->
            val type = simpleTypeName(field.type)
            val default = when {
                field.repeated -> "mutableListOf<Any?>()"
                field.type == "string" -> "\"\""
                field.type == "bool" -> "false"
                field.type == "bytes" -> "ByteArray(0)"
                field.type == "int64" || field.type == "sint64" || field.type == "uint64" || field.type == "fixed64" || field.type == "sfixed64" -> "0L"
                messageNames.contains(type) -> "emptyMap<String, Any?>()"
                isScalar(field.type) -> "0"
                else -> "null"
            }
            "\"${field.name}\" to $default"
        }

        val cases = message.fields.joinToString("\n            ") { field ->
            val type = simpleTypeName(field.type)
            val body = when {
                field.repeated && messageNames.contains(type) ->
                    """
                    |if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    |                    val len = ProtoWire.readVarint32(buffer)
                    |                    val nested = Buffer().write(buffer.readByteArray(len.toLong()))
                    |                    @Suppress("UNCHECKED_CAST")
                    |                    (out["${field.name}"] as MutableList<Any?>).add(decode$type(nested))
                    |                } else ProtoWire.skip(buffer, wire)
                    """.trimMargin()
                messageNames.contains(type) ->
                    """
                    |if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    |                    val len = ProtoWire.readVarint32(buffer)
                    |                    val nested = Buffer().write(buffer.readByteArray(len.toLong()))
                    |                    out["${field.name}"] = decode$type(nested)
                    |                } else ProtoWire.skip(buffer, wire)
                    """.trimMargin()
                field.type == "string" ->
                    """
                    |if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    |                    val len = ProtoWire.readVarint32(buffer)
                    |                    out["${field.name}"] = buffer.readByteArray(len.toLong()).decodeToString()
                    |                } else ProtoWire.skip(buffer, wire)
                    """.trimMargin()
                field.type == "bool" ->
                    """
                    |if (wire == ProtoWire.WIRE_VARINT) out["${field.name}"] = ProtoWire.readVarint32(buffer) != 0
                    |                else ProtoWire.skip(buffer, wire)
                    """.trimMargin()
                field.type == "bytes" ->
                    """
                    |if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    |                    val len = ProtoWire.readVarint32(buffer)
                    |                    out["${field.name}"] = buffer.readByteArray(len.toLong())
                    |                } else ProtoWire.skip(buffer, wire)
                    """.trimMargin()
                field.type == "int64" || field.type == "sint64" || field.type == "uint64" || field.type == "fixed64" || field.type == "sfixed64" ->
                    """
                    |if (wire == ProtoWire.WIRE_VARINT) out["${field.name}"] = ProtoWire.readVarint64(buffer)
                    |                else ProtoWire.skip(buffer, wire)
                    """.trimMargin()
                else ->
                    """
                    |if (wire == ProtoWire.WIRE_VARINT) out["${field.name}"] = ProtoWire.readVarint32(buffer)
                    |                else ProtoWire.skip(buffer, wire)
                    """.trimMargin()
            }
            """
            |${field.number} -> {
            |                $body
            |            }
            """.trimMargin()
        }

        return """
            |private fun decode${message.name}(buffer: Buffer): MutableMap<String, Any?> {
            |    val out = mutableMapOf<String, Any?>(
            |        $defaults
            |    )
            |    while (!buffer.exhausted()) {
            |        val tag = ProtoWire.readVarint32(buffer)
            |        val field = tag ushr 3
            |        val wire = tag and 0x7
            |        when (field) {
            |            $cases
            |            else -> ProtoWire.skip(buffer, wire)
            |        }
            |    }
            |    return out
            |}
            """.trimMargin()
    }

    private fun writeCreateApi(file: File, packageName: String, protos: List<ParsedProto>) {
        val services = protos.flatMap { it.services }
        val serviceClasses = services.joinToString("\n\n") { service ->
            val methods = service.methods.joinToString("\n    ") { method ->
                val camel = method.name.replaceFirstChar { it.lowercase() }
                if (method.clientStreaming || method.serverStreaming) {
                    """public fun $camel(request: Map<String, Any?> = emptyMap()) = (methods["$camel"] as io.github.vishalsharma7nov.kmpproto.RpcMethod.Stream)(request)"""
                } else {
                    """public suspend fun $camel(request: Map<String, Any?> = emptyMap()): Map<String, Any?> = (methods["$camel"] as io.github.vishalsharma7nov.kmpproto.RpcMethod.Unary)(request)"""
                }
            }
            """
            |public class ${service.name}Api internal constructor(private val methods: Map<String, io.github.vishalsharma7nov.kmpproto.RpcMethod>) {
            |    $methods
            |}
            """.trimMargin()
        }

        val props = services.joinToString("\n    ") { service ->
            val serviceCamel = service.name.replaceFirstChar { it.lowercase() }
            """public val $serviceCamel: ${service.name}Api = ${service.name}Api(client.services["$serviceCamel"] ?: emptyMap())"""
        }

        file.writeText(
            """
            |package $packageName
            |
            |import io.github.vishalsharma7nov.kmpproto.ProtoClient
            |import io.github.vishalsharma7nov.kmpproto.ProtoClientConfig
            |import io.github.vishalsharma7nov.kmpproto.client.createClientFromMap
            |
            |/** Auto-generated typed API helpers. Do not edit by hand. */
            |
            |$serviceClasses
            |
            |public class GeneratedApi internal constructor(client: ProtoClient) {
            |    $props
            |}
            |
            |public fun createApi(config: ProtoClientConfig): GeneratedApi =
            |    GeneratedApi(createClientFromMap(config, methodMap, defaultCodecs))
            """.trimMargin() + "\n",
        )
    }
}
