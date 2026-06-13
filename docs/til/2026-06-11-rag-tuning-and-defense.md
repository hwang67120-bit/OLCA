# 2026-06-11 TIL - RAG 튜닝, 할루시네이션 방어, QueryDSL 적용

## 오늘의 목표

어제 추가한 관측 로그를 기반으로 RAG 파이프라인의 응답 길이, 검색 품질, 할루시네이션 위험을 줄이는 것이 목표였다.

오늘의 핵심 방향은 다음과 같았다.

- 프롬프트와 토큰 제한으로 장문 답변 줄이기
- 벡터 검색 유사도 기준으로 관련 없는 문서 제거
- 검색 결과가 없을 때 LLM 호출 차단
- 캐시가 실제 속도 개선에 기여하는지 확인
- 배운 QueryDSL을 작은 범위에 적용해 관계 조회를 명확히 하기

## 1. 프롬프트와 토큰 제한

### 문제

기존 답변은 질문이 짧아도 너무 길게 생성됐다.

예시:

```text
질문: 빌더 패턴은 언제 써?
응답: 긴 설명 + 코드 예시 + 여러 항목
```

로그상으로는 다음 문제가 보였다.

```text
responseLength=1601
OllamaService.chat elapsedMs=16692
```

LLM은 답변을 한 토큰씩 생성하기 때문에, 응답이 길수록 시간이 크게 늘어난다.

### 수정

`PromptBuilder`를 실시간 대화형 AI에 맞게 수정했다.

적용한 원칙:

```text
1. 첫 답변은 2~4문장으로 제한
2. 학습형 질문도 먼저 핵심만 답변
3. 자세한 설명은 사용자가 요청할 때 이어서 제공
4. 코드 예시는 사용자가 요청할 때만 제공
5. 마지막에는 필요한 경우 짧은 후속 질문 1개만 사용
```

추가로 `application.yaml`에 Ollama 생성 토큰 제한을 추가했다.

```yaml
spring:
  ai:
    ollama:
      chat:
        options:
          num-predict: 256
```

### 결과

이전:

```text
responseLength=1601
LLM=16.7초
전체=21.9초
```

수정 후:

```text
responseLength=468
LLM=5.1초
전체=7.1초
```

프롬프트와 토큰 제한이 응답 길이와 속도에 확실히 영향을 주는 것을 확인했다.

## 2. 프롬프트 길이에 대한 이해

처음에는 사용자가 짧게 질문했는데도 `systemPromptLength=303`, `userPromptLength=113`처럼 길이가 찍히는 이유가 헷갈렸다.

정리하면 다음과 같다.

```text
systemPrompt = 항상 붙는 AI 역할/규칙 설명서
userPrompt = 이번 답변 제한 + 관련 지식 + 이전 대화 + 사용자 질문
```

즉 사용자가 보낸 질문만 LLM에게 전달되는 것이 아니라, 서버가 만든 지시문과 자료가 함께 전달된다.

오늘 이해한 핵심:

```text
프롬프트는 AI가 스스로 만든 생각이 아니라,
서버가 LLM에게 보내는 입력문이다.
```

## 3. 벡터 유사도 필터링

### 문제

RAG 검색은 무조건 상위 `topN=3` 문서를 가져오고 있었다.

문제는 관련성이 낮은 질문도 가장 가까운 문서 3개를 억지로 가져온다는 점이었다.

예:

```text
질문: 자바에 대한 검색 해줘
선택문서: Factory Pattern, Builder Pattern, Design Pattern Basic Notes #1
```

KnowledgeBase에 디자인 패턴 문서만 많기 때문에, 관련 없는 질문도 디자인 패턴 문서를 가져오는 문제가 있었다.

### 수정

`KnowledgeBaseService.vectorSearch()`에 최소 유사도 기준을 추가했다.

```java
private static final double MIN_VECTOR_SIMILARITY = 0.85;
```

흐름:

