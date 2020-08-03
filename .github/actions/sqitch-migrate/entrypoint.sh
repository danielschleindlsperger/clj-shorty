#!/bin/sh -l

set -eu

if [ -n "${INPUT_SSH_TUNNEL_ENABLE}" ]; then
  echo "Enabling SSH tunnel mapping."
  sshpass -p "${INPUT_SSH_TUNNEL_PASSWORD}" sh -c 'ssh -fNT -o StrictHostKeyChecking=no -L "${INPUT_SSH_TUNNEL_MAPPING}" "${INPUT_SSH_TUNNEL_DESTINATION}" -p "${INPUT_SSH_TUNNEL_PORT}" && sleep 2' 
fi

sqitch deploy "${INPUT_DATABASE_URL}"
