#!/usr/bin/env python3
"""Read AI verification sandbox evidence without redefining pass/fail rules."""

from __future__ import annotations

import argparse
import json
from dataclasses import dataclass
from pathlib import Path


REQUIRED_FIELDS = (
    "runId",
    "result",
    "success",
    "exitCode",
    "mode",
    "script",
    "image",
    "startedAt",
    "finishedAt",
    "logs",
)


@dataclass(frozen=True)
class VerificationEvidence:
    run_id: str
    result: str
    success: bool
    exit_code: int
    mode: str
    script: str
    image: str
    started_at: str
    finished_at: str
    summary_path: str
    stdout_path: str
    stderr_path: str

    @classmethod
    def from_json(cls, path: Path) -> "VerificationEvidence":
        payload = json.loads(path.read_text(encoding="utf-8"))
        missing = [field for field in REQUIRED_FIELDS if field not in payload]
        if missing:
            raise ValueError(f"Invalid verification result. Missing: {', '.join(missing)}")

        logs = payload.get("logs")
        if not isinstance(logs, dict):
            raise ValueError("Invalid verification result. logs must be an object.")

        success = payload["success"]
        if not isinstance(success, bool):
            raise ValueError("Invalid verification result. success must be a boolean.")

        for field in ("summary", "stdout", "stderr"):
            if field not in logs:
                raise ValueError(f"Invalid verification result. logs.{field} is missing.")

        return cls(
            run_id=str(payload["runId"]),
            result=str(payload["result"]),
            success=success,
            exit_code=int(payload["exitCode"]),
            mode=str(payload["mode"]),
            script=str(payload["script"]),
            image=str(payload["image"]),
            started_at=str(payload["startedAt"]),
            finished_at=str(payload["finishedAt"]),
            summary_path=str(logs["summary"]),
            stdout_path=str(logs["stdout"]),
            stderr_path=str(logs["stderr"]),
        )

    def to_claim_text(self) -> str:
        status = "PASS" if self.success else "FAIL"
        return "\n".join(
            (
                "Container verification evidence:",
                f"- runId: {self.run_id}",
                f"- result: {status}",
                f"- exitCode: {self.exit_code}",
                f"- mode: {self.mode}",
                f"- script: {self.script}",
                f"- image: {self.image}",
                f"- startedAt: {self.started_at}",
                f"- finishedAt: {self.finished_at}",
                f"- summary: {self.summary_path}",
                f"- stdout: {self.stdout_path}",
                f"- stderr: {self.stderr_path}",
            )
        )


def project_root() -> Path:
    return Path(__file__).resolve().parents[2]


def runs_dir(root: Path) -> Path:
    return root / "verification-runs"


def latest_run_id(root: Path) -> str:
    latest_file = runs_dir(root) / "latest-run.txt"
    if not latest_file.exists():
        raise FileNotFoundError("No latest sandbox run found.")
    run_id = latest_file.read_text(encoding="utf-8").strip()
    if not run_id:
        raise ValueError("latest-run.txt is empty.")
    return run_id


def result_path(root: Path, run_id: str) -> Path:
    return runs_dir(root) / run_id / "result.json"


def load_evidence(root: Path, run_id: str) -> VerificationEvidence:
    path = result_path(root, run_id)
    if not path.exists():
        raise FileNotFoundError(f"No result.json found for runId: {run_id}")
    return VerificationEvidence.from_json(path)


def list_runs(root: Path) -> list[str]:
    base = runs_dir(root)
    if not base.exists():
        return []
    return sorted(
        path.name
        for path in base.iterdir()
        if path.is_dir() and (path / "result.json").exists()
    )


def main() -> int:
    parser = argparse.ArgumentParser(description="Inspect AI verification sandbox evidence.")
    parser.add_argument(
        "command",
        choices=("latest", "show", "assert-pass", "list"),
        help="Evidence action to run.",
    )
    parser.add_argument("run_id", nargs="?", help="Run ID. Defaults to latest where supported.")
    args = parser.parse_args()

    root = project_root()

    if args.command == "list":
        for run_id in list_runs(root):
            print(run_id)
        return 0

    run_id = args.run_id or latest_run_id(root)
    evidence = load_evidence(root, run_id)

    if args.command in ("latest", "show"):
        print(evidence.to_claim_text())
        return 0

    if args.command == "assert-pass":
        print(evidence.to_claim_text())
        return 0 if evidence.success else 1

    return 2


if __name__ == "__main__":
    raise SystemExit(main())
