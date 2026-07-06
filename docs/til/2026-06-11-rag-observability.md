# 2026-06-11 TIL - RAG 관측 가능성, 로그, 응답 지연 분석

## 오늘의 목표

RAG 기능을 계속 추가하기 전에, AI 요청이 내부에서 어떻게 처리되는지 보이게 만드는 것이 목표였다.

기존에는 오류 로그는 있었지만 다음 내용을 알기 어려웠다.

- 사용자의 질문이 대화형인지 학습형인지
- RAG를 실제로 탔는지
- 벡터 검색이 실행됐는지
- 임베딩 생성 시간이 얼마나 걸렸는지
- LLM 호출이 얼마나 느린지
- 캐시가 실제로 동작하는지

그래서 오늘은 기능 확장보다 관측 가능성 확보를 우선했다.

## 적용한 작업

### 1. AOP 기반 Trace 로그 추가

공통 실행 시간을 직접 서비스 코드마다 반복해서 찍지 않기 위해 AOP를 적용했다.

추가한 파일:

- `TraceLog`
- `TraceKeys`
- `TraceLogAspect`

적용한 핵심 메서드:

- `ChatFlowService.process`
- `EmbeddingService.embed`
- `KnowledgeBaseService.vectorSearch`
- `KnowledgeBaseService.vectorSearchWithScore`
- `OllamaService.chat`

Reactive `Mono`, `Flux`는 메서드 반환 시점이 아니라 실제 구독 후 실행되는 시간이 중요하므로 `doOnSuccess`, `doOnError`, `doOnComplete` 기준으로 시간을 측정하도록 했다.

### 2. 서비스 의미 로그 추가

AOP는 시간을 기록하고, 서비스 로그는 의미를 기록하도록 역할을 나눴다.

예시:

```text
[AI_TRACE_START] traceId=... userId=1(사용자) sessionId=1(대화) questionLength=2(질문길이)
[AI_CACHE] traceId=... hit=false(캐시미스)
[AI_ROUTE] route=CONVERSATION(대화형) reason=no_knowledge_intent(학습의도없음)
[AI_CONTEXT] route=CONVERSATION(대화형) knowledgeCount=0(지식문서수)
[LLM_REQUEST] systemPromptLength=383(시스템프롬프트길이) userPromptLength=13(사용자프롬프트길이)
[TRACE] traceId=... method=OllamaService.chat(메서드) elapsedMs=9456(소요시간ms) success=true(성공)
```

로그 키는 영어로 유지하고, 값 옆에 한국어 의미를 붙였다.

이유:

- 영어 키는 검색하기 좋다.
- 한국어 설명은 흐름을 읽기 쉽다.
- 로그 파싱 가능성과 학습 편의성을 둘 다 챙길 수 있다.

## 오늘 발견한 오류

### 1. MongoDB `collation` 설정 오류

오류:

```text
Field 'locale' is invalid in: { locale: "chatFlow" }
```

원인:

```java
@Document(collation = "chatFlow")
```

`collation`은 컬렉션 이름이 아니라 문자열 비교/정렬 규칙 설정이다. MongoDB는 이를 locale 값으로 해석했고, `chatFlow`는 올바른 locale이 아니기 때문에 오류가 발생했다.

수정:

```java
@Document(collection = "chatFlow")
```

### 2. MongoDB text index 없음

오류:

```text
text index required for $text query
```

원인:

`KnowledgeBaseRepository.textSearch()`에서 `$text` 쿼리를 사용하지만, MongoDB 컬렉션에 text index가 생성되어 있지 않았다.

도메인에는 이미 다음 설정이 있었다.

```java
@TextIndexed(weight = 3)
private String topic;

@TextIndexed(weight = 2)
private String content;
```

하지만 Spring Data MongoDB의 자동 인덱스 생성이 활성화되어 있지 않았다.

수정:

```yaml
spring:
  data:
    mongodb:
      auto-index-creation: true
```

## 로그로 확인한 사실

### 1. 대화형 질문은 RAG를 타지 않았다

질문:

```text
안녕
```

로그:

```text
[AI_ROUTE] route=CONVERSATION(대화형) reason=no_knowledge_intent(학습의도없음)
[AI_CONTEXT] route=CONVERSATION(대화형) knowledgeCount=0
```

의미:

- 대화형 질문은 KnowledgeBase 벡터 검색을 타지 않았다.
- 라우팅은 1차적으로 정상 작동했다.

### 2. 대화형 질문도 느린 원인은 LLM 호출이었다

로그:

```text
questionLength=2
userPromptLength=13
systemPromptLength=383
OllamaService.chat elapsedMs=9456
totalMs=11599
```

결론:

- RAG가 느린 것이 아니었다.
- 대화형 질문에서는 벡터 검색과 임베딩이 실행되지 않았다.
- 병목은 `OllamaService.chat`, 즉 LLM 호출이었다.

