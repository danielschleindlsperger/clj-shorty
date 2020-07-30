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
HOSTNAME=$(echo $FOO | sed 's/.*@//')
ssh-keyscan "${HOSTNAME}" >> "$HOME/.ssh/known_hosts"

echo "Starting Swarm deployment"
docker --log-level debug --host "ssh://${INPUT_REMOTE_DOCKER_HOST}" \
stack deploy -c ${INPUT_STACK_FILE} ${INPUT_STACK_NAME}