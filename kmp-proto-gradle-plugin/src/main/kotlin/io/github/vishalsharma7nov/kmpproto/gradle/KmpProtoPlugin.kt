package io.github.vishalsharma7nov.kmpproto.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask
import java.io.File

open class KmpProtoExtension {
    var from: String = "local"
    /** Local proto folder, or a subdirectory inside a GitHub clone. Null = discover in the project. */
    var protoPath: String? = null
    var repo: String? = null
    var ref: String? = null
    var module: String? = null
    var out: String = "src/commonMain/kotlin/generated"
    var lockFile: String = "protos.lock.json"
    var packageName: String = "generated"
}

abstract class KmpProtoGenerateTask : DefaultTask() {
    @get:Input
    var from: String = "local"

    @get:Input
    @get:Optional
    var protoPath: String? = null

    @get:Input
    @get:Optional
    var repo: String? = null

    @get:Input
    @get:Optional
    var ref: String? = null

    @get:Input
    @get:Optional
    var module: String? = null

    @get:Input
    var out: String = "src/commonMain/kotlin/generated"

    @get:Input
    var packageName: String = "generated"

    @TaskAction
    fun generate() {
        val projectDir = project.projectDir
        val rootDir = project.rootProject.projectDir
        val protoDir = resolveProtoDir(rootDir, projectDir)
        val outDir = resolveOutDir(rootDir, projectDir, out)
        outDir.mkdirs()
        val files = protoDir.walkTopDown().filter { it.isFile && it.extension == "proto" }.toList()
        if (files.isEmpty()) {
            throw IllegalStateException(
                "No .proto files found under ${protoDir.absolutePath}. " +
                    "Set ProtoClientConfig.protoSource or pass -PkmpProto.protoPath.",
            )
        }
        val parsed = files.map { ProtoParser.parse(it.readText(), it.name) }
        CodeGenerator.writeAll(outDir, packageName, parsed)
        logger.lifecycle(
            "kmp-proto: generated ${parsed.sumOf { p -> p.services.sumOf { s -> s.methods.size } }} methods into ${outDir.absolutePath}",
        )
    }

    private fun resolveOutDir(rootDir: File, projectDir: File, out: String): File {
        val cleaned = out.removePrefix("./")
        if (File(out).isAbsolute) return File(out)
        // Lock files often use repo-root paths like ./kmp-proto/src/...
        if (cleaned.startsWith("${project.name}/") || cleaned.startsWith("kmp-proto/")) {
            return File(rootDir, cleaned)
        }
        return File(projectDir, cleaned)
    }

    private fun resolveProtoDir(rootDir: File, projectDir: File): File {
        return when (from.lowercase()) {
            "local" -> LocalProtoDiscovery.discover(projectDir, rootDir, protoPath)
            "github" -> {
                val repository = repo ?: throw IllegalStateException("github source requires repo")
                val reference = ref ?: throw IllegalStateException("github source requires ref (tag or commit)")
                val staging = File(project.layout.buildDirectory.asFile.get(), "kmp-proto/github")
                staging.deleteRecursively()
                staging.mkdirs()
                val clone = ProcessBuilder(
                    "git", "clone", "--depth", "1", "--branch", reference, repository, staging.absolutePath,
                ).inheritIO().start()
                if (clone.waitFor() != 0) {
                    staging.deleteRecursively()
                    staging.mkdirs()
                    val c2 = ProcessBuilder("git", "clone", repository, staging.absolutePath).inheritIO().start()
                    if (c2.waitFor() != 0) {
                        throw IllegalStateException("Failed to clone $repository")
                    }
                    val co = ProcessBuilder("git", "-C", staging.absolutePath, "checkout", reference)
                        .inheritIO().start()
                    if (co.waitFor() != 0) {
                        throw IllegalStateException("Failed to checkout $reference")
                    }
                }
                val sub = protoPath
                if (sub.isNullOrBlank()) {
                    staging
                } else {
                    val nested = File(staging, sub)
                    if (!nested.exists()) {
                        throw IllegalStateException("Proto path does not exist in repo: ${nested.absolutePath}")
                    }
                    nested
                }
            }
            "buf" -> {
                val mod = module ?: throw IllegalStateException("buf source requires module")
                val reference = ref ?: "main"
                val staging = File(project.layout.buildDirectory.asFile.get(), "kmp-proto/buf")
                staging.deleteRecursively()
                staging.mkdirs()
                val proc = ProcessBuilder(
                    "buf", "export", "$mod:$reference", "-o", staging.absolutePath,
                ).inheritIO().start()
                if (proc.waitFor() != 0) {
                    throw IllegalStateException(
                        "buf export failed for $mod@$reference — install Buf CLI or use local/github",
                    )
                }
                staging
            }
            else -> throw IllegalStateException("Unknown source: $from (use local, github, or buf)")
        }
    }
}

