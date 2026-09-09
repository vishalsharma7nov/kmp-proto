# Bundle / artifact size

`kmp-proto` keeps the default path lean:

- Core: Ktor client + Okio + coroutines
- Generated codecs are only as large as your `.proto` surface
- Mock server is **JVM-only** (not on mobile artifacts)
- Native gRPC is opt-in via a bridge — unused apps stay on HTTP/Connect

Tips:

- Prefer HTTP transport if you do not need streams
- Generate only the protos your app calls
- Avoid shipping unused interceptors/helpers in app modules (tree-shake by not referencing them)
