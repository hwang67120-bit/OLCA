# 2026-06-21 TIL - AI Sandbox Verification과 검증 기반 개발 흐름

## 오늘의 목표

OLCA와 Open-LLM-VTuber를 연동하면서 AI가 생성한 코드와 답변을 더 안전하게 다루는 방법을 정리했다.

현재 프로젝트는 단순히 LLM에게 질문하고 답변을 받는 구조가 아니다.

```text
사용자 질문
-> 의도 라우팅
-> RAG 검색
-> 재랭킹
-> 캐시 정책
-> LLM 호출
-> TTS/프론트 전달
-> 실제 사용 검증
```

이 흐름 안에서 AI가 만든 코드나 학습 자료를 그대로 신뢰하면 위험하다.

특히 로컬 LLM은 다음 한계를 가진다.

- 프로젝트 전체 구조를 항상 정확히 이해하지 못한다.
- 최신 라이브러리나 설정 차이를 잘못 추론할 수 있다.
- 컴파일하지 않은 코드를 그럴듯하게 제시할 수 있다.
- 실행 환경, 포트, 환경변수, DB 상태 같은 외부 조건을 놓칠 수 있다.

따라서 앞으로의 목표는 AI를 정답 생성기로 보는 것이 아니라, 검증 가능한 개발 조수로 만드는 것이다.

## 배경

이번 작업 중 실제로 여러 문제가 발생했다.

```text
OLLAMA_BASE_URL이 VTuber 서버 포트(12393)를 바라봄
-> OLCA가 Ollama가 아닌 FastAPI 서버에 /api/chat 요청
-> 405 Method Not Allowed
-> OLCA 500 오류
```

또 다른 문제도 있었다.

```text
QueryDslConfig.java의 클래스 닫는 중괄호 누락
PowerShell 저장 과정에서 BOM 문자 삽입
프론트 WebSocket 기본 URL이 127.0.0.1로 고정
같은 WebSocket URL 재연결로 기존 연결 종료
```

이 문제들은 LLM이 코드 조각만 생성해서는 알기 어렵다.

결국 코드가 맞는지 판단하려면 실제 실행과 검증이 필요하다.

## 핵심 문제 의식

AI가 제시한 코드는 다음을 보장하지 않는다.

```text
100% 컴파일된다
100% 실행된다
100% 좋은 설계다
100% 현재 프로젝트 환경과 맞다
```

따라서 앞으로는 다음 기준이 필요하다.

```text
실행하지 않은 코드는 확정 답변으로 말하지 않는다.
검증되지 않은 변경은 실제 프로젝트에 반영하지 않는다.
```

## 개념 1: Sandbox Execution

Sandbox Execution은 AI가 만든 코드를 실제 프로젝트에 바로 반영하지 않고, 격리된 실행 환경에서 먼저 검증하는 방식이다.

```text
AI 코드 생성
-> 컨테이너 또는 임시 작업공간에 적용
-> compile/test/eval 실행
-> 성공한 변경만 실제 프로젝트에 반영
```

컨테이너는 AI에게 실험실 역할을 한다.

- 실제 실행 환경을 만들 수 있다.
- 프로젝트 원본을 망가뜨리지 않는다.
- 실패와 성공이 명확하다.
- 매번 깨끗한 상태에서 검증할 수 있다.
- 런타임 오류를 직접 확인할 수 있다.

## 개념 2: Verification Gate

Verification Gate는 AI가 만든 결과물이 정해진 검증 문을 통과해야 다음 단계로 넘어가도록 하는 구조다.

```text
AI 생성물
-> compile
-> test
-> eval
-> 통과
-> 반영
```

OLCA에서는 다음 검증 게이트를 둘 수 있다.

```text
Java 코드 수정
-> ./gradlew compileJava

RAG 로직 수정
-> python3 scripts/knowledge/evaluate-rag.py

프론트 수정
-> npm run build:web

통합 흐름 수정
-> VTuber 실제 채팅 테스트
```

검증 게이트의 핵심은 AI의 설명이 아니라 실행 결과를 기준으로 판단하는 것이다.

## 개념 3: Risk-Based Validation

모든 작업을 같은 강도로 검증하면 너무 느려진다.

그래서 요청의 위험도에 따라 검증 깊이를 다르게 해야 한다.

```text
문법 질문
-> 설명 중심, 실행 검증 생략 가능

작은 메서드 수정
-> compile 또는 관련 단위 테스트

RAG 검색 정책 수정
-> compile + 실패 케이스 평가

프론트 수정
-> build + 화면 확인

배포 전 변경
-> 전체 검증
```