```text
질문 벡터 생성
→ 각 KnowledgeBase embedding과 cosine similarity 계산
→ similarity >= 0.85인 문서만 남김
→ 그중 topN 반환
```

또한 각 문서의 점수를 확인할 수 있도록 로그를 추가했다.

```text
[VECTOR_SCORE] topic=Builder Pattern similarity=0.5227(유사도)
```

### 결과

`스트림에 대해서 알려줘` 테스트에서 다음 결과가 나왔다.

```text
[VECTOR_SCORE] topic=Builder Pattern - 사용 시점 similarity=0.5356
[VECTOR_SCORE] topic=Factory Pattern similarity=0.5391
[VECTOR_SEARCH] minSimilarity=0.85 resultCount=0
```

관련 문서가 없으면 `resultCount=0`이 되도록 필터링이 정상 작동했다.

## 4. 검색 결과 없음 예외 처리

### 문제

처음에는 벡터 검색 결과가 0개여도 LLM 호출이 계속 진행됐다.

흐름:

```text
학습형 질문
→ 벡터 검색 resultCount=0
→ knowledgeDocs=[]
→ 그래도 LLM 호출
→ LLM이 일반 지식으로 아는 척 답변
```

이것이 할루시네이션 위험이었다.

### 수정

`ChatFlowService.generateAnswer()`에 예외 시나리오를 추가했다.

```text
학습형 질문
→ 검색 결과 0개
→ LLM 호출하지 않음
→ "저장된 관련 자료를 못 찾겠어. 키워드를 조금 더 구체적으로 말해줘."
```

### 결과

테스트 결과:

```text
[VECTOR_SEARCH] resultCount=0
[AI_EXCEPTION] type=KNOWLEDGE_NOT_FOUND(지식검색결과없음)
[LLM_REQUEST] 없음
```

응답:

```text
저장된 관련 자료를 못 찾겠어. 키워드를 조금 더 구체적으로 말해줘.
```

오늘의 중요한 깨달음:

```text
자료가 없으면 LLM에게 답변 기회를 주지 않는 것이 할루시네이션 방어다.
```

## 5. 대화형 예외 시나리오

학습형뿐 아니라 대화형에도 최소한의 예외 시나리오를 추가했다.

추가한 흐름:

```text
빈 질문
→ LLM 호출하지 않음
→ "질문이 비어 있어. 궁금한 내용을 한 문장으로 말해줘."
```

```text
너무 짧은 대화형 입력
→ LLM 호출하지 않음
→ "조금만 더 구체적으로 말해줘..."
```

목적:

- 불필요한 LLM 호출 방지
- 애매한 입력에 대해 명확한 재질문
- 비용과 응답 시간 절약

## 6. 선응답 후처리 실험

학습형/검색형 질문이 들어오면 검색 전에 먼저 선응답을 보내는 흐름을 추가했다.

선응답 문구:

```text
잠깐만, 관련 내용을 찾는 중이야.
```

로그:

```text
[AI_PRE_RESPONSE] message=잠깐만, 관련 내용을 찾는 중이야.(선응답)
```

현재 구조에서는 HTTP 응답을 두 번 보낼 수 없기 때문에, 기존 `VTuberWebSocketClient.sendTtsOnly()`를 통해 VTuber 쪽으로 선응답을 보내도록 했다.

테스트 중 WebSocket 서버가 꺼져 있으면 다음 오류가 발생했다.

```text
WebSocket 연결이 거부됨
ConnectException
```

하지만 메인 RAG 흐름은 실패하지 않도록 했다.

결론:

```text
선응답 실패는 전체 답변 실패로 이어지면 안 된다.
선응답은 사용자 경험 보조 기능이고, 메인 RAG 흐름과 분리되어야 한다.
```

## 7. 캐시 효과 확인

같은 질문을 두 번째로 요청하면 캐시가 적중했다.

첫 번째 요청:

```text
캐시미스
임베딩 실행
벡터 검색 실행
LLM 호출
전체 약 14초
```

두 번째 같은 요청:

```text
[AI_CACHE] hit=true(캐시적중)
[AI_TRACE_END] totalMs=22
```

결론:

```text
캐시는 운영에서는 매우 효과적이다.
하지만 테스트할 때는 새 로직이 실행되지 않게 만들 수 있다.
```

테스트 시에는 문장을 조금씩 바꿔 캐시를 피하는 방식으로 진행했다.

예:

```text
빌더 패턴은 언제 써?
빌더 패턴 사용 시점 검색해줘
생성자 값이 많을 때 쓰는 패턴 찾아줘
```

## 8. QueryDSL 1차 적용

오늘 배운 QueryDSL을 작은 범위에 적용했다.

### 적용한 이유

기존 JPQL은 문자열 기반이라 관계가 명확히 보이지 않았다.

기존 코드:

```java
SELECT m FROM ChatMessage m
WHERE m.sessionId.id = :sessionId
ORDER BY m.createdAt DESC
```

문제:

- `userId`를 파라미터로 받지만 실제 조건에 사용하지 않았다.
- `ChatMessage → Session → User` 관계가 코드에서 명확히 보이지 않았다.

### 수정

QueryDSL 의존성과 설정 추가:

```gradle
implementation "com.querydsl:querydsl-jpa:${querydslVersion}:jakarta"
annotationProcessor "com.querydsl:querydsl-apt:${querydslVersion}:jakarta"
```

`QueryDslConfig` 추가:

```java
@Bean
public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
    return new JPAQueryFactory(entityManager);
}
```

`ChatMessageRepositoryImpl.findRecent()`를 QueryDSL로 변경:

```java
return queryFactory
        .selectFrom(chatMessage)
        .where(
                chatMessage.sessionId.id.eq(sessionId),
                chatMessage.sessionId.user.id.eq(userId)
        )
        .orderBy(chatMessage.createdAt.desc())
        .limit(limit)
        .fetch();
```

결과:

```text
ChatMessage
→ Session
→ User
```

관계가 조회 조건에 명확히 드러났다.

## 9. 오늘 이해한 AI 시스템의 특징

일반 커머스 프로젝트와 AI/RAG 프로젝트는 성격이 다르다.

커머스:

```text
상품 ID 일치
재고 차감
결제 성공/실패
상태 변경
```

AI/RAG:

```text
질문 의도 추정
문서 유사도 판단
유사도 기준 조정
프롬프트 반응 예측
LLM 답변 통제
```

커머스는 결정적 로직이 많고, AI는 확률적 판단이 많다.

오늘의 깨달음:

```text
AI 시스템은 정답이 고정된 시스템이 아니라,
로그와 테스트를 통해 계속 기준을 맞춰가는 시스템이다.
```

## 10. 오늘 해결한 것

오늘 해결한 핵심:

```text
1. 프롬프트와 토큰 제한으로 장문 답변 감소
2. 유사도 기준 0.85 적용
3. 벡터 검색 점수 로그 추가
4. 검색 결과 없음 예외 처리
5. 자료 없음 시 LLM 호출 차단
6. 할루시네이션 방어
7. 선응답 후처리 실험
8. 캐시 효과 확인
9. QueryDSL 1차 적용
```

## 11. 아직 남은 것

다음 작업 후보:

```text
1. 정상 질문/비정상 질문 테스트 세트 정리
2. similarity 기준값 데이터 기반 조정
3. 선응답 WebSocket 안정화
4. 캐시 전략 고도화
5. Semantic Cache 실험
6. Prefix Cache 검토
7. 실제 사용자/세션 기반 RAG API 승격
8. VTuber 연결 상태 확인 후 선응답 UX 검증
```

## 오늘의 핵심 문장

```text
AI는 정답을 한 번에 맞히는 시스템이 아니라,
불확실한 판단을 로그와 예외 시나리오로 통제해 가는 시스템이다.
```

```text
자료가 없으면 답하지 않는다.
이 단순한 예외 시나리오가 할루시네이션 방어의 시작이다.
```