abstract class KmpProtoSyncTask : KmpProtoGenerateTask()

abstract class KmpProtoWatchTask : DefaultTask() {
    @get:Input
    var from: String = "local"

    @get:Input
    @get:Optional
    var protoPath: String? = null

    @get:Input
    var out: String = "src/commonMain/kotlin/generated"

    @get:Input
    var packageName: String = "generated"

    @TaskAction
    fun watch() {
        val generate = project.tasks.named("kmpProtoGenerate", KmpProtoGenerateTask::class.java).get()
        generate.from = from
        generate.protoPath = protoPath
        generate.out = out
        generate.packageName = packageName
        generate.generate()
        val dir = LocalProtoDiscovery.discover(
            projectDir = project.projectDir,
            rootDir = project.rootProject.projectDir,
            path = protoPath,
        )
        logger.lifecycle("kmp-proto: watching ${dir.absolutePath} (polling every 2s). Ctrl+C to stop.")
        var last = 0L
        while (!Thread.currentThread().isInterrupted) {
            val newest = dir.walkTopDown().filter { it.extension == "proto" }
                .maxOfOrNull { it.lastModified() } ?: 0L
            if (newest > last) {
                last = newest
                generate.generate()
            }
            Thread.sleep(2000)
        }
    }
}

class KmpProtoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("kmpProto", KmpProtoExtension::class.java)

        project.afterEvaluate {
            val lock = File(project.rootProject.projectDir, ext.lockFile)
            if (lock.exists()) {
                val parsed = LockFile.parse(lock)
                if (ext.protoPath == null && parsed.path != null) ext.protoPath = parsed.path
                if (parsed.source != null) ext.from = parsed.source
                if (parsed.repo != null) ext.repo = parsed.repo
                if (parsed.ref != null) ext.ref = parsed.ref
                if (parsed.module != null) ext.module = parsed.module
                if (parsed.out != null) ext.out = parsed.out
            }
        }

        project.tasks.register("kmpProtoGenerate", KmpProtoGenerateTask::class.java) { task ->
            task.group = "kmp-proto"
            task.description = "Generate method map and message codecs from .proto files"
            task.doFirst {
                task.from = project.findProperty("kmpProto.from") as String? ?: ext.from
                task.protoPath = project.findProperty("kmpProto.protoPath") as String? ?: ext.protoPath
                task.repo = project.findProperty("kmpProto.repo") as String? ?: ext.repo
                task.ref = project.findProperty("kmpProto.ref") as String? ?: ext.ref
                task.module = project.findProperty("kmpProto.module") as String? ?: ext.module
                task.out = project.findProperty("kmpProto.out") as String? ?: ext.out
                task.packageName = project.findProperty("kmpProto.packageName") as String? ?: ext.packageName
            }
        }

        project.tasks.register("syncProtos", KmpProtoSyncTask::class.java) { task ->
            task.group = "kmp-proto"
            task.description = "Sync protos from lock file / extension and regenerate"
            task.doFirst {
                task.from = ext.from
                task.protoPath = ext.protoPath
                task.repo = ext.repo
                task.ref = ext.ref
                task.module = ext.module
                task.out = ext.out
                task.packageName = ext.packageName
            }
        }

        project.tasks.register("kmpProtoWatch", KmpProtoWatchTask::class.java) { task ->
            task.group = "kmp-proto"
            task.description = "Watch local .proto files and regenerate"
            task.doFirst {
                task.from = "local"
                task.protoPath = ext.protoPath
                task.out = ext.out
                task.packageName = ext.packageName
            }
        }
    }
}
