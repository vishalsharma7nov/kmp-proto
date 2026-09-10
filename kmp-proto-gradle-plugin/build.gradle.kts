plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
}

group = "io.github.vishalsharma7nov"
version = libs.versions.project.get()

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(gradleApi())
    compileOnly(localGroovy())
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("kmpProto") {
            id = "io.github.vishalsharma7nov.kmp-proto"
            implementationClass = "io.github.vishalsharma7nov.kmpproto.gradle.KmpProtoPlugin"
            displayName = "KMP Proto"
            description = "Generate dynamic protobuf clients for Kotlin Multiplatform"
        }
    }
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

tasks.test {
    useJUnitPlatform()
}
