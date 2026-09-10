package io.github.vishalsharma7nov.kmpproto.gradle

import java.io.File

internal object LocalProtoDiscovery {
    val candidateDirs: List<String> = listOf("protos", "proto", "vendor/protos")
    val skipDirNames: Set<String> = setOf(
        "build",
        "node_modules",
        "ios",
        "android",
        ".dart_tool",
    )

    fun protoFilesIn(dir: File): List<File> {
        if (!dir.exists()) return emptyList()
        val files = mutableListOf<File>()
        fun walk(current: File) {
            val children = current.listFiles() ?: return
            for (child in children) {
                if (child.isDirectory) {
                    if (child.name.startsWith(".") || child.name in skipDirNames) continue
                    walk(child)
                } else if (child.isFile && child.extension == "proto") {
                    files.add(child)
                }
            }
        }
        walk(dir)
        return files
    }

    fun resolveExisting(path: String, projectDir: File, rootDir: File): File {
        val candidate = File(path)
        val resolved = when {
            candidate.isAbsolute -> candidate
            File(projectDir, path).exists() -> File(projectDir, path)
            else -> File(rootDir, path)
        }
        if (!resolved.exists()) {
            throw IllegalStateException("Proto path does not exist: ${resolved.absolutePath}")
        }
        return resolved
    }

    fun discover(projectDir: File, rootDir: File, path: String?): File {
        if (!path.isNullOrBlank()) {
            return resolveExisting(path, projectDir, rootDir)
        }
        for (rel in candidateDirs) {
            val fromProject = File(projectDir, rel)
            if (protoFilesIn(fromProject).isNotEmpty()) return fromProject
            val fromRoot = File(rootDir, rel)
            if (protoFilesIn(fromRoot).isNotEmpty()) return fromRoot
        }
        if (protoFilesIn(projectDir).isNotEmpty()) return projectDir
        if (protoFilesIn(rootDir).isNotEmpty()) return rootDir
        throw IllegalStateException(
            "No .proto files found in the project directory. " +
                "Set ProtoClientConfig.protoSource (e.g. ProtoSource.local(\"protos\")) " +
                "or pass -PkmpProto.protoPath.",
        )
    }
}
