#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-all}"
IMAGE_NAME="${VERIFY_IMAGE_NAME:-olca-verifier:local}"
DOCKERFILE="${VERIFY_DOCKERFILE:-scripts/dev/ai-verification-sandbox.Dockerfile}"
NETWORK_MODE="${VERIFY_DOCKER_NETWORK:-host}"
RUN_ID="${VERIFY_RUN_ID:-$(date -u +%Y%m%dT%H%M%SZ)-$MODE}"

case "$MODE" in
  all)
    VERIFY_SCRIPT="scripts/dev/verify-all.sh"
    ;;
  backend)
    VERIFY_SCRIPT="scripts/dev/verify-backend.sh"
    ;;
  rag)
    VERIFY_SCRIPT="scripts/dev/verify-rag.sh"
    ;;
  *)
    echo "Usage: $0 [all|backend|rag]" >&2
    exit 2
    ;;
esac

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
RUNS_DIR="$ROOT_DIR/verification-runs"
RUN_DIR="$RUNS_DIR/$RUN_ID"
BUILD_LOG="$RUN_DIR/docker-build.log"
STDOUT_LOG="$RUN_DIR/stdout.log"
STDERR_LOG="$RUN_DIR/stderr.log"
SUMMARY_FILE="$RUN_DIR/summary.txt"
RESULT_FILE="$RUN_DIR/result.json"

mkdir -p "$RUN_DIR"

STARTED_AT="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

if [ ! -f "$ROOT_DIR/$VERIFY_SCRIPT" ]; then
  echo "Missing existing verification script: $VERIFY_SCRIPT" >&2
  echo "This Docker wrapper does not replace project verification logic." >&2
  exit 2
fi

if [ ! -f "$ROOT_DIR/$DOCKERFILE" ]; then
  echo "Missing verifier Dockerfile: $DOCKERFILE" >&2
  exit 2
fi

echo "AI verification sandbox run: $RUN_ID"
echo "Mode: $MODE"
echo "Script: $VERIFY_SCRIPT"
echo "Run directory: $RUN_DIR"

docker build \
  -f "$ROOT_DIR/$DOCKERFILE" \
  -t "$IMAGE_NAME" \
  "$ROOT_DIR" 2>&1 | tee "$BUILD_LOG"

DOCKER_ARGS=(
  run
  --rm
  --network "$NETWORK_MODE"
  -e "OLCA_BASE_URL=${OLCA_BASE_URL:-http://localhost:8080}"
  -e "OLLAMA_BASE_URL=${OLLAMA_BASE_URL:-http://localhost:11434}"
  -e "HOME=/tmp/olca-verifier"
  -e "GRADLE_USER_HOME=/tmp/olca-verifier/.gradle"
  -v "$ROOT_DIR:/workspace"
  -w /workspace
)

if command -v id >/dev/null 2>&1; then
  DOCKER_ARGS+=(--user "$(id -u):$(id -g)")
fi

DOCKER_ARGS+=("$IMAGE_NAME" bash "$VERIFY_SCRIPT")

set +e
docker "${DOCKER_ARGS[@]}" > >(tee "$STDOUT_LOG") 2> >(tee "$STDERR_LOG" >&2)
EXIT_CODE=$?
set -e

FINISHED_AT="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

if [ "$EXIT_CODE" -eq 0 ]; then
  RESULT="pass"
else
  RESULT="fail"
fi

cat > "$SUMMARY_FILE" <<EOF
AI Verification Sandbox Summary
================================
Run ID: $RUN_ID
Result: $RESULT
Exit code: $EXIT_CODE
Mode: $MODE
Script: $VERIFY_SCRIPT
Image: $IMAGE_NAME
Dockerfile: $DOCKERFILE
Network: $NETWORK_MODE
Started at: $STARTED_AT
Finished at: $FINISHED_AT

Logs:
- Docker build: $BUILD_LOG
- Stdout: $STDOUT_LOG
- Stderr: $STDERR_LOG
- Result JSON: $RESULT_FILE
EOF

cat > "$RESULT_FILE" <<EOF
{
  "runId": "$RUN_ID",
  "result": "$RESULT",
  "success": $([ "$EXIT_CODE" -eq 0 ] && echo true || echo false),
  "exitCode": $EXIT_CODE,
  "mode": "$MODE",
  "script": "$VERIFY_SCRIPT",
  "image": "$IMAGE_NAME",
  "dockerfile": "$DOCKERFILE",
  "network": "$NETWORK_MODE",
  "startedAt": "$STARTED_AT",
  "finishedAt": "$FINISHED_AT",
  "logs": {
    "dockerBuild": "$BUILD_LOG",
    "stdout": "$STDOUT_LOG",
    "stderr": "$STDERR_LOG",
    "summary": "$SUMMARY_FILE"
  }
}
EOF

printf '%s\n' "$RUN_ID" > "$RUNS_DIR/latest-run.txt"

echo
cat "$SUMMARY_FILE"

exit "$EXIT_CODE"