즉 항상 전체 테스트를 돌리는 것이 아니라, 위험도에 맞는 검증을 선택한다.

## 개념 4: Test Pyramid

Test Pyramid는 테스트를 빠른 것부터 느린 것까지 계층화하는 개념이다.

OLCA에서는 다음처럼 적용할 수 있다.

```text
빠른 검증
- compileJava
- smoke RAG eval 5개

표준 검증
- targeted eval 실패 케이스
- 주요 API curl 테스트

전체 검증
- full RAG eval 50개
- 프론트 build
- WebSocket 실제 흐름 테스트
```

이 구조를 사용하면 안정성과 속도를 함께 관리할 수 있다.

## 개념 5: Regression Testing

Regression Testing은 기존에 되던 기능이 새 수정 때문에 깨지지 않았는지 확인하는 방식이다.

이번 RAG 평가셋이 이 역할을 한다.

```text
기존 평가셋 20개
-> RAG 정책 수정
-> 다시 평가
-> 20/20 통과 확인
```

수정 전에는 다음 실패가 있었다.

```text
throw/throws 질문이 Packages로 감
팩토리 사용 시점 질문이 기본 Factory 문서로 감
싱글톤 사용 시점 질문이 기본 Singleton 문서로 감
스트림 고블린 패턴이 Factory Pattern으로 감
```

수정 후 결과는 다음과 같았다.

```text
Top1: 20/20 = 100%
Top3: 20/20 = 100%
```

이 결과는 전체 AI 품질이 100%라는 뜻이 아니라, 현재 정의한 회귀 평가셋을 모두 통과했다는 뜻이다.

## 개념 6: Golden Dataset

Golden Dataset은 정답 기준이 정해진 평가 데이터셋이다.

현재 OLCA의 작은 Golden Dataset은 다음 파일이다.

```text
scripts/knowledge/rag_eval_cases.json
```

이 파일은 다음을 정의한다.

- 질문
- 기대 top1 문서
- 허용 top3 문서
- 질문 유형

예:

```json
{
  "id": "unknown-001",
  "question": "스트림 고블린 패턴 알려줘",
  "expectedTop1": "NONE",
  "allowedTop3": ["NONE"],
  "type": "unknown"
}
```

Golden Dataset은 품질을 보장하는 장치라기보다, 품질이 나빠졌는지 감지하는 안전망이다.

## 개념 7: Eval Harness

Eval Harness는 평가셋을 자동으로 실행하고 점수를 계산하는 장치다.

현재 OLCA에서는 다음 스크립트가 그 역할을 한다.

```text
scripts/knowledge/evaluate-rag.py
```

실행 흐름:

```text
rag_eval_cases.json 읽기
-> /api/knowledge/vector-search 호출
-> top1/top3 비교
-> PASS/FAIL 출력
-> 통과율 계산
```

이 구조 덕분에 RAG 수정 후 감으로 판단하지 않고 수치로 확인할 수 있다.

## 개념 8: Staging / Production 분리

학습 자료를 바로 운영 지식에 넣으면 검색 오염이 생길 수 있다.

따라서 장기적으로는 지식 저장소를 다음처럼 분리할 수 있다.

```text
staging knowledge
-> embedding 생성
-> eval 실행
-> 통과
-> production knowledge 반영
```

이 구조가 있으면 새 학습 자료가 기존 검색 품질을 망가뜨리는지 먼저 확인할 수 있다.

예:

```text
새 Java 공식 문서 추가
-> 평가셋 실행
-> unknown 질문이 다시 오답으로 바뀌는지 확인
-> 통과하면 운영 지식으로 승격
```

## 개념 9: Human-in-the-Loop

Human-in-the-Loop은 AI가 모든 결정을 자동으로 하지 않고, 중요한 반영은 사람이 승인하는 구조다.

OLCA의 개발 원칙은 다음과 맞다.

```text
AI가 문제 시나리오를 설명한다.
사용자가 승인한다.
AI가 코드를 수정한다.
AI 또는 사용자가 검증을 실행한다.
검증 결과를 확인한다.
성공한 변경만 반영한다.
```

이 방식은 속도는 조금 느리지만, 프로젝트 안정성을 높인다.

## 속도 문제와 해결 방향

검증을 넣으면 당연히 시간이 늘어난다.

따라서 다음 세 가지 모드를 둘 수 있다.

```text
빠른 답변 모드
- 개념 설명
- 문법 힌트
- 실행 검증 없음

검증 모드
- 코드 작성
- compile/test 실행
- 실패 시 최대 3회 수정

전체 안전 모드
- compile
- RAG full eval
- 프론트 build
- 통합 흐름 확인
```

