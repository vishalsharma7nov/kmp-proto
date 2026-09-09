# Documentation

Industrial-style docs for **kmp-proto** — a Kotlin Multiplatform protobuf client.

## Start here

| Audience | Read |
|----------|------|
| New consumer | [GETTING_STARTED.md](./GETTING_STARTED.md) → root [README.md](../README.md) |
| Integrating config / errors | [CONFIG.md](./CONFIG.md), [ERRORS.md](./ERRORS.md) |
| Generating clients | [GENERATE.md](./GENERATE.md) |
| Understanding design | [ARCHITECTURE.md](./ARCHITECTURE.md) |
| Platform / version fit | [SUPPORT_MATRIX.md](./SUPPORT_MATRIX.md), [VERSIONING.md](./VERSIONING.md) |
| API reference (Dokka) | [API.md](./API.md) |
| Stuck | [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) |

## Full index

| Doc | Purpose |
|-----|---------|
| [GETTING_STARTED.md](./GETTING_STARTED.md) | Prerequisites, install, first call, verify |
| [ARCHITECTURE.md](./ARCHITECTURE.md) | Layers and request lifecycle |
| [FILE_GUIDE.md](./FILE_GUIDE.md) | Which file is for which purpose |
| [CONFIG.md](./CONFIG.md) | `ProtoClientConfig` fields |
| [GENERATE.md](./GENERATE.md) | Local / GitHub / Buf generation |
| [ERRORS.md](./ERRORS.md) | `ProtoClientError` codes and helpers |
| [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) | Common failures and fixes |
| [SUPPORT_MATRIX.md](./SUPPORT_MATRIX.md) | Tested JDK, Android, KMP, transports |
| [VERSIONING.md](./VERSIONING.md) | SemVer and breaking-change policy |
| [API.md](./API.md) | How to generate / browse Dokka HTML |
| [GLOSSARY.md](./GLOSSARY.md) | Terms |
| [BUNDLE_SIZE.md](./BUNDLE_SIZE.md) | Artifact size notes |
| [CODING_STANDARDS.md](./CODING_STANDARDS.md) | Quality gates for contributors |

## Project governance (repo root)

| File | Purpose |
|------|---------|
| [CONTRIBUTING.md](../CONTRIBUTING.md) | How to contribute |
| [CODE_OF_CONDUCT.md](../CODE_OF_CONDUCT.md) | Community standards |
| [SECURITY.md](../SECURITY.md) | Vulnerability reporting |
| [CHANGELOG.md](../CHANGELOG.md) | Keep a Changelog |
| [LICENSE](../LICENSE) | MIT |

## Examples

- Compose Multiplatform: [`example/README.md`](../example/README.md)
- JVM mock server: [`examples/jvm`](../examples/jvm)
- Config template: [`examples/kmp-proto.config.example.kt`](../examples/kmp-proto.config.example.kt)
