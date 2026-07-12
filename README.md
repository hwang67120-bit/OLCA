# Open-LLM-Coding-Assistant (OLCA)

> AI 응답을 개발 흐름 안에서 검증 가능하게 다루기 위한 Java/Spring 기반 개인 개발 조수

OLCA는 단순히 LLM을 호출하는 챗봇이 아니라, 공식 문서를 지식 베이스에 적재하고 RAG 검색, 리랭킹, 평가셋, 검증용 Docker 샌드박스를 통해 답변 근거와 검색 품질을 확인하는 백엔드 프로젝트입니다.

## 핵심 결과

```text
RAG Evaluation
- total=47
- top1_pass=47/47 (100.0%)
- top3_pass=47/47 (100.0%)
- verification: scripts/dev/verify-rag.sh pass
```

```text
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

## 왜 만들었나

처음 목표는 반복 질문과 토큰 비용을 줄이는 것이었습니다. 매번 같은 내용을 다시 물어보고, 이미 찾아본 자료를 다시 설명시키는 과정이 비효율적으로 느껴졌습니다.

사용할수록 단순한 비용 절감보다 더 큰 문제가 보였습니다. 외워야 할 내용과 다시 찾아야 할 자료가 늘어나면서, 개인 지식 베이스와 연결된 개발 보조 도구가 필요해졌습니다.

그래서 OLCA는 질문을 먼저 분류하고, 학습형 질문만 RAG 검색으로 연결하며, 검색 결과는 리랭킹과 평가셋으로 검증하는 방향으로 발전했습니다. 목표는 AI 답변을 많이 생성하는 것이 아니라, 개발자가 확인하고 다시 실행해볼 수 있는 답변 흐름을 만드는 것입니다.

## 시스템 흐름

```text
사용자 질문
-> 질문 라우팅
-> 학습형 질문이면 RAG 검색
-> KnowledgeBase vector search
-> reranking
-> 평가셋으로 검색 품질 검증
-> 검증 가능한 답변 흐름 구성
```

## 기술 스택

- Java 21
- Spring Boot
- Spring Web / WebFlux
- MongoDB
- MySQL / JPA
- Ollama Embedding
- Python 평가 스크립트
- Docker 검증 샌드박스

## 주요 구현

### RAG 검색

공식 문서를 MongoDB `KnowledgeBase`에 저장하고, 문서 내용을 embedding vector로 변환해 질문 vector와 의미 유사도를 비교합니다.

초기에는 벡터 유사도만으로 검색 결과를 정렬했지만, 문서가 늘어나면서 같은 키워드가 다른 문맥에서 사용되는 문제가 발생했습니다. 이를 해결하기 위해 리랭킹 레이어를 추가했습니다.

### Reranking

최종 검색 점수는 단순 vector score가 아니라 여러 보정 점수를 함께 사용합니다.

```text
finalScore
= vectorScore
+ keywordScore
+ topicScore
+ sourceTrustScore
- domainPenalty
```

사용한 보정 기준은 다음과 같습니다.

- 핵심 intent keyword 기반 boost
- 공식 문서 source trust boost
- unknown pattern guard
- Java Collection 전용 scorer
- Java language feature 전용 scorer
- metadata context scorer
- domain mismatch penalty

### KnowledgeBase metadata

공식 문서가 늘어나면서 `List`, `Map`, `Stream`, `Generics`처럼 같은 단어가 다른 의미로 쓰이는 문제가 생겼습니다. 문자열 기반 scorer만으로는 문맥을 계속 하드코딩하게 될 위험이 있어, `KnowledgeBase`에 metadata를 추가했습니다.

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

metadata는 문서의 출처와 문맥을 구조화하고, 리랭킹에서 질문 의도와 후보 문서의 주제가 맞는지 판단하는 보조 기준으로 사용합니다.

## RAG 품질 회복 기록

### Collections 추가 후 품질 하락

Java Collections 공식 문서를 추가한 뒤 검색 공간이 넓어지면서 top1 정확도가 하락했습니다.

```text
Collections 추가 직후
- total=32
- top1_pass=27/32 (84.4%)
- top3_pass=28/32 (87.5%)
```

주요 원인은 `List`, `Set`, `Map` 질문이 디자인패턴 문서로 밀리거나, 알 수 없는 패턴 질문이 Collection 문서와 잘못 연결되는 것이었습니다.

보정 후 결과는 다음과 같습니다.

```text
RAG Evaluation
- total=32
- top1_pass=32/32 (100.0%)
- top3_pass=32/32 (100.0%)
```

### 공식 문서 5개 추가 후 문맥 충돌

다음 공식 문서를 추가로 적재했습니다.

- Java Official - Generics
- Java Official - Enum Types
- Java Official - Annotations
- Java Official - Lambda Expressions
- Java Official - Stream Aggregate Operations

확장 직후에는 `List<String>` 질문이 `List Interface`로 밀리고, `filter map collect` 질문이 `Map Interface`로 밀리는 문제가 발생했습니다. 이는 문서 내용의 문제가 아니라 같은 키워드가 제네릭, 컬렉션, 스트림 문맥에서 다르게 쓰이는 문제였습니다.

`JavaLanguageFeatureScorer`, `MetadataContextScorer`, token 단위 판정을 추가해 최종적으로 47개 평가셋을 모두 통과했습니다.

```text
Official Java docs 확장 후 RAG Evaluation
- total=47
- top1_pass=47/47 (100.0%)
- top3_pass=47/47 (100.0%)
```

## 검증용 Docker 샌드박스

OLCA의 Docker 구성은 배포용 Docker가 아닙니다. Spring Boot 애플리케이션, MongoDB, Ollama를 모두 컨테이너로 묶어 실행하는 배포 구성은 아직 포함하지 않았습니다.

현재 Docker는 이미 실행 중인 OLCA 서버를 대상으로 검증 스크립트를 컨테이너 내부에서 다시 실행하는 검증 재현용 샌드박스입니다. 목적은 내 로컬 셸 환경에만 의존하지 않고, 같은 검증 명령을 별도 컨테이너 환경에서 재확인하는 것입니다.

```bash
bash scripts/dev/verify-in-docker.sh all
bash scripts/dev/verify-in-docker.sh backend
bash scripts/dev/verify-in-docker.sh rag
```

전제 조건은 다음과 같습니다.

- OLCA 서버가 실행 중이어야 함
- MongoDB에 평가 대상 지식 문서가 적재되어 있어야 함
- Ollama embedding 서버가 실행 중이어야 함
- Docker 컨테이너가 `OLCA_BASE_URL`에 접근 가능해야 함

검증 결과는 `verification-runs/` 아래에 저장됩니다.

```text
verification-runs/<runId>/
  docker-build.log
  stdout.log
  stderr.log
  summary.txt
  result.json