무한 반복은 위험하다.

```text
돌아갈 때까지 반복
```

보다는 다음 방식이 안전하다.

```text
최대 3회 자동 수정
-> 실패 시 로그와 원인 후보 보고
-> 사용자 판단 요청
```

## OLCA에 적용할 수 있는 검증 파이프라인

장기적으로 다음 구조를 목표로 할 수 있다.

```text
사용자 요청
-> AI가 수정 시나리오 작성
-> 컨테이너 작업공간 생성
-> 코드 수정
-> compile/test/eval 실행
-> 성공하면 diff 보고
-> 사용자 승인
-> 실제 프로젝트 반영
```

프로젝트별 검증 명령은 다음과 같이 나눌 수 있다.

```text
OLCA Backend
- ./gradlew compileJava
- python3 scripts/knowledge/evaluate-rag.py

Open-LLM-VTuber Frontend
- npm run build:web
- WebSocket 연결 확인

TTS / VTuber 흐름
- 실제 채팅 입력
- Java API response 확인
- backend-synth-complete 확인
```

## 포트폴리오 설명 문장

이번 개념은 다음 문장으로 정리할 수 있다.

```text
LLM/RAG 기반 개인 개발 조수에 sandbox execution, risk-based validation, regression eval gate를 적용해 AI 응답과 코드 변경의 신뢰도를 높이는 구조를 설계하고 있다.
```

조금 더 실무적으로 쓰면 다음과 같다.

```text
AI가 생성한 코드와 지식 자료를 바로 신뢰하지 않고, 격리된 검증 환경과 평가셋 기반 gate를 통해 compile/test/eval을 통과한 결과만 반영하는 구조를 설계했다.
```

## 오늘의 결론

AI가 코드를 제시한다고 해서 그 코드가 실제로 돌아간다는 보장은 없다.

따라서 OLCA의 다음 방향은 단순한 AI 답변 생성이 아니라, 검증 가능한 AI 개발 조수를 만드는 것이다.

핵심 원칙은 다음과 같다.

```text
AI의 말이 아니라 실행 결과를 믿는다.
검증되지 않은 변경은 반영하지 않는다.
학습 자료는 평가셋을 통과한 뒤 운영 지식으로 승격한다.
검증 깊이는 위험도에 따라 조절한다.
```

이 구조는 프로젝트가 커질수록 중요해진다.

현재 OLCA는 RAG 평가셋 20개 기준 100%를 통과했지만, 이것은 끝이 아니라 검증 체계의 시작점이다.
## 검증 오케스트레이션 적용 방향

컨테이너 검증은 새로운 판단 로직을 만드는 기능이 아니다.

기존에 이미 검증 기준으로 사용하던 기능을 재사용해서, 격리된 환경에서도 같은 기준으로 변경 사항을 확인하는 오케스트레이션 계층이다.

```text
기존 기능
- Gradle compile
- RAG 평가셋
- API health check
- 프론트 build
- 실제 WebSocket 흐름 테스트

검증 오케스트레이션
- 위 기능을 순서대로 실행
- 실패한 단계와 로그를 보고
- 성공한 변경만 반영 후보로 올림
```

이 방식의 목적은 기존 로직을 다시 만드는 것이 아니라, 기존 로직이 계속 안전하게 동작하는지 확인하는 것이다.

따라서 첫 단계는 컨테이너를 바로 붙이는 것이 아니라, 로컬과 컨테이너에서 공통으로 사용할 수 있는 검증 스크립트를 만드는 것이다.

```text
scripts/dev/verify-backend.sh
-> ./gradlew compileJava 재사용

scripts/dev/verify-rag.sh
-> scripts/knowledge/evaluate-rag.py 재사용

scripts/dev/verify-all.sh
-> backend 검증과 RAG 검증을 순차 실행
```

이 구조를 사용하면 나중에 컨테이너를 추가하더라도 검증 기준을 새로 만들 필요가 없다.

```text
로컬 실행
-> scripts/dev/verify-all.sh

컨테이너 실행
-> 같은 scripts/dev/verify-all.sh 실행

CI 실행
-> 같은 scripts/dev/verify-all.sh 실행
```

핵심 원칙은 다음과 같다.

```text
검증 로직은 제품 로직에 침투하지 않는다.
검증 기준은 기존 명령과 평가셋을 재사용한다.
컨테이너는 새로운 판단자가 아니라 격리된 실행 장소다.
```
