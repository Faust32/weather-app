#!/bin/bash
# wait-for-it.sh

HOST=$1
PORT=$2
shift 2
# shellcheck disable=SC2124
CMD="$@"

# shellcheck disable=SC2086
until nc -z -w 5 "${HOST}" ${PORT}
do
  echo "Waiting for ${HOST}:${PORT}..."
  sleep 1
done

echo "${HOST}:${PORT} is up, executing command"
exec ${CMD}