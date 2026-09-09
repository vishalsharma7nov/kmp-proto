#!/usr/bin/env bash
set -euo pipefail
# Maintainer shortcut — sync protos from protos.lock.json and regenerate.
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
./gradlew :kmp-proto:syncProtos
