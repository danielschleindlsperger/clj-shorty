#!/bin/sh -l

set -e

echo "Deploying app '${INPUT_APP_NAME}' with image '${INPUT_IMAGE_NAME}' to CapRover."

echo $PATH

caprover deploy \
  --caproverUrl "${INPUT_CAPROVER_URL}" \
  --caproverPassword "${INPUT_PASSWORD}" \
  --caproverApp "${INPUT_APP_NAME}" \
  --imageName "${INPUT_IMAGE_NAME}"

echo "Successfully deployed app '${INPUT_APP_NAME}'."