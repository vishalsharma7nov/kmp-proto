# Troubleshooting

Also see [SUPPORT_MATRIX.md](./SUPPORT_MATRIX.md) for tested versions.

## Android Studio: Incompatible Gradle JVM / Java 25

**Symptom:** Sync fails with Gradle 8.14 vs JVM 25.

**Cause:** IDE **Gradle JDK** is set to bundled JetBrains Runtime 25.

**Fix:** Settings → Build Tools → Gradle → **Gradle JDK = 21** (Homebrew `openjdk@21` or Temurin 21).  
Do not use **Use Embedded JDK** when it is Java 25.

## GitHub Packages auth failed

Add a token with `read:packages` and configure the Maven repository credentials
(`gpr.user` / `gpr.key` or `GITHUB_ACTOR` / `GITHUB_TOKEN`). Never commit tokens.

## Wrong baseUrl

`baseUrl` must be an absolute URL starting with `http://` or `https://`. Trailing slashes are stripped.

## Empty method map

Run generate/sync against `.proto` files that contain `service` and `rpc` blocks.
Confirm `kmpProto.out` / package paths match your source sets.

## Streaming fails on Http transport

Streaming requires `TransportKind.Connect` or `NativeGrpc`. Plain HTTP+protobuf is unary-only.

## Native gRPC UNSUPPORTED

Install a `NativeGrpcBridge` via `installedNativeGrpcBridge = ...` or use HTTP/Connect.

## iOS / Android engine issues

Each target uses a Ktor engine (OkHttp / Darwin / CIO). Pass `httpClient` in config for custom engines
(tests often use `ktor-client-mock`).

## Buf export failed

Install the [Buf CLI](https://buf.build/docs/installation) or switch to local/GitHub sources.

## Example app shows no Run configuration

1. Open the **repo root** in Android Studio (not only `example/`)
2. Sync with Gradle JDK 21
3. Select run config **`example`** (see [`.run/`](../.run/) and [`example/README.md`](../example/README.md))
