# kmp-proto

[![CI](https://github.com/vishalsharma7nov/kmp-proto/actions/workflows/ci.yml/badge.svg)](https://github.com/vishalsharma7nov/kmp-proto/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](./LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-purple.svg)](https://kotlinlang.org/)
[![Platforms](https://img.shields.io/badge/platforms-Android%20%7C%20iOS%20%7C%20JVM-brightgreen.svg)](./docs/SUPPORT_MATRIX.md)

**Kotlin Multiplatform client for protobuf APIs.**

Point it at your `.proto` files (GitHub, local folder, or Buf), generate client methods, and call your server:

```kotlin
val user = api.userService.getUser(mapOf("id" to "1"))
```

| | |
|--|--|
| **Maven** | `io.github.vishalsharma7nov:kmp-proto` |
| **Plugin** | `io.github.vishalsharma7nov.kmp-proto` |
| **Docs** | [docs/README.md](./docs/README.md) |
| **Security** | [SECURITY.md](./SECURITY.md) |
| **Contributing** | [CONTRIBUTING.md](./CONTRIBUTING.md) |

---

## Features

- Dynamic client from proto `service` / `rpc` definitions
- Generated method map + message codecs + `createApi`
- App config outside the package (`kmp-proto.config.kt`)
- Transports: **HTTP + protobuf**, **Connect**, **native gRPC** (expect/actual bridge)
- Interceptors (auth, logging), offline queue, path presets, version headers
- Optional validation wrappers and coroutine `Flow` helpers
- Gradle plugin: `generate` / `sync` / `watch` (local, GitHub, or Buf)
- JVM mock server; Compose Multiplatform + JVM examples

---

## Requirements

See the full [support matrix](./docs/SUPPORT_MATRIX.md).

| | Version |
|--|---------|
| JDK (Gradle) | **21** |
| Android `minSdk` | **24** |
| Kotlin / AGP | per [`gradle/libs.versions.toml`](./gradle/libs.versions.toml) |

> Android Studio: set **Gradle JDK = 21**. Bundled JBR **25** is incompatible with Gradle 8.14.

---

## Quick start

### 1. Install (GitHub Packages)

```kotlin
maven {
    url = uri("https://maven.pkg.github.com/vishalsharma7nov/kmp-proto")
    credentials {
        username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
        password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
    }
}
```

```kotlin
implementation("io.github.vishalsharma7nov:kmp-proto:0.1.0")
```

### 2. App config (not inside the library)

```kotlin
import io.github.vishalsharma7nov.kmpproto.ProtoClientConfig

val config = ProtoClientConfig(
    baseUrl = "https://api.example.com",
    getHeaders = { mapOf("Authorization" to "Bearer ${getToken()}") },
)
```

Template: [`examples/kmp-proto.config.example.kt`](./examples/kmp-proto.config.example.kt).

### 3. Call your API

```kotlin
import io.github.vishalsharma7nov.kmpproto.generated.createApi
import io.github.vishalsharma7nov.kmpproto.errors.isProtoClientError

val api = createApi(config)

try {
    val user = api.userService.getUser(mapOf("id" to "1"))
    println(user)
} catch (e: Throwable) {
    if (isProtoClientError(e)) println(e) else throw e
}
```

Dynamic style (`createClient` + `call`) is also supported — see [docs/GETTING_STARTED.md](./docs/GETTING_STARTED.md).

---

## Generate from your own protos

```kotlin
plugins {
    id("io.github.vishalsharma7nov.kmp-proto") version "0.1.0"
}
```

```bash
./gradlew kmpProtoGenerate -PkmpProto.from=local -PkmpProto.protoPath=./protos -PkmpProto.out=./src/commonMain/kotlin/generated
./gradlew kmpProtoWatch
./gradlew kmpProtoGenerate -PkmpProto.from=github -PkmpProto.repo=https://github.com/you/protos.git -PkmpProto.ref=v1.0.0
./gradlew kmpProtoGenerate -PkmpProto.from=buf -PkmpProto.module=buf.build/acme/petapis -PkmpProto.ref=1.0.0
```

`.proto` files must include `service` and `rpc` blocks. Details: [docs/GENERATE.md](./docs/GENERATE.md).

---

## Documentation

| Doc | Description |
|-----|-------------|
| [docs/README.md](./docs/README.md) | **Docs index** |
| [Getting started](./docs/GETTING_STARTED.md) | Prerequisites → first call |
| [Support matrix](./docs/SUPPORT_MATRIX.md) | Platforms & versions |
| [Architecture](./docs/ARCHITECTURE.md) | Design & lifecycle |
| [Config](./docs/CONFIG.md) / [Errors](./docs/ERRORS.md) | Integration reference |
| [API (Dokka)](./docs/API.md) | Generate HTML API docs |
| [Versioning](./docs/VERSIONING.md) | SemVer policy |
| [Troubleshooting](./docs/TROUBLESHOOTING.md) | Common failures |

---

## Examples

| Project | Description |
|---------|-------------|
| [`example/`](./example) | Compose Multiplatform (Android + iOS) |
| [`examples/jvm`](./examples/jvm) | JVM + mock protobuf server |

```bash
./gradlew :examples-jvm:run
```

Android Studio: sync with **JDK 21**, run configuration **`example`** — see [`example/README.md`](./example/README.md).

---

## Version catalog

Edit **[`gradle/libs.versions.toml`](./gradle/libs.versions.toml)** for JDK, Android SDK, Kotlin, AGP, and library versions. CI reads `jdk` from that file.

---

## Contributing

- [CONTRIBUTING.md](./CONTRIBUTING.md) — setup, tests, PR expectations  
- [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) — community standards  
- [SECURITY.md](./SECURITY.md) — private vulnerability reporting  
- [CHANGELOG.md](./CHANGELOG.md) — Keep a Changelog  

```bash
./gradlew :kmp-proto:jvmTest :kmp-proto:dokkaHtml
```

---

## License

[MIT](./LICENSE)
