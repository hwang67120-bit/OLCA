# 2026-06-14 TIL - RAG 검색 재랭킹과 공식 문서 우선순위 개선

## 오늘의 목표

공식 문서 기반 학습 자료를 KnowledgeBase에 추가한 뒤, 실제 검색 결과에서 공식 문서가 올바르게 선택되는지 검증했다.

초기 목표는 단순했다.

- 공식 Java 문서를 작은 단위로 학습 자료에 추가한다.
- 질문이 들어왔을 때 해당 공식 문서가 검색되는지 확인한다.
- 검색 결과가 이상하면 원인을 로그로 추적한다.

하지만 테스트 과정에서 단순한 임베딩 저장 문제가 아니라, 검색 랭킹 구조 자체의 문제가 발견되었다.

## 배경

현재 OLCA의 RAG 흐름은 다음 구조를 가진다.

```text
사용자 질문
-> 질문 라우팅
-> KnowledgeBase 검색
-> 관련 문서 선택
-> PromptBuilder로 근거 구성
-> Ollama 답변 생성
```

KnowledgeBase 문서는 저장 시 임베딩이 생성된다.

```text
topic + content
-> EmbeddingService.embed()
-> vectorSize=768
-> MongoDB KnowledgeBase.embedding 저장
```

질문 검색 시에는 질문을 임베딩하고, 저장된 문서 임베딩과 cosine similarity를 비교한다.

## 추가한 공식 기반 학습 자료

처음에는 작은 공식 자료 하나만 추가했다.

```text
topic: Java Official - Classes and Objects
source: Oracle Java Tutorials - Classes
url: https://docs.oracle.com/javase/tutorial/java/javaOO/classes.html
keywords:
- java
- official
- class
- object
- 클래스
- 객체
- oracle
```

저장 요청:

```bash
curl -X POST http://localhost:8080/api/knowledge \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "Java Official - Classes and Objects",
    "keywords": ["java","official","class","object","클래스","객체","oracle"],
    "content": "출처: Oracle Java Tutorials - Classes. URL: https://docs.oracle.com/javase/tutorial/java/javaOO/classes.html\n\nJava에서 클래스는 객체를 만들기 위한 설계 단위다. 클래스는 상태를 표현하는 필드, 동작을 표현하는 메서드, 객체 생성 시 초기값을 정하는 생성자를 함께 가진다. 객체는 클래스를 바탕으로 만들어진 실제 인스턴스이며, 각 객체는 자신의 필드 값을 가진다. 이 개념은 도메인 모델, DTO, 엔티티, 서비스 객체를 구분할 때 기본이 된다."
  }'
```

저장 결과:

```text
topic=Java Official - Classes and Objects
version=1
embedding 생성 성공
```

## 발견한 문제

공식 Java 문서를 추가했지만, 다음 질문에서 공식 문서가 상위에 오지 않았다.

```text
자바 클래스와 객체 차이 알려줘
```

초기 vector search 결과는 다음과 유사했다.

```text
1. Factory Pattern
2. Builder Pattern
3. Design Pattern Basic Notes #1
4. Design Pattern Basic Notes #2
5. Java Official - Classes and Objects
```

즉, 공식 Java 기본 문서보다 디자인 패턴 문서가 더 높은 점수를 받았다.

## 원인 분석

문제는 저장 실패가 아니었다.

```text
저장: 성공
임베딩 생성: 성공
검색 후보 포함: 성공
최종 랭킹: 실패
```

임베딩 모델은 질문의 다음 표현에 강하게 반응했다.

```text
자바
클래스
객체
차이
```

기존 디자인 패턴 문서에도 다음 표현이 많이 포함되어 있었다.

```text
java
객체
생성
클래스
new
구현체
```

특히 Factory Pattern 문서는 "객체 생성 책임"을 설명하기 때문에, 벡터 검색에서 "클래스와 객체" 질문과 의미적으로 가까운 후보로 잡혔다.

이 현상은 단순 버그라기보다 순수 벡터 검색의 일반적인 한계에 가깝다.

```text
벡터 검색은 의미적으로 가까운 후보를 잘 찾지만,
질문의 실제 의도와 문서의 도메인을 항상 정확히 분리하지는 못한다.
```

