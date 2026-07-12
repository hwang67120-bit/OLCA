# AI Verification Sandbox

OLCA의 Docker 구성은 배포용 Docker가 아니라 검증 재현용 샌드박스입니다.

이 샌드박스는 Spring Boot 애플리케이션, MongoDB, Ollama를 모두 컨테이너로 묶어 실행하지 않습니다. 대신 이미 실행 중인 OLCA 서버를 대상으로 기존 검증 스크립트를 컨테이너 내부에서 다시 실행합니다.

## 목적

- 제품 로직은 변경하지 않는다.
- 기존 검증 스크립트(`verify-backend.sh`, `verify-rag.sh`, `verify-all.sh`)를 재사용한다.
- 로컬 셸 환경에만 의존하지 않고 컨테이너 환경에서 검증 명령을 다시 실행한다.
- 실행 결과와 로그를 `verification-runs/`에 남긴다.

## 검증 범위

```text
verify-in-docker.sh
-> Docker image build
-> selected verify script 실행
-> stdout/stderr/result.json/summary.txt 저장
```

모드별 실행 스크립트는 다음과 같습니다.

```text
all     -> scripts/dev/verify-all.sh
backend -> scripts/dev/verify-backend.sh
rag     -> scripts/dev/verify-rag.sh
```

## 실행 방법

```bash
bash scripts/dev/verify-in-docker.sh all
bash scripts/dev/verify-in-docker.sh backend
bash scripts/dev/verify-in-docker.sh rag
```

최근 검증 결과는 다음 명령으로 확인합니다.

```bash
python3 scripts/dev/verification-evidence.py latest
python3 scripts/dev/verification-evidence.py assert-pass
```

## 전제 조건

이 Docker 샌드박스는 독립 실행 배포 환경이 아니므로 다음 전제가 필요합니다.

- OLCA 서버가 실행 중이어야 한다.
- `OLCA_BASE_URL`로 서버에 접근 가능해야 한다.
- RAG 검증을 실행하려면 MongoDB에 평가 대상 지식 문서가 적재되어 있어야 한다.
- embedding을 사용하는 검증에서는 Ollama 서버가 실행 중이어야 한다.
- 기본 네트워크 모드는 `host`이다.

기본 환경 변수는 다음과 같습니다.

```bash
OLCA_BASE_URL=http://localhost:8080
OLLAMA_BASE_URL=http://localhost:11434
VERIFY_DOCKER_NETWORK=host
```

## Evidence

각 실행은 `verification-runs/` 아래에 증적을 남깁니다.

```text
verification-runs/<runId>/
  docker-build.log
  stdout.log
  stderr.log
  summary.txt
  result.json
verification-runs/latest-run.txt
```

`summary.txt`는 사람이 빠르게 확인하기 위한 파일이고, `result.json`은 도구나 AI가 검증 결과를 읽기 위한 파일입니다.

검증 성공을 주장할 때는 최소한 다음 정보를 함께 확인해야 합니다.

- Run ID
- mode
- script
- image
- dockerfile
- result
- exit code

예시:

```text
Run ID: 20260704T165438Z-all
Result: pass
Exit code: 0
Mode: all
Script: scripts/dev/verify-all.sh
Image: olca-verifier:local
Dockerfile: scripts/dev/ai-verification-sandbox.Dockerfile
Network: host
```

## 한계

이 Docker 구성은 배포용이 아닙니다.

포함하지 않는 것:

- Spring Boot 애플리케이션 실행용 Dockerfile
- MongoDB 컨테이너 구성
- Ollama 컨테이너 구성
- 전체 서비스를 한 번에 실행하는 docker-compose.yml
- 운영 프로필, 볼륨, 헬스체크 구성

배포용 Docker는 후속 개선 과제로 분리합니다.

## 경계

검증 샌드박스는 새로운 품질 규칙을 만들지 않습니다. pass/fail 기준은 기존 검증 스크립트가 소유합니다.

- `scripts/dev/verify-backend.sh`
- `scripts/dev/verify-rag.sh`
- `scripts/dev/verify-all.sh`

Docker는 이 스크립트를 다른 실행 환경에서 재현하고, 결과 증적을 남기는 역할만 합니다.
