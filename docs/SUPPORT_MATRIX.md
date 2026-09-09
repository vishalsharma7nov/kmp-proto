# Support matrix

Versions below are the **tested** baselines for this repository. Values marked
“catalog” are defined in [`gradle/libs.versions.toml`](../gradle/libs.versions.toml).

## Toolchains

| Component | Supported / tested | Notes |
|-----------|--------------------|-------|
| JDK (Gradle daemon + toolchain) | **21** (catalog `jdk`) | Gradle 8.14 does **not** run on Java 25+ |
| JVM bytecode target | **17** (catalog `jvmTarget`) | Android and JVM consume 17 bytecode |
| Gradle | **8.14** (wrapper) | Use the wrapper; do not mix random Gradle installs |
| Kotlin | **2.1.0** | Align plugins with the catalog |
| Android Gradle Plugin | **8.7.3** | |
| Android `minSdk` | **24** | |
| Android `compileSdk` / `targetSdk` | **35** | |
| Compose Multiplatform (example) | **1.7.1** | Example app only |
| Ktor | **3.0.3** | Client engines per target |

## Kotlin Multiplatform targets

| Target | Library (`:kmp-proto`) | Example |
|--------|------------------------|---------|
| Android | Yes | Yes (`:example`) |
| JVM | Yes | Yes (`:examples-jvm`) |
| iOS (`iosArm64`, `iosSimulatorArm64`, `iosX64`) | Yes | Yes (`iosApp` + Compose) |

## Transports

| Transport | Status | Notes |
|-----------|--------|-------|
| `TransportKind.Http` | Supported | Unary HTTP + protobuf |
| `TransportKind.Connect` | Supported | Connect protocol headers / content types |
| `TransportKind.NativeGrpc` | Bridge only | Requires installing a `NativeGrpcBridge`; otherwise `UNSUPPORTED` |

## Proto sources (Gradle plugin)

| Source | Status |
|--------|--------|
| Local folder | Supported |
| GitHub repo + ref | Supported |
| Buf module + ref | Supported (Buf CLI may be required) |

## CI

GitHub Actions (`.github/workflows/ci.yml`) builds the plugin, runs `:kmp-proto:jvmTest`, and runs the JVM example on **Temurin JDK 21**.

## IDE notes

- Android Studio **Gradle JDK** must be **21** (not embedded JBR 25).
- See [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) for sync failures.

## Unsupported / out of scope

- Running Gradle on JDK 25+ with the current wrapper
- Guaranteeing binary compatibility across Kotlin major versions without a release bump
- Production SLA / commercial support (community project — see [SECURITY.md](../SECURITY.md) for vulnerability process only)