## 처음 생각한 단순 해결책

처음에는 다음 방식도 가능해 보였다.

- similarity threshold를 조정한다.
- topic guard 조건을 추가한다.
- design-pattern 문서에 penalty를 하드코딩한다.

하지만 이 방식은 문제가 생길 때마다 if 조건을 계속 추가하는 구조가 된다.

```text
문제 발견
-> 조건 추가
-> 또 다른 예외 발견
-> 조건 추가
-> 검색 로직 복잡도 증가
```

이 방식은 단기적으로는 동작하지만 장기적으로는 유지보수하기 어렵다.

## 설계 방향 전환

검색 문제를 "조건 하나 추가"가 아니라 "검색 파이프라인 재설계"로 보기로 했다.

참고한 개념은 다음과 같다.

- Hybrid Search
- Re-ranking
- Guardrails
- Strategy Pattern
- Chain of Responsibility
- 정책과 인프라 분리

최종 목표는 다음 구조였다.

```text
1. Vector Search
   질문과 의미적으로 가까운 후보를 넓게 가져온다.

2. Topic Guard
   명백히 다른 도메인의 문서를 제거하거나 감점한다.

3. Re-ranking
   keyword boost, topic boost, source trust, domain penalty를 계산한다.

4. Final Selection
   finalScore 기준으로 LLM에 전달할 근거 문서를 선택한다.
```

## 적용한 구조

새로 `knowledge.search` 패키지를 만들고 검색 정책을 분리했다.

```text
knowledge.search
├── KnowledgeReranker
├── KnowledgeCandidateScorer
├── KnowledgeCandidateContext
├── KnowledgeSearchCandidate
├── CandidateScoreContribution
├── KeywordBoostScorer
├── SourceTrustScorer
├── DomainPenaltyScorer
└── KnowledgeSearchText
```

### KnowledgeBaseService 역할 변경

기존에는 `KnowledgeBaseService`가 다음 책임을 모두 가지고 있었다.

```text
DB 조회
임베딩
cosine similarity 계산
topic guard
정렬
최종 문서 선택
```

수정 후에는 다음 정도만 담당한다.

```text
질문 임베딩 생성
KnowledgeBase 문서 조회
최신 버전 문서만 추림
KnowledgeReranker에 후보 전달
```

검색 정책은 `KnowledgeReranker`와 scorer 구현체가 담당한다.

## 점수 모델

최종 점수는 다음 개념으로 계산한다.

```text
finalScore =
  vectorScore
  + keywordScore
  + topicScore
  + sourceTrustScore
  - domainPenalty
```

### vectorScore

기존 cosine similarity 점수다.

의미적으로 가까운 후보를 찾는 기본 점수로 사용한다.

### keywordScore

질문 핵심 키워드가 문서의 topic, keywords, content에 직접 매칭되면 가산한다.

예:

```text
질문: 자바 클래스와 객체 차이 알려줘
키워드: java, class, classes, object, objects, 클래스, 객체
```

### topicScore

문서 topic에 질문 키워드가 직접 들어가면 추가 가산한다.

예:

```text
topic: Java Official - Classes and Objects
matched topic keywords:
- Java
- Classes
- Objects
```

### sourceTrustScore

공식 문서, Oracle 문서, docs.oracle.com 같은 신뢰 가능한 출처는 가산한다.

예:

```text
source_trust:official
```

### domainPenalty

질문은 Java 기본 개념인데, 문서가 design-pattern 도메인일 경우 감점한다.

예:

```text
domain_penalty:design_pattern_mismatch
```

## 검증 결과

수정 후 같은 질문으로 다시 검색했다.

```bash
curl --get "http://localhost:8080/api/knowledge/vector-search" \
  --data-urlencode "question=자바 클래스와 객체 차이 알려줘" \
  --data-urlencode "topN=5"
```

핵심 로그:

```text
[RAG_SCORE] topic=Java Official - Classes and Objects
vector=0.7339
keyword=0.7200
topicScore=0.4200
sourceTrust=0.1200
penalty=0.0000
final=1.9939
usable=true
matchedKeywords=[java, class, object, objects]
reasons=[topic_pass, keyword_match, topic_keyword_match:4, source_trust:official]
```

