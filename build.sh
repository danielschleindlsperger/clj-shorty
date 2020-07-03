#!/bin/sh

set -e

docker login https://docker.pkg.github.com --username $DOCKER_USER --password $DOCKER_PASSWORD


IMAGE_NAME="docker.pkg.github.com/danielschleindlsperger/clj-shorty/clj-shorty"
VERSION=$(git show -s --format=%ci-%h $GITHUB_SHA | sed 's/+//g' | sed 's/[:+ ]/-/g')
NAME="${IMAGE_NAME}:${VERSION}"

docker build -t ${NAME} .
docker push ${NAME}
