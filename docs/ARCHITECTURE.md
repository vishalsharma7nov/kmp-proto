# Architecture

How `kmp-proto` turns `.proto` files into working API calls.

```text
Proto source (local / GitHub / Buf)
        │
        ▼
Gradle plugin (generate / sync / watch)
        │
        ▼
Generated method map + message codecs
        │
        ▼
createClient(config) / createApi(config)
        │
        ▼
Transport (Ktor HTTP / Connect / native gRPC)
        │
        ▼
Your backend
```

## Layers

1. **Proto source** — `.proto` files describing messages and services (`protos.lock.json`).
2. **Code generation** — Gradle plugin writes `MethodMap.kt`, `Messages.kt`, `CreateApi.kt`.
3. **App configuration** — `kmp-proto.config.kt` with `baseUrl`, headers, transport.
4. **Client factory** — builds `api.userService.getUser` from the method map (no hand-written per-RPC networking).
5. **Transport** — encode → POST → decode; map failures to `ProtoClientError`.

## Request lifecycle

1. App calls `api.userService.getUser(mapOf("id" to "1"))`
2. Client looks up method map entry
3. Codec encodes request → `ByteArray`
4. Transport builds URL from `baseUrl` + path preset
5. `getHeaders()` adds Authorization
6. Ktor POST with protobuf body
7. Response bytes checked (status, max size)
8. Codec decodes → `Map`
9. On failure → `ProtoClientError` (never swallowed)

## Security notes

- Prefer HTTPS `baseUrl` in production
- Never log raw auth tokens; use secure storage in the app
- Report vulnerabilities privately — [SECURITY.md](../SECURITY.md)

## Related docs

- [GETTING_STARTED.md](./GETTING_STARTED.md)
- [FILE_GUIDE.md](./FILE_GUIDE.md)
- [CONFIG.md](./CONFIG.md)
- [ERRORS.md](./ERRORS.md)
- [SUPPORT_MATRIX.md](./SUPPORT_MATRIX.md)
- [API.md](./API.md)
