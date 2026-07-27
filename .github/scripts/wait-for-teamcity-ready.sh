#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:8111}"
ADMIN_USER="${2:-admin}"
ADMIN_PASS="${3:-admin}"
TIMEOUT_SECONDS="${4:-600}"
SLEEP_SECONDS="${5:-10}"

REST_URL="${BASE_URL%/}/app/rest/server"

echo "Waiting for TeamCity REST: ${REST_URL} (user=${ADMIN_USER}, timeout=${TIMEOUT_SECONDS}s)"
end=$((SECONDS + TIMEOUT_SECONDS))

while (( SECONDS < end )); do
  code="$(curl -sS -o /tmp/tc-rest-body.txt -w '%{http_code}' \
    -u "${ADMIN_USER}:${ADMIN_PASS}" \
    -H 'Accept: application/json' \
    "${REST_URL}" || true)"

  if [[ "${code}" == "200" ]]; then
    echo "TeamCity REST is ready (HTTP 200)."
    head -c 200 /tmp/tc-rest-body.txt || true
    echo
    exit 0
  fi

  echo "Not ready yet: HTTP ${code}"
  sleep "${SLEEP_SECONDS}"
done

echo "Timeout waiting for authenticated TeamCity REST at ${REST_URL}" >&2
echo "Last response body:" >&2
head -c 500 /tmp/tc-rest-body.txt >&2 || true
echo >&2
exit 1
