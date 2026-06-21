#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

echo "=== OLCA Verification Pipeline ==="
echo "root: $ROOT_DIR"
echo

bash scripts/dev/verify-backend.sh
bash scripts/dev/verify-rag.sh

echo
echo "[OK] All OLCA verification steps passed."