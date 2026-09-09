# Versioning and compatibility

`kmp-proto` follows [Semantic Versioning 2.0.0](https://semver.org/).

Given a version `MAJOR.MINOR.PATCH`:

| Change | Bump |
|--------|------|
| Bug fix, docs, internal refactor with no API change | **PATCH** |
| New backward-compatible API or feature | **MINOR** |
| Breaking public API or behavior change | **MAJOR** |

## Pre-1.0 policy (`0.y.z`)

While the major version is `0`:

- The public API is not frozen
- Breaking changes may appear in **MINOR** releases (`0.1 → 0.2`) when necessary
- We still document breaking changes clearly in [CHANGELOG.md](../CHANGELOG.md)
- Prefer deprecation warnings for at least one minor when practical

After `1.0.0`, breaking changes require a **MAJOR** bump.

## What counts as public API

Breaking if removed or signature-changed without a major (or pre-1.0 minor) bump:

- Types/functions in published Maven artifacts under `io.github.vishalsharma7nov.kmpproto` (except `…generated` when regenerating from *your* protos)
- Gradle plugin id `io.github.vishalsharma7nov.kmp-proto` extension DSL and documented task names/properties
- Documented error codes in [ERRORS.md](./ERRORS.md)
- Documented config fields in [CONFIG.md](./CONFIG.md)

Not covered by SemVer guarantees:

- Files under `example/`, `examples/`, `vendor/protos/`
- Generated sample codecs in this repo’s `generated/` folder (they track sample protos)
- Undocumented internal/`internal` Kotlin APIs

## Dependency alignment

Toolchain and library versions live in [`gradle/libs.versions.toml`](../gradle/libs.versions.toml).
See [SUPPORT_MATRIX.md](./SUPPORT_MATRIX.md) for tested platforms.

## Release checklist (maintainers)

1. Update `project` in `gradle/libs.versions.toml`
2. Move `[Unreleased]` notes in `CHANGELOG.md` into a dated version section
3. `./gradlew :kmp-proto:check :kmp-proto:dokkaHtml`
4. Tag `vX.Y.Z` and publish via the release workflow
5. Verify GitHub Packages artifact coordinates
