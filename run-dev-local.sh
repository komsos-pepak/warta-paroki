#!/bin/bash
mvn -B package --file pom.xml -DskipTests
export VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)-dev

docker rmi local-komsos-wartaparoki-spring:$VERSION --force
docker build -t local-komsos-wartaparoki-spring:$VERSION .


docker service update --image local-komsos-wartaparoki-spring:$VERSION --force local-komsos_wartaparoki_svc