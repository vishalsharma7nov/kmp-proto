# Config

`ProtoClientConfig` lives in **your app**, not inside the published library.

| Field | Type | Default | Purpose |
|-------|------|---------|---------|
| `baseUrl` | `String` | required | API origin (`https://api.example.com`) |
| `protoSource` | `ProtoSource?` | null | Where generate loads `.proto` files (local path, GitHub, or Buf). When omitted, generate fetches them from the project directory. Not used by `createApi` at request time. |
| `getHeaders` | `suspend () -> Map` | null | Auth and other headers |
| `timeoutMs` | `Long` | 30000 | Request timeout |
| `maxResponseBytes` | `Int` | 2 MiB | Reject oversized responses |
| `transport` | `TransportKind` | `Http` | `Http`, `Connect`, or `NativeGrpc` |
| `httpClient` | `HttpClient?` | platform default | Override Ktor client (tests) |
| `onError` | `(ProtoClientError) -> Unit` | null | Logging hook (still throws) |
| `pathForMethod` | `(MethodMapEntry) -> String` | from preset | Custom URL path builder |
| `pathPreset` | `PathTemplatePreset` | `Connect` | `Connect`, `Envoy`, `GrpcGateway`, `Restish`, … |
| `interceptors` | `List<Interceptor>` | empty | Auth, logging, etc. |
| `retryOnUnauthorized` | `Boolean` | true | Single 401 retry when no auth interceptor |

Example: [`examples/kmp-proto.config.example.kt`](../examples/kmp-proto.config.example.kt)
