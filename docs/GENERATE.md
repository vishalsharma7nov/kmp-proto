# Generate

Set the same location on app config as `ProtoClientConfig.protoSource` (see [CONFIG.md](./CONFIG.md)). The Gradle properties below are what `kmpProtoGenerate` / `kmpProtoWatch` actually read.

## Local

`from` defaults to `local`. `protoPath` is optional: if you omit it, generate looks for `.proto` files in `protos/`, `proto/`, `vendor/protos/`, then the project directory.

```bash
./gradlew kmpProtoGenerate -PkmpProto.out=./src/commonMain/kotlin/generated
./gradlew kmpProtoGenerate \
  -PkmpProto.from=local \
  -PkmpProto.protoPath=./protos \
  -PkmpProto.out=./src/commonMain/kotlin/generated
```

Matches:

```kotlin
protoSource = ProtoSource.local()           // discover in the project
protoSource = ProtoSource.local("protos")   // explicit folder
```

## GitHub

Pin a tag or commit (do not use a moving branch for releases). Optional `protoPath` is a subdirectory inside the clone.

```bash
./gradlew kmpProtoGenerate \
  -PkmpProto.from=github \
  -PkmpProto.repo=https://github.com/you/protos.git \
  -PkmpProto.ref=v1.2.3 \
  -PkmpProto.protoPath=protos
```

Matches `ProtoSource.github(repo = "...", ref = "v1.2.3", path = "protos")`.

## Buf

Requires the Buf CLI installed:

```bash
./gradlew kmpProtoGenerate \
  -PkmpProto.from=buf \
  -PkmpProto.module=buf.build/acme/petapis \
  -PkmpProto.ref=1.0.0
```

Matches `ProtoSource.buf(module = "buf.build/acme/petapis", ref = "1.0.0")`.

## Lock file

`protos.lock.json` at the repo root stores the default source for `./gradlew syncProtos`.

## Watch

```bash
./gradlew kmpProtoWatch
```

Regenerates when local `.proto` files change.

## When to regenerate

Any time you add/rename an RPC or message field that clients need. Generated files are overwritten — do not edit them by hand.