최종 검색 결과:

```text
topics=[
  Java Official - Classes and Objects,
  Factory Pattern,
  Builder Pattern - 사용 시점,
  Singleton Pattern - 사용 시점,
  Design Pattern Basic Notes #2
]
```

기존에는 `Factory Pattern`이 1등이었지만, 수정 후에는 공식 Java 문서가 1등으로 올라왔다.

## 이번 문제의 핵심 교훈

### 1. 벡터 검색은 후보 검색이지 최종 판단자가 아니다

벡터 검색은 의미적으로 가까운 문서를 찾는 데 강하다.

하지만 질문 의도, 출처 신뢰도, 도메인 불일치까지 완벽히 판단하지는 못한다.

따라서 RAG에서는 보통 다음 구조가 더 안정적이다.

```text
Retriever
-> Reranker
-> Evidence Selector
-> LLM
```

### 2. threshold 튜닝만으로는 한계가 있다

similarity threshold를 낮추면 필요한 문서를 더 잘 찾을 수 있지만, 동시에 관련 없는 문서도 들어올 수 있다.

반대로 threshold를 높이면 노이즈는 줄지만, 필요한 공식 문서가 탈락할 수 있다.

이번 문제는 threshold 문제가 아니라 ranking 문제였다.

### 3. 검색 정책은 인프라와 분리해야 한다

임베딩 생성, MongoDB 조회, cosine similarity 계산은 인프라에 가깝다.

반면 어떤 문서를 우선할지, 어떤 출처를 신뢰할지, 어떤 도메인을 감점할지는 정책이다.

정책을 분리해야 다음 요구사항이 생겨도 전체 검색 서비스를 뜯지 않아도 된다.

예:

```text
Spring 질문이면 Spring 공식 문서 우선
프로젝트 내부 질문이면 내부 README 우선
Java 기본 질문이면 Oracle 공식 문서 우선
디자인 패턴 질문이면 design-pattern 문서 우선
```

### 4. 로그는 단순 디버깅이 아니라 설계 검증 도구다

이번에 `[RAG_SCORE]` 로그를 추가하면서 다음을 확인할 수 있게 되었다.

```text
왜 이 문서가 후보가 되었는가?
어떤 키워드가 매칭되었는가?
공식 문서 boost가 적용되었는가?
도메인 penalty가 적용되었는가?
최종 점수는 왜 역전되었는가?
```

이제 검색 결과가 이상할 때 감으로 수정하지 않고, 점수 요소별로 원인을 볼 수 있다.

## 남은 문제

이번 수정으로 공식 Java 문서가 1등으로 올라오긴 했지만, 디자인 패턴 문서도 여전히 top 5 안에 남아 있다.

이유는 공통 키워드가 여전히 많기 때문이다.

```text
java
객체
object
class
생성
```

다음 개선 후보:

- Java 기본 질문에서는 design-pattern 문서를 top 3 밖으로 밀기
- domainPenalty 값을 더 강하게 조정하기
- EvidenceSelector를 추가해서 LLM에는 top 1~2개만 전달하기
- 질문 intent를 먼저 분류해서 ranking strategy를 선택하기
- 공식 문서 학습 자료를 더 추가해서 후보 품질을 높이기

## 오늘의 결론

오늘의 핵심은 "검색이 안 된다"가 아니었다.

정확히는 다음 문제였다.

```text
공식 문서는 저장되고 검색 후보에도 들어오지만,
벡터 점수만으로 정렬하면 질문 의도와 다른 문서가 위로 올라온다.
```

그래서 해결책도 단순 threshold 조정이 아니라, 검색 후보에 대한 재랭킹 구조였다.

최종적으로 OLCA의 RAG 검색은 다음 단계로 발전했다.

```text
단순 벡터 검색
-> 가드가 있는 벡터 검색
-> 점수 모델 기반 재랭킹 검색
```

이번 작업은 RAG가 "일단 답하는 기능"에서 "왜 이 근거를 선택했는지 설명 가능한 구조"로 넘어가는 중요한 단계였다.

