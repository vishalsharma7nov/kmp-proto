# API reference

Public API documentation is generated with [Dokka](https://kotlinlang.org/docs/dokka-introduction.html).

## Generate locally

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || echo /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home)"
./gradlew :kmp-proto:dokkaHtml
```

HTML output (open in a browser):

```text
kmp-proto/build/dokka/html/index.html
```

## What is documented

- Public types in `io.github.vishalsharma7nov.kmpproto` and subpackages
- Config, transport kinds, errors, interceptors, path presets
- Generated sample APIs under `…generated` (from `vendor/protos` — regenerate for your own protos)

`internal` APIs are excluded via Kotlin visibility + Dokka defaults.

## Publishing docs (optional)

Maintainers can attach Dokka HTML to a GitHub Release or host via GitHub Pages. CI currently focuses on compile/test; run Dokka before tagging a release (see [VERSIONING.md](./VERSIONING.md)).

## Related

- [CONFIG.md](./CONFIG.md) — config field table
- [ERRORS.md](./ERRORS.md) — error code table
- [VERSIONING.md](./VERSIONING.md) — what counts as public API
