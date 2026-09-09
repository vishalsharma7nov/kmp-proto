# Contributing to kmp-proto

Thanks for contributing. This document is the source of truth for how we accept changes.

## Code of conduct

Participation is governed by [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md).

## Before you start

1. Search [existing issues](https://github.com/vishalsharma7nov/kmp-proto/issues) for duplicates.
2. For larger changes, open an issue first so we can agree on scope.
3. Read [docs/SUPPORT_MATRIX.md](./docs/SUPPORT_MATRIX.md) and [docs/VERSIONING.md](./docs/VERSIONING.md).

## Development setup

**Prerequisites**

| Tool | Version |
|------|---------|
| JDK | **21** (see `jdk` in `gradle/libs.versions.toml`) |
| Android SDK | as in version catalog (`compileSdk` / `minSdk`) |
| Xcode | latest stable (iOS samples only) |

```bash
git clone https://github.com/vishalsharma7nov/kmp-proto.git
cd kmp-proto

export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || echo /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home)"

./gradlew :kmp-proto-gradle-plugin:build :kmp-proto:jvmTest :examples-jvm:run
```

Android Studio / IntelliJ: set **Gradle JDK** to **21** (not bundled JBR 25). See README.

## Branching and commits

- Branch from `main` / `master`: `feat/…`, `fix/…`, `docs/…`, `chore/…`
- Prefer small, reviewable PRs
- Commit messages: imperative mood, explain **why** (e.g. `fix: reject empty baseUrl before transport`)
- Do not commit secrets, `local.properties`, or machine-specific JDK paths

## Coding standards

Follow [docs/CODING_STANDARDS.md](./docs/CODING_STANDARDS.md):

- Library module uses `explicitApi()`
- Failures must surface as `ProtoClientError` (do not swallow)
- Keep app config outside published artifacts (`kmp-proto.config.kt` in the app)
- Do not hand-edit files under `generated/` — regenerate via the Gradle plugin

## Tests

```bash
./gradlew :kmp-proto:check
./gradlew :kmp-proto:jvmTest
```

Add or update tests for behavior changes. Prefer JVM tests for transport/codec logic.

## Documentation

If you change public API, config fields, error codes, or generate flags:

- Update the matching doc under `docs/`
- Update [CHANGELOG.md](./CHANGELOG.md) under `## [Unreleased]`
- Regenerate API docs when public signatures change: `./gradlew :kmp-proto:dokkaHtml`

## Pull requests

Use the PR template. Every PR should include:

- Summary of **why**
- Test plan (commands you ran)
- Notes on breaking changes (if any)

CI must be green before merge.

## Security

Do not open public issues for undisclosed vulnerabilities. Follow [SECURITY.md](./SECURITY.md).

## License

By contributing, you agree that your contributions are licensed under the MIT License (see [LICENSE](./LICENSE)).
