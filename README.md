# Open-LLM-Coding-Assistant

> 개발자를 위한 AI 코딩 어시스턴트

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

### Phase 0: 기획 ✅
- [x] API 명세서
- [x] ERD 설계
- [x] 기술 스택 정리
- [x] 로직 흐름 설계
- [x] 도메인 기능 명세
- [x] README 작성

### Phase 1: 구현
- [x] 도메인 Entity
- [x] Repository
- [ ] Service (진행 중)
- [ ] Controller
- [ ] DTO

### Phase 2: 테스트
- [ ] 단위 테스트
- [ ] 통합 테스트
- [ ] Postman 테스트
- [ ] 리팩토링

### Phase 3: 고급
- [ ] Config
- [ ] JWT 보안
- [ ] AOP
- [ ] 전체 테스트

### Phase 4: 배포
- [ ] Docker
- [ ] EC2 배포
- [ ] MySQL/MongoDB 연결
- [ ] 모니터링

## 🔗 연동

- **Python 서버**: Open-LLM-VTuber
- **AI 모델**: Ollama (localhost:11434)

## 📌 설계 원칙

✅ **빠르고 간단하게 시작**  
✅ **확장 가능한 구조**  
✅ **유지보수 쉬운 코드**

## 🎯 목표

**실사용 가능한 개발자 AI 어시스턴트 (3~4개월)**

## 👨‍💻 개발자

매아

## 📄 라이선스

MIT
# OLCA Development
