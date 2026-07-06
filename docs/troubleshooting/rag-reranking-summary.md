# RAG 검색 재랭킹 트러블슈팅 요약

## 문제

공식 Java 문서(`Java Official - Classes and Objects`)를 KnowledgeBase에 추가했지만, 다음 질문에서 공식 문서가 1순위로 검색되지 않았다.

```text
자바 클래스와 객체 차이 알려줘
```

초기 검색 결과에서는 `Factory Pattern`, `Builder Pattern` 같은 디자인 패턴 문서가 공식 Java 문서보다 위에 노출되었다.

## 원인

저장이나 임베딩 생성 실패가 아니라, 벡터 유사도 기반 정렬의 한계였다.

디자인 패턴 문서에도 `java`, `객체`, `생성`, `class`, `object` 같은 표현이 포함되어 있어 질문과 의미적으로 가깝게 계산되었다.

즉 문제는 다음과 같았다.

```text
문서 저장: 성공
임베딩 생성: 성공
검색 후보 포함: 성공
최종 랭킹: 부정확
```

## 해결

`KnowledgeBaseService` 내부에 있던 검색 판단 로직을 `knowledge.search` 패키지로 분리하고, 재랭킹 구조를 추가했다.

추가된 주요 구성:

- `KnowledgeReranker`
- `KnowledgeCandidateScorer`
- `KeywordBoostScorer`
- `SourceTrustScorer`
- `DomainPenaltyScorer`
- `KnowledgeSearchCandidate`
- `KnowledgeSearchText`

최종 점수는 다음 요소를 합산한다.

```text
finalScore =
  vectorScore
  + keywordScore
  + topicScore
  + sourceTrustScore
  - domainPenalty
```

## 결과

수정 후 같은 질문에서 공식 Java 문서가 1순위로 올라왔다.

```text
Java Official - Classes and Objects
vector=0.7339
keyword=0.7200
topicScore=0.4200
sourceTrust=0.1200
penalty=0.0000
final=1.9939
```

최종 검색 결과:

```text
1. Java Official - Classes and Objects
2. Factory Pattern
3. Builder Pattern - 사용 시점
4. Singleton Pattern - 사용 시점
5. Design Pattern Basic Notes #2
```

## 배운 점

벡터 검색은 최종 판단자가 아니라 후보 검색기에 가깝다.

RAG 검색 품질을 안정화하려면 단순 similarity threshold 조정보다 다음 구조가 더 적합하다.

```text
Retriever
-> Guard
-> Reranker
-> Evidence Selector
-> LLM
```

이번 작업으로 OLCA의 RAG 검색은 단순 벡터 검색에서 점수 모델 기반 재랭킹 구조로 발전했다.

## 남은 개선

- 질문 intent에 따른 ranking strategy 분리
- Java 기본 질문에서 design-pattern 문서 penalty 강화
- LLM에 전달할 evidence를 top 1~2개로 제한하는 EvidenceSelector 추가
- 공식 문서 학습 자료 확장
- 평가 데이터셋 구축

