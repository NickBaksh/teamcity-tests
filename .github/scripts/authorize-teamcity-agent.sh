#!/usr/bin/env bash
set -euo pipefail

# Known-good authorize flow (based on 9506f58), fixed for TeamCity 2026.1:
# - locator authorized:any (default hides unauthorized agents)
# - do NOT use includeDisconnected (unsupported → HTTP 400)

BASE_URL="${1:-http://localhost:8111}"
ADMIN_USER="${2:-admin}"
ADMIN_PASS="${3:-admin}"
TIMEOUT_SECONDS="${4:-300}"
SLEEP_SECONDS="${5:-5}"

if [[ "${1:-}" == "purge" || "${1:-}" == "authorize" ]]; then
  MODE="$1"
  BASE_URL="${2:-http://localhost:8111}"
  ADMIN_USER="${3:-admin}"
  ADMIN_PASS="${4:-admin}"
  TIMEOUT_SECONDS="${5:-300}"
  SLEEP_SECONDS="${6:-5}"
else
  MODE="authorize"
fi

REST="${BASE_URL%/}/app/rest"
AUTH=(-u "${ADMIN_USER}:${ADMIN_PASS}")
FIELDS='agent(id,name,connected,authorized,enabled)'
LOCATOR='authorized:any'

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required" >&2
  exit 1
fi

list_agents() {
  curl -fsS "${AUTH[@]}" -H 'Accept: application/json' \
    "${REST}/agents?locator=${LOCATOR}&fields=${FIELDS}"
}

delete_agent() {
  local id="$1"
  echo "Deleting agent id=${id}"
  curl -sS -o /tmp/tc-agent-del.txt -w "HTTP %{http_code}\n" "${AUTH[@]}" \
    -X DELETE "${REST}/agents/id:${id}" || true
}

purge_disconnected() {
  local payload ids
  payload="$(list_agents || echo '{"agent":[]}')"
  ids="$(echo "${payload}" | jq -r '.agent // [] | .[] | select((.connected // false) != true) | .id')"
  [[ -z "${ids}" ]] && return 0
  while IFS= read -r id; do
    [[ -n "${id}" ]] || continue
    echo "Deleting disconnected/stale agent id=${id}"
    delete_agent "${id}"
  done <<< "${ids}"
}

purge_all_agents() {
  local payload ids
  payload="$(list_agents || echo '{"agent":[]}')"
  echo "${payload}"
  ids="$(echo "${payload}" | jq -r '.agent // [] | .[] | .id')"
  [[ -z "${ids}" ]] && {
    echo "No agents to purge."
    return 0
  }
  while IFS= read -r id; do
    [[ -n "${id}" ]] || continue
    delete_agent "${id}"
  done <<< "${ids}"
}

put_status() {
  local path="$1"
  curl -sS -o /tmp/tc-agent-put.txt -w '%{http_code}' "${AUTH[@]}" \
    -X PUT \
    -H 'Content-Type: application/json' \
    -H 'Accept: application/json' \
    -d '{"status":true}' \
    "${REST}${path}"
}

if [[ "${MODE}" == "purge" ]]; then
  echo "Purging all TeamCity agents at ${BASE_URL}..."
  purge_all_agents
  exit 0
fi

echo "Preparing connected TeamCity agent at ${BASE_URL}..."

end=$((SECONDS + TIMEOUT_SECONDS))
while (( SECONDS < end )); do
  purge_disconnected || true

  payload="$(list_agents || echo '{"agent":[]}')"
  echo "${payload}"

  id="$(echo "${payload}" | jq -r '
    (.agent // [])
    | map(select((.connected // false) == true))
    | (map(select((.authorized // false) != true)) + map(select((.authorized // false) == true)))
    | .[0].id // empty
  ')"

  if [[ -z "${id}" ]]; then
    echo "No connected agent yet; waiting..."
    sleep "${SLEEP_SECONDS}"
    continue
  fi

  name="$(echo "${payload}" | jq -r --arg id "${id}" '
    (.agent // [])[] | select((.id|tostring) == $id) | .name
  ')"
  authorized="$(echo "${payload}" | jq -r --arg id "${id}" '
    (.agent // [])[] | select((.id|tostring) == $id) | .authorized // false
  ')"
  enabled="$(echo "${payload}" | jq -r --arg id "${id}" '
    (.agent // [])[] | select((.id|tostring) == $id) | .enabled // false
  ')"

  echo "Using connected agent id=${id} name=${name} authorized=${authorized} enabled=${enabled}"

  if [[ "${authorized}" != "true" ]]; then
    code="$(put_status "/agents/id:${id}/authorizedInfo")"
    echo "authorize HTTP=${code}"
    if [[ "${code}" != "200" && "${code}" != "204" ]]; then
      cat /tmp/tc-agent-put.txt || true
      sleep "${SLEEP_SECONDS}"
      continue
    fi
  fi

  if [[ "${enabled}" != "true" ]]; then
    code="$(put_status "/agents/id:${id}/enabledInfo")"
    echo "enable HTTP=${code}"
  fi

  payload="$(list_agents)"
  ready="$(echo "${payload}" | jq -r --arg id "${id}" '
    (.agent // [])[]
    | select((.id|tostring) == $id)
    | select((.connected // false) == true and (.authorized // false) == true)
    | .id
  ')"

  if [[ -n "${ready}" ]]; then
    echo "Connected agent id=${ready} is authorized and ready for builds."
    echo "${payload}" | jq -r '
      (.agent // [])[]
      | "\(.id) \(.name) connected=\(.connected) authorized=\(.authorized) enabled=\(.enabled)"
    '
    exit 0
  fi

  sleep "${SLEEP_SECONDS}"
done

echo "Timed out waiting for a connected authorized TeamCity agent." >&2
list_agents >&2 || true
exit 1
