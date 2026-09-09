plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    id("io.github.vishalsharma7nov.kmp-proto")
    `maven-publish`
}

group = "io.github.vishalsharma7nov"
version = libs.versions.project.get()

kmpProto {
    from = "local"
    protoPath = "../vendor/protos"
    out = "src/commonMain/kotlin/io/github/vishalsharma7nov/kmpproto/generated"
    packageName = "io.github.vishalsharma7nov.kmpproto.generated"
    lockFile = "protos.lock.json"
}

val jvmTargetVersion = libs.versions.jvmTarget.get()
val javaVersion = JavaVersion.toVersion(jvmTargetVersion)

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jdk.get().toInt())

    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(jvmTargetVersion))
                }
            }
        }
        publishLibraryVariants("release")
    }

    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "KmpProto"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                api(libs.ktor.client.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.okio)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.cio)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
}

android {
    namespace = "io.github.vishalsharma7nov.kmpproto"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("kmp-proto")
            description.set("Kotlin Multiplatform dynamic protobuf client with generate plugin, HTTP/Connect transports, and typed errors")
            url.set("https://github.com/vishalsharma7nov/kmp-proto")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("vishalsharma7nov")
                    name.set("vishalsharma7nov")
                }
            }
            scm {
                url.set("https://github.com/vishalsharma7nov/kmp-proto")
                connection.set("scm:git:git://github.com/vishalsharma7nov/kmp-proto.git")
                developerConnection.set("scm:git:ssh://github.com/vishalsharma7nov/kmp-proto.git")
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/vishalsharma7nov/kmp-proto")
            credentials {
                username = project.findProperty("gpr.user") as String?
                    ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String?
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
