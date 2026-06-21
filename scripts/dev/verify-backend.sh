#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

echo "=== OLCA Backend Verification ==="
echo "root: $ROOT_DIR"
echo

echo "[1/1] compileJava"
./gradlew compileJava

echo
echo "[OK] Backend compile verification passed."