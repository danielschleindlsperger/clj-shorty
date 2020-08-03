#!/bin/sh -l

set -e

echo "Logging into registry."
docker login ${INPUT_REGISTRY} --username ${INPUT_USERNAME} --password ${INPUT_PASSWORD}

echo "Registering the private key with the agent."
mkdir -p "$HOME/.ssh"
printf '%s\n' "$INPUT_SSH_PRIVATE_KEY" > "$HOME/.ssh/id_rsa"
chmod 600 "$HOME/.ssh/id_rsa"
eval $(ssh-agent)
ssh-add "$HOME/.ssh/id_rsa"

echo "Adding SSH fingerprint to known hosts."
HOSTNAME=$(echo $INPUT_REMOTE_DOCKER_HOST | sed 's/.*@//')
echo $HOSTNAME
ssh-keyscan -p 22 -t rsa "${HOSTNAME}" >> "$HOME/.ssh/known_hosts"

echo "Starting Swarm deployment"
COMPOSE_FILE=`realpath "${GITHUB_WORKSPACE}/${INPUT_STACK_FILE}"`
echo $COMPOSE_FILE

docker --log-level debug --host "ssh://${INPUT_REMOTE_DOCKER_HOST}" \
stack deploy -c ${COMPOSE_FILE} --with-registry-auth --resolve-image=always ${INPUT_STACK_NAME}