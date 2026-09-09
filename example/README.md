# Example Compose Multiplatform app

Demo for `io.github.vishalsharma7nov:kmp-proto`.

## Layout

| Path | Role |
|------|------|
| `src/commonMain/` | Shared UI (`App.kt`) |
| `src/androidMain/` | Android entry (`MainActivity`) |
| `src/iosMain/` | iOS Compose entry (`MainViewController`) |
| `../iosApp/` | Xcode host app that embeds the Kotlin framework |

## What this demo teaches

1. Keep `kmp-proto.config` **in the app**
2. Call `createApi(config)` / `createClient(config)`
3. Call a generated method: `api.userService.getUser(...)`
4. Handle errors with `isProtoClientError(e)`

## Run (Android)

### Android Studio

1. Open this repo root in Android Studio (not only the `example/` folder).
2. Set **Gradle JDK** to **21** (Settings → Build Tools → Gradle). Do **not** use JetBrains Runtime 25.
3. **File → Sync Project with Gradle Files** and wait until sync succeeds.
4. Top toolbar run config → select **`example`** (Android App).  
   Fallback: **`example (installDebug)`** (Gradle only).
5. Pick your phone / emulator in the device dropdown → green **Run**.

Shared configs live in [`.run/`](../.run/).

### CLI

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
./gradlew :example:installDebug
adb shell am start -n io.github.vishalsharma7nov.kmpproto.example/.MainActivity
```

Mock mode is ON by default (`USE_MOCK_SERVER = true`). It still goes through
`createApi` → generated `GetUserRequest` / `User` codecs from `vendor/protos/user.proto`
(via an in-process mock HTTP engine). Tap **Call getUser** to see the decoded user map.

## Run (iOS)

1. Set your Apple Team ID in [`iosApp/Configuration/Config.xcconfig`](../iosApp/Configuration/Config.xcconfig) (`TEAM_ID=...`)
2. Open `iosApp/iosApp.xcodeproj` in Xcode
3. Select an iOS Simulator and Run

Xcode builds the Kotlin framework via `:example:embedAndSignAppleFrameworkForXcode`.

## Native gRPC note

For `TransportKind.NativeGrpc` install a `NativeGrpcBridge` (see library `nativegrpc` package). HTTP/Connect work without native code.
