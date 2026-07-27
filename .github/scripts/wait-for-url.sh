#!/usr/bin/env bash
set -euo pipefail

URL="${1:?Usage: wait-for-url.sh <url> [timeout_seconds]}"
TIMEOUT_SECONDS="${2:-300}"
SLEEP_SECONDS="${3:-5}"

echo "Waiting for ${URL} (timeout=${TIMEOUT_SECONDS}s)..."
end=$((SECONDS + TIMEOUT_SECONDS))

while (( SECONDS < end )); do
  if curl -fsS -o /dev/null "${URL}"; then
    echo "Ready: ${URL}"
    exit 0
  fi
  echo "Not ready yet: ${URL}"
  sleep "${SLEEP_SECONDS}"
done

echo "Timeout waiting for ${URL}" >&2
exit 1
