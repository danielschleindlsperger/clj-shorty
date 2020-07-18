#!/usr/bin/env bash

set -e

SHA=$(git rev-parse --short HEAD)
VERSION=$(git show -s --format=%ci-%h $SHA | sed 's/+//g' | sed 's/[:+ ]/-/g')
IMAGE="docker.pkg.github.com/danielschleindlsperger/clj-shorty/clj-shorty:${VERSION}"
SERVER_HOST=94.130.75.201

# Log into registry
cat $GITHUB_TOKEN | docker login https://docker.pkg.github.com -u danielschleindlsperger --password-stdin

# Build and tag image
docker build . -t $IMAGE

# Push image
docker push $IMAGE
