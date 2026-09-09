# Errors

All failures surface as `ProtoClientError`.

| Code | When |
|------|------|
| `INVALID_CONFIG` | Missing/invalid `baseUrl`, bad transport |
| `INVALID_ARGUMENT` | Encode / local validation failed |
| `TIMEOUT` | Exceeded `timeoutMs` |
| `NETWORK` | DNS, offline, connection reset |
| `HTTP` | Non-success HTTP |
| `RPC` | Connect/gRPC status in body/headers |
| `DECODE` | Response bytes invalid for expected message |
| `PAYLOAD_TOO_LARGE` | Response exceeds `maxResponseBytes` |
| `ABORTED` | Coroutine cancelled / aborted |
| `INTERNAL` | Unexpected library failure (includes `cause`) |
| `UNSUPPORTED` | Feature not available (e.g. native gRPC missing) |

## Helpers

```kotlin
if (isProtoClientError(e)) {
    val code = getErrorCode(e)
    if (e is ProtoClientError && e.retryable) {
        // safe to retry
    }
}
```

`onError` in config is for logging only — it does **not** replace throw behavior.
