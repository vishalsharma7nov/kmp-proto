# Getting started

End-to-end path from empty app to a working unary call.

## Prerequisites

Confirm your environment against [SUPPORT_MATRIX.md](./SUPPORT_MATRIX.md):

| Requirement | Minimum |
|-------------|---------|
| JDK | **21** |
| Android `minSdk` (if Android) | **24** |
| Kotlin / AGP | Match `gradle/libs.versions.toml` when building from source |

Install JDK 21 if needed (`brew install openjdk@21` on macOS).  
In Android Studio, set **Gradle JDK = 21** (not JetBrains Runtime 25).

## 1. Add the Maven repository (GitHub Packages)

In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/vishalsharma7nov/kmp-proto")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

Create a GitHub token with `read:packages` and set `gpr.user` / `gpr.key` in
`~/.gradle/gradle.properties`, or export `GITHUB_ACTOR` / `GITHUB_TOKEN`.

## 2. Add the dependency

In your shared / app module:

```kotlin
implementation("io.github.vishalsharma7nov:kmp-proto:0.1.0")
```

## 3. Create app config (outside the library)

Create `kmp-proto.config.kt` **in your app**:

```kotlin
import io.github.vishalsharma7nov.kmpproto.ProtoClientConfig
import io.github.vishalsharma7nov.kmpproto.ProtoSource

val config = ProtoClientConfig(
    baseUrl = "https://api.example.com",
    protoSource = ProtoSource.local("protos"),
    getHeaders = { mapOf("Authorization" to "Bearer $token") },
)
```

Full field reference: [CONFIG.md](./CONFIG.md).  
Template: [`examples/kmp-proto.config.example.kt`](../examples/kmp-proto.config.example.kt).

## 4. Call a method

Prefer the generated helper when you run the Gradle plugin on your protos:

```kotlin
import io.github.vishalsharma7nov.kmpproto.generated.createApi
import io.github.vishalsharma7nov.kmpproto.errors.isProtoClientError

val api = createApi(config)

try {
    val user = api.userService.getUser(mapOf("id" to "1"))
    println(user)
} catch (e: Throwable) {
    if (isProtoClientError(e)) {
        // e.code, e.retryable, e.status — see ERRORS.md
    } else {
        throw e
    }
}
```

Dynamic string-based calls also work via `createClient` + `call` (see root README).

## 5. Generate from your own `.proto` files

```kotlin
plugins {
    id("io.github.vishalsharma7nov.kmp-proto") version "0.1.0"
}
```

```bash
./gradlew kmpProtoGenerate \
  -PkmpProto.out=./src/commonMain/kotlin/generated
```

Pass `-PkmpProto.protoPath=./protos` (or `ProtoSource.local("protos")` on config) to pin a folder. Omit both to fetch `.proto` files from the project directory. GitHub/Buf: `ProtoSource.github` / `ProtoSource.buf`, or `-PkmpProto.from=github` / `-PkmpProto.from=buf`.

Your protos must declare `service` / `rpc` blocks. Details: [GENERATE.md](./GENERATE.md).

## 6. Verify

| Check | Command / action |
|-------|------------------|
| Library tests (from this repo) | `./gradlew :kmp-proto:jvmTest` |
| JVM demo with mock server | `./gradlew :examples-jvm:run` |
| Android example | See [`example/README.md`](../example/README.md) |
| API HTML | `./gradlew :kmp-proto:dokkaHtml` → [API.md](./API.md) |

## Next steps

- [ARCHITECTURE.md](./ARCHITECTURE.md) — how encode → transport → decode works  
- [ERRORS.md](./ERRORS.md) — structured failures  
- [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) — auth, empty method map, native gRPC  
- [SUPPORT_MATRIX.md](./SUPPORT_MATRIX.md) — platforms and versions  
