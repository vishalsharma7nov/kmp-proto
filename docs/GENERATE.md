# Generate

## Local

```bash
./gradlew kmpProtoGenerate \
  -PkmpProto.from=local \
  -PkmpProto.protoPath=./protos \
  -PkmpProto.out=./src/commonMain/kotlin/generated
```

## GitHub

Pin a tag or commit (do not use a moving branch for releases):

```bash
./gradlew kmpProtoGenerate \
  -PkmpProto.from=github \
  -PkmpProto.repo=https://github.com/you/protos.git \
  -PkmpProto.ref=v1.2.3
```

## Buf

Requires the Buf CLI installed:

```bash
./gradlew kmpProtoGenerate \
  -PkmpProto.from=buf \
  -PkmpProto.module=buf.build/acme/petapis \
  -PkmpProto.ref=1.0.0
```

## Lock file

`protos.lock.json` at the repo root stores the default source for `./gradlew syncProtos`.

## Watch

```bash
./gradlew kmpProtoWatch
```

Regenerates when local `.proto` files change.

## When to regenerate

Any time you add/rename an RPC or message field that clients need. Generated files are overwritten — do not edit them by hand.
