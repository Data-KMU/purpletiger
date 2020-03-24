#!/bin/bash
./mvnw clean package && \
docker build -f src/main/docker/Dockerfile.jvm -t taaja/purpletiger . && \
docker save -o taaja-purpletiger.tar taaja/purpletiger && \
scp taaja-purpletiger.tar taaja@taaja.io:/home/taaja && \
ssh taaja@taaja.io 'docker load --quiet --input /home/taaja/taaja-purpletiger.tar && cd /home/taaja/deployment/taaja && docker-compose up -d'