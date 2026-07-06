# RAG Metadata Context Troubleshooting

## 배경

Java 공식 문서를 확장하면서 같은 키워드가 서로 다른 문맥에서 다른 의미로 쓰이는 문제가 확인되었다.

대표 사례는 다음과 같다.

- `List<String>`: `List Interface`가 아니라 `Generics` 문맥의 예시
- `map`: `Map Interface`일 수도 있고, Stream의 `map` 중간 연산일 수도 있음
- `collection`: Collection API 문맥이지만, 문자열 안에 `collect`가 포함되어 Stream 연산으로 오인될 수 있음

초기에는 문자열 기반 scorer와 query keyword만으로 보정했지만, 지식 문서가 늘어날수록 키워드 충돌이 반복될 가능성이 있었다. 그래서 문서에 `metadata`를 추가해 문서의 출처와 문맥을 구조적으로 표현하도록 했다.

## 추가한 metadata

`KnowledgeBase`에 다음 metadata를 추가했다.

```json
{
  "sourceType": "OFFICIAL_DOCS",
  "sourceName": "Oracle Java Tutorials - Generics",
  "sourceUrl": "https://docs.oracle.com/javase/tutorial/java/generics/index.html",
  "domain": "java",
  "category": "language-feature",
  "topicKey": "java-generics"
}
```

필드 역할은 다음과 같다.

- `sourceType`: 공식 문서, 개인 노트, 테스트 데이터 같은 출처 종류
- `sourceName`: 사람이 확인할 수 있는 출처 이름
- `sourceUrl`: 원문 추적 주소
- `domain`: 큰 지식 영역
- `category`: 문서 성격
- `topicKey`: 리랭킹에서 사용할 고유 주제 키

## 발생한 문제

metadata를 도입한 뒤 Stream 문서가 일부 질문에서 과하게 상승했다.

```text
metadata 도입 직후 RAG Evaluation
- total=47
- top1_pass=42/47 (89.4%)
- top3_pass=46/47 (97.9%)
```

실패 예시는 다음과 같다.

```text
- 스트림 고블린 패턴 알려줘
  expected=NONE
  actual=Java Official - Stream Aggregate Operations

- 자바 Collection 인터페이스는 뭐야?
  expected=Java Official - Collection Interface
  actual=Java Official - Stream Aggregate Operations

- 중복 제거가 필요하면 어떤 컬렉션을 써?
  expected=Java Official - Set Interface
  actual=Java Official - Stream Aggregate Operations
```

## 원인

원인은 metadata 자체가 아니라, metadata scorer와 Java language feature scorer가 Stream 문맥을 너무 넓게 판정한 것이었다.

특히 `collection`이라는 단어 안에 `collect`가 포함되어 있었기 때문에, Collection 질문이 Stream의 `collect` 연산 질문처럼 처리되었다.

```text
collection contains collect
```

또한 `스트림 고블린 패턴`처럼 알 수 없는 패턴 질문은 unknown guard로 낮아져야 했지만, Stream 관련 metadata boost가 다시 점수를 올리는 문제가 있었다.

## 수정

수정은 다음 기준으로 진행했다.

- `collect`, `filter`, `map`은 부분 문자열이 아니라 토큰 단위로만 Stream 연산으로 인정
- `collection`은 `collect`로 해석하지 않음
- 알 수 없는 패턴 질문은 metadata scorer와 Java language feature scorer에서도 boost하지 않음
- metadata는 기존 문자열 scorer를 대체하지 않고, 문맥이 명확한 경우에만 보조 점수로 사용

수정 후 결과는 다음과 같다.

```text
metadata context 수정 후 RAG Evaluation
- total=47
- top1_pass=47/47 (100.0%)
- top3_pass=47/47 (100.0%)
- verification: scripts/dev/verify-rag.sh pass
```

## 결론

metadata는 문서 수가 적을 때는 필수처럼 보이지 않을 수 있다. 하지만 공식 문서가 늘어나면 같은 단어가 여러 Java 문맥에서 다른 의미로 쓰이기 때문에, 문서 자체의 문맥을 구조화할 필요가 생긴다.

이번 수정의 핵심은 metadata를 크게 도입하는 것이 아니라, 검증 중 실제로 발생한 키워드 충돌을 기준으로 최소 metadata와 최소 scorer를 추가한 것이다.