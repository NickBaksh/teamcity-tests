#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:8111}"
ADMIN_USER="${2:-admin}"
ADMIN_PASS="${3:-admin}"

echo "Bootstrapping TeamCity at ${BASE_URL}..."

if curl -fsS -u "${ADMIN_USER}:${ADMIN_PASS}" -H 'Accept: application/json' \
  "${BASE_URL%/}/app/rest/server" >/dev/null 2>&1; then
  echo "TeamCity already configured."
  exit 0
fi

try_post() {
  local url="$1"
  shift || true
  curl -sS -o /tmp/tc-bootstrap-post.txt -w '%{http_code}' \
    -X POST "${url}" "$@" || true
}

echo "Attempting first-start automation..."
try_post "${BASE_URL%/}/showAgreement.html" \
  --data-urlencode "accept=true" >/dev/null || true
try_post "${BASE_URL%/}/installLicense.html" \
  --data-urlencode "continue=true" >/dev/null || true

try_post "${BASE_URL%/}/createAdmin.html" \
  --data-urlencode "username=${ADMIN_USER}" \
  --data-urlencode "password=${ADMIN_PASS}" \
  --data-urlencode "retypedPassword=${ADMIN_PASS}" \
  --data-urlencode "submitCreateAdmin=Create Account" >/dev/null || true

try_post "${BASE_URL%/}/admin/setupAdmin.html" \
  --data-urlencode "login=${ADMIN_USER}" \
  --data-urlencode "password=${ADMIN_PASS}" \
  --data-urlencode "retypedPassword=${ADMIN_PASS}" >/dev/null || true

echo "Bootstrap attempts finished."
exit 0
