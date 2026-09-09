# File guide — which file is for which purpose

## In your KMP app (outside the package)

| Path | Purpose |
|------|---------|
| `kmp-proto.config.kt` | Your settings: API URL, auth headers, timeout, transport. **You create this.** |
| `src/commonMain/kotlin/generated/` | Output of `kmpProtoGenerate` for your own protos. **Do not edit by hand.** |
| Gradle Maven credentials | Only needed to install from GitHub Packages. |

## In this library repository

| Path | Purpose |
|------|---------|
| `kmp-proto/src/commonMain/.../KmpProto.kt` | Public `createClient` entry |
| `kmp-proto/src/commonMain/.../Types.kt` | Config, method map, codec types |
| `kmp-proto/src/commonMain/.../transport/` | Sends/receives data (Ktor) |
| `kmp-proto/src/commonMain/.../errors/` | `ProtoClientError` and helpers |
| `kmp-proto/src/commonMain/.../client/` | Builds `api.service.method` from the method map |
| `kmp-proto/src/commonMain/.../streaming/` | Streaming RPC helpers |
| `kmp-proto/src/commonMain/.../nativegrpc/` | Native gRPC bridge hooks |
| `kmp-proto/src/commonMain/.../codecs/` | Protobuf wire helpers |
| `kmp-proto/src/commonMain/.../interceptors/` | Unary interceptor pipeline |
| `kmp-proto/src/commonMain/.../auth/` | Bearer token + refresh interceptor |
| `kmp-proto/src/commonMain/.../logging/` | Request/response logging |
| `kmp-proto/src/commonMain/.../offline/` | Offline mutation queue |
| `kmp-proto/src/commonMain/.../path/` | URL path presets |
| `kmp-proto/src/commonMain/.../connect/` | Connect envelopes + error mapping |
| `kmp-proto/src/commonMain/.../validation/` | Optional runtime validation |
| `kmp-proto/src/commonMain/.../flowhelpers/` | Optional coroutine Flow helpers |
| `kmp-proto/src/commonMain/.../generated/` | Auto-generated — **do not edit by hand** |
| `kmp-proto/src/jvmMain/.../mockserver/` | JVM mock server |
| `kmp-proto-gradle-plugin/` | CLI/tasks: generate, sync, watch |
| `vendor/protos/` | Sample `.proto` files |
| `protos.lock.json` | Where sync reads protos from |
| `scripts/sync-protos.sh` | Maintainer shortcut |
| `example/` | Compose Multiplatform sample (Android + iOS) |
| `iosApp/` | Xcode host app for the Compose iOS example |
| `examples/jvm/` | JVM sample |
| `examples/kmp-proto.config.example.kt` | Copy-paste config template |
| `docs/` | Documentation index — start at [`docs/README.md`](./README.md) |
| `CONTRIBUTING.md` / `SECURITY.md` / `CODE_OF_CONDUCT.md` | Community & security process |
| `CHANGELOG.md` | Keep a Changelog |
| `gradle/libs.versions.toml` | **Single config** for JDK, Android SDK, Kotlin, AGP, and library versions |
| `.github/workflows/` | CI, sync PRs, publish |
| `.github/ISSUE_TEMPLATE/` / `PULL_REQUEST_TEMPLATE.md` | Issue and PR templates |
| `.run/` | Shared Android Studio run configurations |

## What you should not edit

- Anything under `generated/` after a generate — it will be overwritten
- Files inside the published Maven artifact — put config in your app instead
