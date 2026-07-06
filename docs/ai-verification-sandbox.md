# AI Verification Sandbox

This document describes the first verification-sandbox step.

The sandbox is not a new verifier and does not contain new RAG judgment logic.
It only runs the existing project verification scripts inside an isolated Docker
container.

## Goal

- Keep product logic unchanged.
- Reuse the existing `scripts/dev/verify-all.sh` flow.
- Reuse existing backend compile checks and RAG eval checks.
- Return Docker exit code and logs to the caller.

## Files

- `scripts/dev/ai-verification-sandbox.Dockerfile`
- `scripts/dev/verify-in-docker.sh`
- `scripts/dev/verification-evidence.py`

## Usage

```bash
bash scripts/dev/verify-in-docker.sh all
bash scripts/dev/verify-in-docker.sh backend
bash scripts/dev/verify-in-docker.sh rag
```

After a run, inspect the latest evidence:

```bash
python3 scripts/dev/verification-evidence.py latest
python3 scripts/dev/verification-evidence.py assert-pass
```

## OLCA API

```bash
curl -X POST http://localhost:8080/api/verification/sandbox \
  -H "Content-Type: application/json" \
  -d '{"mode":"all","repeat":4}'
```

`repeat` can be `1` to `4`. If any run fails, OLCA stops the sequence and
returns the evidence collected so far.
## Flow

```text
AI or developer requests verification
-> verify-in-docker.sh selects an existing verify script
-> Docker builds/uses olca-verifier:local
-> container mounts the current project at /workspace
-> existing verify script runs inside the container
-> exit code and logs are stored and returned unchanged
```

## Evidence

Each run writes a folder under `verification-runs/`.

```text
verification-runs/<runId>/
  docker-build.log
  stdout.log
  stderr.log
  summary.txt
  result.json
verification-runs/latest-run.txt
```

Use `summary.txt` for quick human review. Use `result.json` when OLCA or an AI
tool needs a machine-readable result.

An answer should only claim sandbox verification when it can include the run ID,
script, image, result, and exit code from `summary.txt` or `result.json`.

`verification-evidence.py assert-pass` exists for that policy. It does not
create new quality rules. It only reads `result.json` and returns success when
the existing Docker run succeeded with exit code `0`.

## Boundaries

The sandbox may define runtime tools such as Java, Gradle, Python, bash, curl,
and git. It must not define new pass/fail rules for RAG quality.

Pass/fail remains owned by existing scripts such as:

- `scripts/dev/verify-backend.sh`
- `scripts/dev/verify-rag.sh`
- `scripts/dev/verify-all.sh`
