#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

questions=(
  "자바 클래스와 객체 차이 알려줘"
  "자바 인터페이스는 언제 써?"
  "자바 상속은 언제 쓰는 게 좋아?"
  "자바 패키지는 왜 나눠?"
  "자바 예외 처리는 왜 필요해?"
)

for question in "${questions[@]}"; do
  printf '\n===== %s =====\n' "$question"
  curl -sS --get "${BASE_URL}/api/knowledge/vector-search" \
    --data-urlencode "question=${question}" \
    --data-urlencode "topN=5"
  printf '\n'
done
