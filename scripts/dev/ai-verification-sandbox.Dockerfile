FROM gradle:jdk21-jammy

USER root

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update -o Acquire::Retries=5 \
    && apt-get install -y --no-install-recommends \
        bash \
        ca-certificates \
        curl \
        git \
        jq \
        python3 \
        python3-pip \
        python3-requests \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

CMD ["bash", "scripts/dev/verify-all.sh"]
