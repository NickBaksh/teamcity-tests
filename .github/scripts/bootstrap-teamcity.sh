#!/usr/bin/env bash
# Ensures TeamCity accepts ADMIN_USER:ADMIN_PASS for REST.
# After backup restore the password in the zip may differ — reset via super-user token.
set -euo pipefail

BASE_URL="${1:-http://localhost:8111}"
ADMIN_USER="${2:-admin}"
ADMIN_PASS="${3:-admin}"
CONTAINER="${TEAMCITY_CONTAINER:-teamcity-server}"
LOGDIR="${TEAMCITY_LOGDIR:-infra/teamcity-server/logs}"

REST_URL="${BASE_URL%/}/app/rest/server"
USERS_URL="${BASE_URL%/}/app/rest/users"

echo "Bootstrapping TeamCity at ${BASE_URL}..."

if curl -fsS -u "${ADMIN_USER}:${ADMIN_PASS}" -H 'Accept: application/json' \
  "${REST_URL}" >/dev/null 2>&1; then
  echo "TeamCity already accepts ${ADMIN_USER} credentials."
  exit 0
fi

extract_super_token() {
  local token=""
  if command -v docker >/dev/null 2>&1; then
    token="$(docker logs "${CONTAINER}" 2>&1 \
      | grep -oE 'Super user authentication token:[[:space:]]*[0-9]+' \
      | tail -n 1 \
      | grep -oE '[0-9]+$' || true)"
  fi
  if [[ -z "${token}" && -f "${LOGDIR}/teamcity-server.log" ]]; then
    token="$(grep -oE 'Super user authentication token:[[:space:]]*[0-9]+' \
      "${LOGDIR}/teamcity-server.log" \
      | tail -n 1 \
      | grep -oE '[0-9]+$' || true)"
  fi
  printf '%s' "${token}"
}

echo "Configured credentials rejected — trying super-user password reset..."

TOKEN=""
for _ in $(seq 1 30); do
  TOKEN="$(extract_super_token)"
  if [[ -n "${TOKEN}" ]]; then
    break
  fi
  sleep 2
done

if [[ -z "${TOKEN}" ]]; then
  echo "Could not find Super user authentication token in docker logs / ${LOGDIR}" >&2
  docker logs "${CONTAINER}" 2>&1 | tail -n 80 >&2 || true
  exit 1
fi

echo "Found super-user token (from server log)."

# Verify super-user REST access (empty username + token as password).
if ! curl -fsS -u ":${TOKEN}" -H 'Accept: application/json' "${REST_URL}" >/dev/null 2>&1; then
  echo "Super-user REST auth failed." >&2
  exit 1
fi

# Reset password for locator username:ADMIN_USER
code="$(curl -sS -o /tmp/tc-set-pass.txt -w '%{http_code}' \
  -u ":${TOKEN}" \
  -X PUT \
  -H 'Content-Type: text/plain' \
  --data-binary "${ADMIN_PASS}" \
  "${USERS_URL}/username:${ADMIN_USER}/password" || true)"

if [[ "${code}" != "200" && "${code}" != "204" ]]; then
  echo "Failed to set password for username:${ADMIN_USER} (HTTP ${code})." >&2
  echo "Users:" >&2
  curl -sS -u ":${TOKEN}" -H 'Accept: application/json' "${USERS_URL}" | head -c 2000 >&2 || true
  echo >&2
  cat /tmp/tc-set-pass.txt >&2 || true
  exit 1
fi

if curl -fsS -u "${ADMIN_USER}:${ADMIN_PASS}" -H 'Accept: application/json' \
  "${REST_URL}" >/dev/null 2>&1; then
  echo "Password for ${ADMIN_USER} reset; REST auth OK."
  exit 0
fi

echo "Password reset reported success, but ${ADMIN_USER}:${ADMIN_PASS} still fails." >&2
exit 1
