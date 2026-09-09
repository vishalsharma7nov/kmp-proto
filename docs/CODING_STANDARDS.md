# Coding standards

Enforce in CI and local builds. Full contributor workflow: [CONTRIBUTING.md](../CONTRIBUTING.md).

## Quality gates

1. `./gradlew :kmp-proto:check` — compile + unit/integration tests
2. Kotlin `explicitApi()` on the library module
3. Failures must use `ProtoClientError` (never swallow or replace with silent defaults)
4. Validate `ProtoClientConfig` before RPCs (`baseUrl`, bounds on timeout / payload size)
5. Bound response sizes via `maxResponseBytes`
6. Do not hand-edit `generated/` outputs — run the Gradle plugin

## Public API

- Prefer additive changes; follow [VERSIONING.md](./VERSIONING.md)
- Document new config fields in [CONFIG.md](./CONFIG.md) and error codes in [ERRORS.md](./ERRORS.md)
- Update [CHANGELOG.md](../CHANGELOG.md) `[Unreleased]` for user-visible changes
- Regenerate Dokka when signatures change: `./gradlew :kmp-proto:dokkaHtml`

## Style

- Prefer small functions and explicit null / error handling
- Keep app-specific config **out** of published library sources
- Match existing package layout under `io.github.vishalsharma7nov.kmpproto`

## Local commands

```bash
./gradlew :kmp-proto:jvmTest
./gradlew :kmp-proto:check
./gradlew :kmp-proto:dokkaHtml
./gradlew :examples-jvm:run
```
