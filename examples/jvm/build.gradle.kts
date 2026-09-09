plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "io.github.vishalsharma7nov"
version = libs.versions.project.get()

dependencies {
    implementation(project(":kmp-proto"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.server.cio)
}

application {
    mainClass.set("io.github.vishalsharma7nov.kmpproto.examples.JvmMainKt")
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}
