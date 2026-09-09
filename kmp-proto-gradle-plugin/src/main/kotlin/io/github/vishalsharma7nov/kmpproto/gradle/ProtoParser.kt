package io.github.vishalsharma7nov.kmpproto.gradle

import java.io.File

data class ParsedProto(
    val fileName: String,
    val packageName: String,
    val messages: List<ParsedMessage>,
    val services: List<ParsedService>,
)

data class ParsedMessage(
    val name: String,
    val fields: List<ParsedField>,
)

data class ParsedField(
    val repeated: Boolean,
    val type: String,
    val name: String,
    val number: Int,
)

data class ParsedService(
    val name: String,
    val methods: List<ParsedMethod>,
)

data class ParsedMethod(
    val name: String,
    val requestType: String,
    val responseType: String,
    val clientStreaming: Boolean,
    val serverStreaming: Boolean,
)

object ProtoParser {
    private val packageRegex = Regex("""^\s*package\s+([\w.]+)\s*;""", RegexOption.MULTILINE)
    private val messageRegex = Regex("""message\s+(\w+)\s*\{([^{}]*(?:\{[^{}]*\}[^{}]*)*)\}""", RegexOption.MULTILINE)
    private val fieldRegex = Regex("""^\s*(repeated\s+)?(\w+)\s+(\w+)\s*=\s*(\d+)\s*;""", RegexOption.MULTILINE)
    private val serviceRegex = Regex("""service\s+(\w+)\s*\{([^}]*)\}""", RegexOption.MULTILINE)
    private val rpcRegex =
        Regex("""rpc\s+(\w+)\s*\(\s*(stream\s+)?([\w.]+)\s*\)\s*returns\s*\(\s*(stream\s+)?([\w.]+)\s*\)\s*;""")

    fun parse(text: String, fileName: String): ParsedProto {
        val packageName = packageRegex.find(text)?.groupValues?.get(1).orEmpty()
        val messages = messageRegex.findAll(text).map { match ->
            val name = match.groupValues[1]
            val body = match.groupValues[2]
            val fields = fieldRegex.findAll(body).map { f ->
                ParsedField(
                    repeated = f.groupValues[1].isNotBlank(),
                    type = f.groupValues[2],
                    name = f.groupValues[3],
                    number = f.groupValues[4].toInt(),
                )
            }.toList()
            ParsedMessage(name, fields)
        }.toList()

        val services = serviceRegex.findAll(text).map { match ->
            val name = match.groupValues[1]
            val body = match.groupValues[2]
            val methods = rpcRegex.findAll(body).map { r ->
                ParsedMethod(
                    name = r.groupValues[1],
                    requestType = r.groupValues[3],
                    responseType = r.groupValues[5],
                    clientStreaming = r.groupValues[2].isNotBlank(),
                    serverStreaming = r.groupValues[4].isNotBlank(),
                )
            }.toList()
            ParsedService(name, methods)
        }.toList()

        return ParsedProto(fileName, packageName, messages, services)
    }
}

object LockFile {
    data class Parsed(
        val source: String?,
        val path: String?,
        val repo: String?,
        val ref: String?,
        val module: String?,
        val out: String?,
    )

    fun parse(file: File): Parsed {
        val text = file.readText()
        fun stringField(name: String): String? {
            val re = Regex("\"$name\"\\s*:\\s*\"([^\"]*)\"")
            val nullRe = Regex("\"$name\"\\s*:\\s*null")
            if (nullRe.containsMatchIn(text)) return null
            return re.find(text)?.groupValues?.get(1)
        }
        return Parsed(
            source = stringField("source"),
            path = stringField("path"),
            repo = stringField("repo"),
            ref = stringField("ref"),
            module = stringField("module"),
            out = stringField("out"),
        )
    }
}
