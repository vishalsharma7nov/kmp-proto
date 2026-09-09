pluginManagement {
    includeBuild("kmp-proto-gradle-plugin")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Lets jvmToolchain(21) locate / auto-provision an installed JDK 21.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

// Keep in sync with gradle/libs.versions.toml → [versions].jdk and [versions].gradle
val requiredJdkMajor = 21
val gradleSupportsJvmThrough = 24 // Gradle 8.14; Java 25+ needs a newer Gradle

val runningJvmMajor = JavaVersion.current().majorVersion.toInt()
if (runningJvmMajor > gradleSupportsJvmThrough) {
    throw GradleException(
        """
        |Incompatible Gradle JVM: Java $runningJvmMajor.
        |
        |This project uses Gradle 8.14, which runs on Java 8–$gradleSupportsJvmThrough.
        |Compilation requires JDK $requiredJdkMajor (see gradle/libs.versions.toml → jdk).
        |
        |Android Studio / IntelliJ often defaults to the bundled JetBrains Runtime (JBR),
        |which may be newer than Gradle supports — that is not your Homebrew/system JDK.
        |
        |Fix:
        |  Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK
        |  → select JDK $requiredJdkMajor (e.g. Homebrew openjdk@$requiredJdkMajor)
        |
        |CLI:
        |  export JAVA_HOME="\$(/usr/libexec/java_home -v $requiredJdkMajor 2>/dev/null || echo /opt/homebrew/opt/openjdk@$requiredJdkMajor/libexec/openjdk.jdk/Contents/Home)"
        |  ./gradlew <task>
        |
        |If JDK $requiredJdkMajor is not installed: brew install openjdk@$requiredJdkMajor
        """.trimMargin(),
    )
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kmp-proto-monorepo"

include(":kmp-proto")
include(":example")
include(":examples-jvm")

project(":examples-jvm").projectDir = file("examples/jvm")
