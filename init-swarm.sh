#!/bin/bash
docker swarm init

docker network create --driver=overlay spring_network --attachable=true

docker stack deploy local-env -c docker-compose-local-env.yml