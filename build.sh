#!/bin/sh

set -e

docker login --username=_ --password=${HEROKU_TOKEN} registry.heroku.com

HEROKU_APP_NAME="clj-shorty"

docker build -t registry.heroku.com/${HEROKU_APP_NAME}/web .
docker push registry.heroku.com/${HEROKU_APP_NAME}/web
