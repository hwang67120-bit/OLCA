# Open-LLM-Coding-Assistant

> AI 응답을 개발 흐름 안에서 검증 가능하게 다루기 위한 개인 개발 조수

## 왜 만들었나

처음 목표는 거창한 AI 시스템이 아니라, 반복 질문과 토큰 비용을 줄이는 것이었습니다.  
매번 같은 내용을 다시 물어보고, 이미 찾아본 자료를 다시 설명시키는 과정이 비효율적으로 느껴졌습니다.

사용할수록 단순한 비용 절감보다 더 큰 문제가 보였습니다.  
외워야 할 내용과 다시 찾아야 할 자료가 늘어나면서, 개인 지식 베이스와 연결된 개발 보조 도구가 필요해졌습니다.

그 다음에는 답변을 받는 것만으로는 부족했습니다.  
아이디어를 정리하고, 설계를 비교하고, 막힌 지점에서 브레인스토밍을 도와주는 조수가 필요했습니다.

하지만 AI 답변은 자료가 부족할 때도 그럴듯하게 이어질 수 있습니다.  
그래서 OLCA는 단순한 챗봇이 아니라, 질문 라우팅, RAG 검색, 리랭킹, 평가셋, Docker 검증을 통해 답변 흐름을 확인할 수 있는 개발 조수로 확장되었습니다.

## 검증 가능한 AI 개발 조수

OLCA는 AI를 개발자의 작업 흐름 안에서 더 다루기 쉽게 만들기 위한 프로젝트입니다.  
개발자가 AI 응답을 실제 작업에 사용할 때, 그 답변이 어떤 근거와 검증 절차를 거쳤는지 확인할 수 있도록 만든 실험입니다.

LLM은 강력하지만, 질문의 의도나 자료의 유무를 항상 안정적으로 구분하지는 못합니다.  
그래서 OLCA는 질문을 먼저 라우팅하고, 학습형 질문만 RAG 검색으로 연결하며, 검색 결과는 리랭킹과 평가셋으로 검증합니다. 자료가 부족한 경우에는 억지로 답변을 만들기보다, 답변 가능한 범위를 분리하는 방향을 선택했습니다.

이 프로젝트의 목표는 응답을 많이 생성하는 것이 아니라, 개발자가 확인하고 다시 실행해볼 수 있는 답변 흐름을 만드는 것입니다.

## RAG 데이터 확장 후 검색 품질 회복

Java Collections 공식 문서를 추가한 뒤, 기존 RAG 평가셋의 검색 공간이 넓어지면서 top1 정확도가 하락했습니다.

```text
Collections 추가 직후
- total=32
- top1_pass=27/32 (84.4%)
- top3_pass=28/32 (87.5%)
```

주요 실패는 `List`, `Set`, `Map` 질문이 디자인패턴 문서로 밀리거나, 알 수 없는 패턴 질문이 Collection 문서와 잘못 연결되는 형태였습니다. 원인은 단순히 문서가 부족한 것이 아니라, 리랭킹 레이어에서 공통 도메인 키워드와 핵심 intent 키워드를 충분히 분리하지 못한 데 있었습니다.

보정 과정에서는 다음을 적용했습니다.

- `UsageIntentScorer`가 디자인패턴 질문에만 사용 시점 문서를 boost하도록 범위를 제한
- 알 수 없는 패턴 질문이 모든 후보에서 강하게 낮아지도록 guard 확장
- `JavaCollectionScorer`로 `List`, `Set`, `Map`, `Collection` 질문의 전용 intent 보정 추가
- Query expansion을 Spring property로 켜고 끌 수 있게 분리
- 리랭킹에서 `java`, `official`, `oracle` 같은 공통 노이즈 키워드는 제외하고 핵심 intent 키워드만 점수화
- 언어 차원의 Java interface 질문과 Collections interface 문서가 섞이지 않도록 domain penalty 보정

최종 검증 결과는 다음과 같습니다.

```text
RAG Evaluation
- total=32
- top1_pass=32/32 (100.0%)
- top3_pass=32/32 (100.0%)

AI Verification Sandbox
- Run ID: 20260704T165438Z-all
- Result: pass
- Exit code: 0
- Mode: all
- Script: scripts/dev/verify-all.sh
- Image: olca-verifier:local
- Dockerfile: scripts/dev/ai-verification-sandbox.Dockerfile
- Network: host
```

검증 증적은 다음 위치에 남습니다.

```text
verification-runs/20260704T165438Z-all/result.json
verification-runs/20260704T165438Z-all/stdout.log
verification-runs/20260704T165438Z-all/stderr.log
verification-runs/20260704T165438Z-all/docker-build.log
```

## 📋 프로젝트 개요

Open-LLM-VTuber의 성능을 향상시키고 더 쉬운 LLM 통합을 위한 Java 기반 백엔드 파이프라인

## 🎯 주요 기능

### 3가지 시나리오

1. **외부 검색** - MongoDB에서 과거 대화 검색
2. **일반 AI** - Ollama 직접 호출로 AI 답변
3. **하이브리드 (RAG)** - 과거 대화 컨텍스트 기반 AI 답변

## 🛠️ 기술 스택

### Backend
- Java 21
- Spring Boot 4.0.6
- Spring Web
- Spring WebSocket
- WebClient (WebFlux)

### Database
- MySQL (JPA) - 실시간 대화
- MongoDB - 검색 인덱스
- H2 (개발용)

