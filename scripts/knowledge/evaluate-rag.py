#!/usr/bin/env python3
import json
import os
import sys
import urllib.parse
import urllib.request
from pathlib import Path


BASE_URL = os.environ.get("OLCA_BASE_URL", "http://localhost:8080")
TOP_N = 5


def request_vector_search(question):
    query = urllib.parse.urlencode({"question": question, "topN": TOP_N})
    url = f"{BASE_URL}/api/knowledge/vector-search?{query}"
    with urllib.request.urlopen(url, timeout=30) as response:
        return json.loads(response.read().decode("utf-8"))


def evaluate_case(case):
    results = request_vector_search(case["question"])
    topics = [item["topic"] for item in results]
    top1 = topics[0] if topics else "NONE"
    expected_top1 = case["expectedTop1"]
    allowed_top3 = set(case["allowedTop3"])

    if expected_top1 == "NONE":
        top1_pass = top1 == "NONE"
        top3_pass = not topics
    else:
        top1_pass = top1 == expected_top1
        top3_pass = any(topic in allowed_top3 for topic in topics[:3])

    return {
        "id": case["id"],
        "type": case["type"],
        "question": case["question"],
        "expectedTop1": expected_top1,
        "top1": top1,
        "top3": topics[:3],
        "top1Pass": top1_pass,
        "top3Pass": top3_pass,
    }


def main():
    if len(sys.argv) > 1:
        cases_path = Path(sys.argv[1])
    else:
        cases_path = Path(__file__).with_name("rag_eval_cases.json")

    cases = json.loads(cases_path.read_text(encoding="utf-8"))
    reports = []

    for case in cases:
        report = evaluate_case(case)
        reports.append(report)
        status = "PASS" if report["top1Pass"] else "FAIL"
        print(
            f"[{status}] {report['id']} type={report['type']} "
            f"top1={report['top1']} expected={report['expectedTop1']}"
        )
        print(f"       top3={report['top3']}")

    total = len(reports)
    top1_pass = sum(1 for report in reports if report["top1Pass"])
    top3_pass = sum(1 for report in reports if report["top3Pass"])
    top1_rate = top1_pass / total * 100
    top3_rate = top3_pass / total * 100

    print()
    print("===== RAG Evaluation Summary =====")
    print(f"total={total}")
    print(f"top1_pass={top1_pass}/{total} ({top1_rate:.1f}%)")
    print(f"top3_pass={top3_pass}/{total} ({top3_rate:.1f}%)")

    failed = [report for report in reports if not report["top1Pass"]]
    if failed:
        print()
        print("===== Top1 Failures =====")
        for report in failed:
            print(
                f"- {report['id']} question={report['question']} "
                f"expected={report['expectedTop1']} actual={report['top1']}"
            )

    if top1_rate < 70.0 or top3_rate < 85.0:
        sys.exit(1)


if __name__ == "__main__":
    main()
