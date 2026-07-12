# Deployment Docker

OLCA에는 두 종류의 Docker 구성이 있습니다.

- `Dockerfile`, `docker-compose.yml`: 다른 컴퓨터에서 OLCA를 실행하기 위한 배포/실행용 구성
- `scripts/dev/ai-verification-sandbox.Dockerfile`: 이미 실행 중인 OLCA를 검증하기 위한 검증용 샌드박스

이 문서는 배포/실행용 Docker만 다룹니다.

## 목적

배포용 Docker의 목표는 면접관이나 리뷰어가 로컬 Java, Gradle, MongoDB 설치 상태에 덜 의존하고 OLCA 서버를 실행해볼 수 있게 만드는 것입니다.

현재 구성은 다음 컨테이너를 함께 실행합니다.

```text
olca       -> Spring Boot application
mongo      -> KnowledgeBase 저장소
ollama     -> local LLM / embedding server
ollama-init -> 첫 실행 시 필요한 Ollama 모델 pull
```

## 실행 방법

```bash
docker compose up --build
```

백그라운드 실행:

```bash
docker compose up --build -d
```

로그 확인:

```bash
docker compose logs -f olca
```

종료:

```bash
docker compose down
```

볼륨까지 삭제:

```bash
docker compose down -v
```

## Ollama model 준비

`ollama-init` 서비스가 첫 실행 시 필요한 모델을 자동으로 받습니다.

```text
nomic-embed-text -> embedding model
llama3.1         -> default chat model
```

채팅 모델은 환경 변수로 바꿀 수 있습니다.

```bash
OLLAMA_MODEL=qwen2.5 docker compose up --build
```

모델을 수동으로 다시 받을 때는 다음 명령을 사용합니다.

```bash
docker compose exec ollama ollama pull nomic-embed-text
docker compose exec ollama ollama pull llama3.1
```

## 주요 환경 변수

```text
SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/olca
OLLAMA_BASE_URL=http://ollama:11434
OLLAMA_MODEL=llama3.1
VTUBER_WEBSOCKET_URL=ws://host.docker.internal:8000/client-ws
```

`VTUBER_WEBSOCKET_URL`은 Open-LLM-VTuber가 별도로 실행 중일 때 연결하기 위한 값입니다. OLCA 서버만 확인할 경우 기본값을 그대로 두어도 됩니다.

## 검증용 Docker와의 차이

배포용 Docker는 애플리케이션을 실행합니다.
검증용 Docker는 이미 실행 중인 애플리케이션을 대상으로 검증 스크립트를 실행합니다.

```text
배포용 Docker
-> docker compose up --build
-> OLCA, MongoDB, Ollama 실행

검증용 Docker
-> bash scripts/dev/verify-in-docker.sh all
-> 실행 중인 OLCA를 대상으로 검증 스크립트 실행
```

## 한계

현재 배포용 Docker는 포트폴리오 리뷰와 로컬 재현을 위한 구성입니다.
운영 배포를 위한 다음 항목은 아직 포함하지 않았습니다.

- 운영용 secret 관리
- HTTPS / reverse proxy
- MongoDB 인증 계정
- Ollama 모델 사전 bake-in
- CI/CD 이미지 배포
- healthcheck 기반 readiness 보강
