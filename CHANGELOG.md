# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)
(see [docs/VERSIONING.md](./docs/VERSIONING.md)).

## [Unreleased]

### Added

- Industrial documentation pack: contributing, security, code of conduct, support matrix, versioning, Dokka API guide, GitHub issue/PR templates
- Compose example mock mode uses generated protobuf codecs (`GetUserRequest` / `User`) via Ktor `MockEngine`

### Changed

- Example Android Studio run configurations under `.run/`
- Gradle JDK compatibility check in `settings.gradle.kts` (fail fast on Java 25+)

## [0.1.0] - 2026-09-09

### Added

- Kotlin Multiplatform dynamic protobuf client (`io.github.vishalsharma7nov:kmp-proto`)
- Gradle plugin: `kmpProtoGenerate` / `syncProtos` / `kmpProtoWatch` (local, GitHub, Buf)
- HTTP + protobuf unary transport (Ktor), Connect, native gRPC bridge hooks
- Structured `ProtoClientError`, interceptors, auth, logging, offline queue, path presets, version headers
- Optional validation wrappers and coroutine Flow helpers
- JVM mock server, Compose Multiplatform example, JVM sample
- Initial docs, CI, sync, and publish workflows

[Unreleased]: https://github.com/vishalsharma7nov/kmp-proto/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/vishalsharma7nov/kmp-proto/releases/tag/v0.1.0