### AI
- Ollama (로컬 LLM)

### Tools
- Lombok
- Validation
- JUnit

## 📊 ERD

### MySQL (JPA)
- User
- Session
- ChatMessage
- Tag
- ChatTag (중간 테이블)

### MongoDB
- KnowledgeBase (검색용)

## 🔄 로직 흐름
Start → 사용자 질문 → 타입 분석 → [검색/AI/RAG] → 응답 → End

## 📝 API 명세

### 1순위: 대화 저장
POST /api/chat
Request: { question, answer }
Response: { id, saved }

### 2순위: 대화 검색
GET /api/search?q=키워드
Response: [ {question, answer, timestamp} ]

### 3순위: AI 응답
POST /api/ask
Request: { question }
Response: { answer, sources }

## 💼 도메인 기능 명세

### **User (회원)**

**Command:**
- 회원가입 (username)

**Query:**
- ID로 사용자 조회
- username으로 사용자 조회 (중복 체크)

---

### **Session (채팅방)**

**Command:**
- 새 채팅방 생성 (title, userId)

**Query:**
- 사용자별 채팅방 목록 조회
- 채팅방 ID로 조회

**특징:**
- Stateless (각 Session 독립)
- 나중에 Stateful 확장 가능

---

### **ChatMessage (대화 내용)**

**Command:**
- 없음 (다른 서비스에서 저장)

**Query:**
- Session별 대화 목록 조회
- 키워드 검색

**기술:**
- JPA (MySQL)
- 실시간 성능 테스트 예정

---

### **Tag (태그)**

**Command:**
- 태그 생성 (name)
- 대화에 태그 연결 (chatMessageId, tagId)

**Query:**
- 태그별 대화 조회
- 전체 태그 목록

**방식:**
- 수동 태그 입력
- AI 자동 분류는 Phase 2

---

### **KnowledgeBase (지식 베이스 - MongoDB)**

**Command:**
- 자주 묻는 질문 저장
- 키워드 인덱싱

**Query:**
- 키워드 기반 실시간 검색
- 관련 대화 찾기

**기술:**
- MongoDB 전문 검색
- 빠른 캐싱

---

## 🚀 개발 로드맵

### Phase 0: 프로젝트 기반 정리 ✅
- [x] Java/Spring 기반 OLCA 백엔드 구성
- [x] MySQL/JPA 도메인 초안 구성
- [x] MongoDB 기반 KnowledgeBase 저장 구조 구성
- [x] Open-LLM-VTuber 연동 방향 정리
- [x] Ollama 기반 로컬 LLM/Embedding 흐름 연결

### Phase 1: 질문 라우팅과 RAG 파이프라인 ✅
- [x] 대화형/학습형/자료 없음 질문 라우팅
- [x] 학습형 질문만 RAG 검색으로 연결
- [x] 자료가 부족한 경우 억지 답변 생성을 막는 guard 구성
- [x] Query expansion 적용 및 `olca.rag.query-expansion.enabled` 옵션 추가
- [x] KnowledgeBase vector search API 구성

### Phase 2: 리랭킹과 검색 품질 보정 ✅
- [x] vector similarity 기반 후보 검색
- [x] keyword/topic/sourceTrust/domainPenalty 기반 reranking
- [x] 디자인패턴 사용 시점 boost 범위 제한
- [x] unknown pattern guard 확장
- [x] Java Collection 전용 scorer 추가
- [x] 공통 노이즈 키워드와 핵심 intent 키워드 분리
- [x] Java language interface와 Collections interface 도메인 경계 보정

### Phase 3: 평가셋과 회귀 검증 ✅
- [x] `scripts/knowledge/rag_eval_cases.json` 평가셋 구성
- [x] Java basic, design pattern, unknown, Java collection 케이스 포함
- [x] `scripts/knowledge/evaluate-rag.py` 평가 스크립트 구성
- [x] Collections 추가 후 하락한 검색 품질 회복
- [x] 최종 RAG 평가 `top1_pass=32/32`, `top3_pass=32/32` 확인

### Phase 4: 자동 검증과 컨테이너 증적 ✅
- [x] `scripts/dev/verify-rag.sh` 구성
- [x] `scripts/dev/verify-all.sh` 구성
- [x] Docker 기반 `verify-in-docker.sh all` 검증
- [x] `verification-evidence.py assert-pass` 증적 확인
- [x] 검증 Run ID 기록: `20260704T165438Z-all`

### Phase 5: 다음 개선 과제
- [ ] KnowledgeBase metadata 도입 검토 (`domain`, `category`, `topicKey`, `source`)
- [ ] 문자열 기반 scorer를 metadata 기반 reranking으로 점진적 전환
- [ ] Query expansion ON/OFF 결과를 검증 리포트에 함께 기록
- [ ] 평가셋 확장 시 top1/top3 변화 추적 자동화
- [ ] README 구조를 기능 목록보다 문제 해결 서사 중심으로 계속 정리

## 🔗 연동

- **Python 서버**: Open-LLM-VTuber
- **AI 모델**: Ollama (localhost:11434)

## 📌 설계 원칙

✅ **빠르고 간단하게 시작**  
✅ **확장 가능한 구조**  
✅ **유지보수 쉬운 코드**

## 🎯 목표

**실사용 가능한 개발자 AI 어시스턴트 (3~4개월)**



## 📄 라이선스

MIT
# OLCA Development
