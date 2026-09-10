package io.github.vishalsharma7nov.kmpproto.gradle

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LocalProtoDiscoveryTest {
    private fun tempDir(): File = createTempDirectory("kmp-proto-").toFile()

    @Test
    fun usesExplicitPath() {
        val tmp = tempDir()
        try {
            val protos = File(tmp, "custom").apply { mkdirs() }
            File(protos, "user.proto").writeText("syntax = \"proto3\";")
            val found = LocalProtoDiscovery.discover(tmp, tmp, "custom")
            assertEquals(protos.canonicalFile, found.canonicalFile)
        } finally {
            tmp.deleteRecursively()
        }
    }

    @Test
    fun findsProtosDirectory() {
        val tmp = tempDir()
        try {
            val protos = File(tmp, "protos").apply { mkdirs() }
            File(protos, "user.proto").writeText("syntax = \"proto3\";")
            val found = LocalProtoDiscovery.discover(tmp, tmp, null)
            assertEquals(protos.canonicalFile, found.canonicalFile)
        } finally {
            tmp.deleteRecursively()
        }
    }

    @Test
    fun findsVendorProtosWhenProtosMissing() {
        val tmp = tempDir()
        try {
            val vendor = File(tmp, "vendor/protos").apply { mkdirs() }
            File(vendor, "user.proto").writeText("syntax = \"proto3\";")
            val found = LocalProtoDiscovery.discover(tmp, tmp, null)
            assertEquals(vendor.canonicalFile, found.canonicalFile)
        } finally {
            tmp.deleteRecursively()
        }
    }

    @Test
    fun prefersProjectDirOverRoot() {
        val tmp = tempDir()
        try {
            val project = File(tmp, "app").apply { mkdirs() }
            val projectProtos = File(project, "protos").apply { mkdirs() }
            File(projectProtos, "app.proto").writeText("syntax = \"proto3\";")
            val rootProtos = File(tmp, "protos").apply { mkdirs() }
            File(rootProtos, "root.proto").writeText("syntax = \"proto3\";")
            val found = LocalProtoDiscovery.discover(project, tmp, null)
            assertEquals(projectProtos.canonicalFile, found.canonicalFile)
        } finally {
            tmp.deleteRecursively()
        }
    }

    @Test
    fun skipsNodeModules() {
        val tmp = tempDir()
        try {
            val nested = File(tmp, "node_modules/other").apply { mkdirs() }
            File(nested, "skip.proto").writeText("syntax = \"proto3\";")
            File(tmp, "app.proto").writeText("syntax = \"proto3\";")
            val found = LocalProtoDiscovery.discover(tmp, tmp, null)
            assertEquals(tmp.canonicalFile, found.canonicalFile)
            assertTrue(LocalProtoDiscovery.protoFilesIn(tmp).none { it.name == "skip.proto" })
        } finally {
            tmp.deleteRecursively()
        }
    }

    @Test
    fun throwsWhenNothingFound() {
        val tmp = tempDir()
        try {
            val error = assertFailsWith<IllegalStateException> {
                LocalProtoDiscovery.discover(tmp, tmp, null)
            }
            assertTrue(error.message!!.contains("No .proto files found"))
        } finally {
            tmp.deleteRecursively()
        }
    }
}
