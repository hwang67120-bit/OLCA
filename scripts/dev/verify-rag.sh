#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BASE_URL="${OLCA_BASE_URL:-http://localhost:8080}"
cd "$ROOT_DIR"

echo "=== OLCA RAG Verification ==="
echo "root: $ROOT_DIR"
echo "OLCA_BASE_URL: $BASE_URL"
echo

if ! command -v curl >/dev/null 2>&1; then
  echo "[FAIL] curl is required for the OLCA health check."
  exit 1
fi

if ! curl -fsS "$BASE_URL/api/knowledge" >/dev/null; then
  echo "[FAIL] OLCA is not reachable: $BASE_URL/api/knowledge"
  echo "Start OLCA first, then rerun this script."
  exit 1
fi

echo "[1/1] RAG evaluation"
OLCA_BASE_URL="$BASE_URL" python3 scripts/knowledge/evaluate-rag.py

echo
echo "[OK] RAG verification passed."