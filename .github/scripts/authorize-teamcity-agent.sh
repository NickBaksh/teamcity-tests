#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-authorize}"
BASE_URL="${2:-http://localhost:8111}"
ADMIN_USER="${3:-admin}"
ADMIN_PASS="${4:-admin}"
TIMEOUT_SECONDS="${5:-300}"
SLEEP_SECONDS="${6:-5}"

if [[ "${MODE}" != "purge" && "${MODE}" != "authorize" ]]; then
  BASE_URL="${1:-http://localhost:8111}"
  ADMIN_USER="${2:-admin}"
  ADMIN_PASS="${3:-admin}"
  TIMEOUT_SECONDS="${4:-300}"
  SLEEP_SECONDS="${5:-5}"
  MODE="authorize"
fi

REST="${BASE_URL%/}/app/rest"
AUTH=(-u "${ADMIN_USER}:${ADMIN_PASS}")
FIELDS='agent(id,name,connected,authorized,enabled,uptodate,upgrading)'
AGENT_CONF="${TEAMCITY_AGENT_CONF:-infra/teamcity-agent/conf/buildAgent.properties}"
CONTAINER="${TEAMCITY_CONTAINER:-teamcity-agent}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required" >&2
  exit 1
fi

list_agents() {
  curl -fsS "${AUTH[@]}" -H 'Accept: application/json' \
    "${REST}/agents?locator=includeDisconnected:true&fields=${FIELDS}"
}

delete_agent() {
  local id="$1"
  echo "Deleting agent id=${id}"
  curl -sS -o /tmp/tc-agent-del.txt -w "HTTP %{http_code}\n" "${AUTH[@]}" \
    -X DELETE "${REST}/agents/id:${id}" || true
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

disable_agent_upgrade() {
  if [[ -f "${AGENT_CONF}" ]] && ! grep -q 'teamcity.agent.upgrade.disabled=true' "${AGENT_CONF}"; then
    echo "Disabling agent auto-upgrade in ${AGENT_CONF}"
    echo 'teamcity.agent.upgrade.disabled=true' >> "${AGENT_CONF}"
    docker restart "${CONTAINER}" >/dev/null || true
    sleep 15
  fi
}

if [[ "${MODE}" == "purge" ]]; then
  echo "Purging all TeamCity agents at ${BASE_URL}..."
  purge_all_agents
  exit 0
fi

echo "Authorizing connected TeamCity agent at ${BASE_URL}..."

UPGRADE_WAIT_DEADLINE=$((SECONDS + 90))
end=$((SECONDS + TIMEOUT_SECONDS))
while (( SECONDS < end )); do
  payload="$(list_agents || echo '{"agent":[]}')"
  echo "${payload}"

  id="$(echo "${payload}" | jq -r '
    (.agent // [])
    | map(select((.connected // false) == true))
    | (map(select((.authorized // false) != true)) + .)
    | .[0].id // empty
  ')"

  if [[ -z "${id}" ]]; then
    echo "No connected agent yet; waiting..."
    docker ps -a --filter "name=${CONTAINER}" --format 'table {{.Names}}\t{{.Status}}' || true
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
  upgrading="$(echo "${payload}" | jq -r --arg id "${id}" '
    (.agent // [])[] | select((.id|tostring) == $id) | .upgrading // false
  ')"

  echo "Using agent id=${id} name=${name} authorized=${authorized} enabled=${enabled} upgrading=${upgrading}"

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

  if [[ "${upgrading}" == "true" ]]; then
    if (( SECONDS >= UPGRADE_WAIT_DEADLINE )); then
      disable_agent_upgrade
      UPGRADE_WAIT_DEADLINE=$((SECONDS + 90))
    else
      echo "Agent is still upgrading; waiting..."
    fi
    sleep "${SLEEP_SECONDS}"
    continue
  fi

  payload="$(list_agents)"
  ready="$(echo "${payload}" | jq -r --arg id "${id}" '
    (.agent // [])[]
    | select((.id|tostring) == $id)
    | select((.connected // false) == true
        and (.authorized // false) == true
        and (.enabled // false) == true
        and ((.upgrading // false) != true))
    | .id
  ')"

  if [[ -n "${ready}" ]]; then
    echo "Agent id=${ready} is connected, authorized, enabled, and not upgrading."
    echo "${payload}" | jq -r '
      (.agent // [])[]
      | "\(.id) \(.name) connected=\(.connected) authorized=\(.authorized) enabled=\(.enabled) upgrading=\(.upgrading)"
    '
    exit 0
  fi

  sleep "${SLEEP_SECONDS}"
done

echo "Timed out waiting for a ready TeamCity agent." >&2
list_agents >&2 || true
docker logs "${CONTAINER}" --tail 80 >&2 || true
exit 1
