#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   authorize-teamcity-agent.sh purge [baseUrl] [user] [pass]
#   authorize-teamcity-agent.sh authorize [baseUrl] [user] [pass] [timeoutSec] [sleepSec]
#   authorize-teamcity-agent.sh [baseUrl] [user] [pass] [timeoutSec] [sleepSec]   # legacy → authorize

MODE="${1:-authorize}"
BASE_URL="${2:-http://localhost:8111}"
ADMIN_USER="${3:-admin}"
ADMIN_PASS="${4:-admin}"
TIMEOUT_SECONDS="${5:-420}"
SLEEP_SECONDS="${6:-5}"

if [[ "${MODE}" != "purge" && "${MODE}" != "authorize" ]]; then
  BASE_URL="${1:-http://localhost:8111}"
  ADMIN_USER="${2:-admin}"
  ADMIN_PASS="${3:-admin}"
  TIMEOUT_SECONDS="${4:-420}"
  SLEEP_SECONDS="${5:-5}"
  MODE="authorize"
fi

REST="${BASE_URL%/}/app/rest"
AUTH=(-u "${ADMIN_USER}:${ADMIN_PASS}")
# Do not request unknown fields (e.g. "upgrading") — TeamCity returns HTTP 400 and lists nothing.
FIELDS='agent(id,name,connected,authorized,enabled,uptodate)'
AGENT_CONF="${TEAMCITY_AGENT_CONF:-infra/teamcity-agent/conf/buildAgent.properties}"
CONTAINER="${TEAMCITY_AGENT_CONTAINER:-teamcity-agent}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required" >&2
  exit 1
fi

list_agents() {
  local code body
  body="$(mktemp)"
  code="$(curl -sS -o "${body}" -w '%{http_code}' "${AUTH[@]}" -H 'Accept: application/json' \
    "${REST}/agents?locator=includeDisconnected:true&fields=${FIELDS}" || echo "000")"
  if [[ "${code}" != "200" ]]; then
    echo "WARN: list agents HTTP ${code}" >&2
    cat "${body}" >&2 || true
    echo >&2
    rm -f "${body}"
    echo '{"agent":[]}'
    return 0
  fi
  cat "${body}"
  rm -f "${body}"
}

delete_agent() {
  local id="$1"
  echo "Deleting agent id=${id}"
  curl -sS -o /tmp/tc-agent-del.txt -w "HTTP %{http_code}\n" "${AUTH[@]}" \
    -X DELETE "${REST}/agents/id:${id}" || true
}

purge_all_agents() {
  local payload ids
  payload="$(list_agents)"
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

disable_upgrade_and_restart() {
  local waited=0
  while [[ ! -f "${AGENT_CONF}" && "${waited}" -lt 90 ]]; do
    echo "Waiting for agent conf: ${AGENT_CONF}"
    sleep 2
    waited=$((waited + 2))
  done
  if [[ ! -f "${AGENT_CONF}" ]]; then
    echo "Agent conf not ready: ${AGENT_CONF}" >&2
    return 1
  fi
  if grep -q 'teamcity.agent.upgrade.disabled=true' "${AGENT_CONF}"; then
    echo "Auto-upgrade already disabled in agent conf."
    return 0
  fi
  echo "Disabling agent auto-upgrade and restarting ${CONTAINER}..."
  echo 'teamcity.agent.upgrade.disabled=true' >> "${AGENT_CONF}"
  docker restart "${CONTAINER}" >/dev/null
  sleep 25
}

if [[ "${MODE}" == "purge" ]]; then
  echo "Purging all TeamCity agents at ${BASE_URL}..."
  purge_all_agents
  exit 0
fi

echo "Authorizing connected TeamCity agent at ${BASE_URL}..."

UPGRADE_FIX_ATTEMPTED=0
end=$((SECONDS + TIMEOUT_SECONDS))
while (( SECONDS < end )); do
  payload="$(list_agents)"
  echo "${payload}"

  id="$(echo "${payload}" | jq -r '
    (.agent // [])
    | map(select((.connected // false) == true))
    | (map(select((.authorized // false) != true)) + .)
    | .[0].id // empty
  ')"

  if [[ -z "${id}" ]]; then
    echo "No connected agent yet; waiting..."
    docker ps -a --filter "name=^/${CONTAINER}$" --format '{{.Names}} {{.Status}}' || true
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
  uptodate="$(echo "${payload}" | jq -r --arg id "${id}" '
    (.agent // [])[] | select((.id|tostring) == $id) | .uptodate // true
  ')"

  echo "Using agent id=${id} name=${name} authorized=${authorized} enabled=${enabled} uptodate=${uptodate}"

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

  # Stop plugins upgrade loop that leaves the agent unable to take builds.
  if [[ "${UPGRADE_FIX_ATTEMPTED}" -eq 0 ]]; then
    UPGRADE_FIX_ATTEMPTED=1
    disable_upgrade_and_restart || true
    sleep "${SLEEP_SECONDS}"
    continue
  fi

  payload="$(list_agents)"
  ready="$(echo "${payload}" | jq -r '
    (.agent // [])[]
    | select((.connected // false) == true
        and (.authorized // false) == true
        and (.enabled // false) == true)
    | .id
  ' | head -n1)"

  if [[ -n "${ready}" ]]; then
    echo "Agent id=${ready} is ready for builds."
    echo "${payload}" | jq -r '
      (.agent // [])[]
      | "\(.id) \(.name) connected=\(.connected) authorized=\(.authorized) enabled=\(.enabled) uptodate=\(.uptodate)"
    '
    exit 0
  fi

  sleep "${SLEEP_SECONDS}"
done

echo "Timed out waiting for a ready TeamCity agent." >&2
list_agents >&2 || true
docker logs "${CONTAINER}" --tail 120 >&2 || true
exit 1
