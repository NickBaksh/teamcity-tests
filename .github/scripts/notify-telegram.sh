#!/usr/bin/env bash
set -euo pipefail

BOT_TOKEN="${TELEGRAM_BOT_TOKEN:-}"
CHAT_ID="${TELEGRAM_CHAT_ID:-}"

if [[ -z "${BOT_TOKEN}" || -z "${CHAT_ID}" ]]; then
  echo "Telegram secrets are not set — skip notification."
  exit 0
fi

STATUS="${1:?Usage: notify-telegram.sh <success|failure|cancelled> <run_url> [extra]}"
RUN_URL="${2:-}"
EXTRA="${3:-}"

case "${STATUS}" in
  success) MARK="[OK]" ;;
  failure) MARK="[FAIL]" ;;
  cancelled) MARK="[CANCELLED]" ;;
  *) MARK="[INFO]" ;;
esac

ACTOR="${GITHUB_ACTOR:-unknown}"
EVENT="${GITHUB_EVENT_NAME:-unknown}"
REPO="${GITHUB_REPOSITORY:-unknown}"
BRANCH="${GITHUB_REF_NAME:-unknown}"
SHA="${GITHUB_SHA:-unknown}"
SHORT_SHA="$(echo "${SHA}" | cut -c1-7)"

SWAGGER_URL="${SWAGGER_COVERAGE_URL:-}"
if [[ -n "${SWAGGER_URL}" ]]; then
  SWAGGER_LINE="swagger: ${SWAGGER_URL}swagger/"
else
  SWAGGER_LINE=""
fi

TEXT="${MARK} CI ${STATUS}
repo: ${REPO}
event: ${EVENT}
branch: ${BRANCH}
actor: ${ACTOR}
commit: ${SHORT_SHA}
run: ${RUN_URL}
${EXTRA}
${SWAGGER_LINE}"

curl -fsS -X POST "https://api.telegram.org/bot${BOT_TOKEN}/sendMessage" \
  --data-urlencode "chat_id=${CHAT_ID}" \
  --data-urlencode "text=${TEXT}" \
  >/dev/null

echo "Telegram notification sent (${STATUS})."