verification-runs/latest-run.txt
```

자세한 내용은 [AI Verification Sandbox](docs/ai-verification-sandbox.md)를 참고합니다.

## 문서

- [AI Verification Sandbox](docs/ai-verification-sandbox.md)
- [RAG reranking troubleshooting](docs/troubleshooting/rag-reranking-summary.md)
- [RAG metadata context troubleshooting](docs/troubleshooting/rag-metadata-context.md)
- [RAG Java basic evaluation](docs/evaluation/rag-java-basic-eval.md)

## 개발 로드맵

### Phase 0: 프로젝트 기반 정리 완료

- Java/Spring 기반 OLCA 백엔드 구성
- MongoDB 기반 KnowledgeBase 저장 구조 구성
- Ollama 기반 로컬 LLM/Embedding 흐름 연결

### Phase 1: 질문 라우팅과 RAG 파이프라인 완료

- 학습형 질문만 RAG 검색으로 연결
- 자료가 부족한 경우 억지 답변 생성을 막는 guard 구성
- KnowledgeBase vector search API 구성

### Phase 2: 리랭킹과 검색 품질 보정 완료

- vector similarity 기반 후보 검색
- keyword/topic/sourceTrust/domainPenalty 기반 reranking
- unknown pattern guard 확장
- Java Collection / Java language feature scorer 추가
- 공통 노이즈 키워드와 핵심 intent 키워드 분리

### Phase 3: 평가셋과 회귀 검증 완료

- `scripts/knowledge/rag_eval_cases.json` 평가셋 구성
- `scripts/knowledge/evaluate-rag.py` 평가 스크립트 구성
- 최종 RAG 평가 `top1_pass=47/47`, `top3_pass=47/47` 확인

### Phase 4: 자동 검증과 컨테이너 증적 완료

- `scripts/dev/verify-rag.sh` 구성
- `scripts/dev/verify-all.sh` 구성
- Docker 기반 검증 샌드박스 구성
- 검증 Run ID 기록: `20260704T165438Z-all`

### Phase 5: 공식 문서 확장과 metadata context 완료

- Java 공식 문서 5개 추가 적재
- 평가셋을 32개에서 47개로 확장
- KnowledgeBase metadata 도입
- metadata context scorer 추가
- metadata 도입 후 `collection`/`collect` 부분 문자열 오인 문제 수정

## 후속 개선 과제

- 공식 문서, 개인 노트, 실험용 데이터 분리 정책 강화
- 문자열 기반 scorer를 metadata 기반 reranking으로 점진적 전환
- Query expansion ON/OFF 결과를 검증 리포트에 함께 기록
- 평가셋 확장 시 top1/top3 변화 추적 자동화
- 배포용 Dockerfile 및 docker-compose 구성

## 라이선스

MIT