### 3. 학습형 질문은 실제로 벡터 검색을 사용했다

질문:

```text
빌더 패턴은 언제 써?
```

로그:

```text
[AI_ROUTE] route=KNOWLEDGE(학습형)
[VECTOR_SEARCH] expandedQuestionLength=129
[EMBEDDING] inputLength=129
[EMBEDDING] vectorSize=768
[VECTOR_SEARCH] resultCount=3 topics=[Factory Pattern, Builder Pattern - 사용 시점, Builder Pattern]
```

의미:

- 질문 확장이 실행됐다.
- 질문 임베딩이 생성됐다.
- 768차원 벡터가 생성됐다.
- KnowledgeBase의 임베딩과 유사도 비교 후 상위 문서가 선택됐다.

즉 RAG는 이론상 붙어 있는 것이 아니라 실제로 벡터 검색 경로를 타고 있었다.

### 4. 학습형 질문의 병목도 대부분 LLM이었다

로그:

```text
전체 시간: 21904ms
임베딩 시간: 3819ms
벡터 검색 전체: 3971ms
LLM 시간: 16692ms
응답 길이: 1601
```

결론:

- 가장 큰 병목은 LLM 장문 생성이다.
- 두 번째 병목은 임베딩 생성이다.
- 현재 데이터 규모에서는 벡터 검색 자체보다 LLM 응답 생성 시간이 더 크다.

## 오늘 얻은 결론

### 1. 지금 문제는 RAG 추가 부족이 아니라 관측 부족이었다

기능은 있었지만 전체 흐름이 보이지 않았다.

오늘 로그를 추가하면서 다음을 확인할 수 있게 되었다.

- 질문 경로
- 캐시 hit/miss
- RAG 사용 여부
- 임베딩 시간
- 벡터 검색 결과
- LLM 호출 시간
- 전체 응답 시간

### 2. 응답 지연의 핵심은 프롬프트와 답변 길이다

학습형 질문에서 `userPromptLength=742`, `responseLength=1601`이 나왔다.

짧은 질문이어도 RAG 문서 3개가 프롬프트에 들어가고, LLM이 장문 답변을 생성하면 응답 시간이 크게 늘어난다.

다음 개선 방향:

- 시스템 프롬프트에 짧은 답변 원칙 추가
- RAG 답변은 3~5문장 제한
- 필요 시 `num_predict`로 생성 토큰 제한
- 자세한 설명은 사용자가 요청할 때 이어서 제공

### 3. 캐시는 실제로 속도 개선 가능성이 있다

같은 질문을 두 번째 요청했을 때 빨라졌다.

검증 기준:

```text
[AI_CACHE] hit=true(캐시적중)
```

캐시가 적중하면 다음 경로가 생략되어야 한다.

- 벡터 검색
- 임베딩
- LLM 호출

## 다음 작업 후보

### 1. 프롬프트 제어

목표:

- 장문병 방지
- 실시간 대화에 가까운 응답

원칙:

```text
첫 답변은 2~4문장
한 번에 하나의 핵심만 답변
자세한 설명은 사용자가 요청할 때 이어서 제공
마지막에 짧은 꼬리 질문 1개
```

### 2. 토큰 제한

Ollama 옵션에서 생성 토큰 수를 제한한다.

목표:

- 응답 길이 제한
- LLM 생성 시간 감소

### 3. 선응답 후처리 구조

학습형/검색형 질문이면 먼저 짧은 선응답을 보낸다.

예:

```text
잠깐만, 관련 내용을 확인해볼게.
```

그 후 RAG 검색과 LLM 답변 생성을 진행한다.

검색 결과가 없으면:

```text
관련 자료를 못 찾겠어. 키워드를 조금 더 정확히 말해줘.
```

### 4. 캐싱 전략

단계별 캐싱 후보:

- 동일 질문 답변 캐시
- 질문 임베딩 캐시
- 벡터 검색 결과 캐시
- 문서 캐시
- Semantic Cache
- Prefix Cache

주의:

- 최종 답변 캐시는 보수적으로 적용
- 의미 기반 캐시는 오답 위험이 있으므로 검색 결과 캐시에 먼저 적용
- KnowledgeBase 변경 시 캐시 무효화 필요

### 5. Resilience4j와 완충 구조 검토

실시간 경로에는 Kafka를 바로 넣기보다 먼저 다음을 고려한다.

- TimeLimiter
- CircuitBreaker
- Bulkhead
- RateLimiter
- timeout
- cache TTL/maxSize

Kafka는 실시간 대화보다는 다음 작업에 더 적합하다.

- 긴 문서 import
- 대량 embedding 생성
- 실패 작업 재시도
- trace/log 이벤트 적재

## 오늘의 핵심 문장

```text
보이지 않는 로직은 개선할 수 없다.
오늘은 RAG를 더 붙인 것이 아니라, RAG가 실제로 어떻게 움직이는지 보이게 만들었다.
```
