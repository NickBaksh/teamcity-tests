#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:8111}"
ADMIN_USER="${2:-admin}"
ADMIN_PASS="${3:-admin}"

echo "🔧 Bootstrapping TeamCity at ${BASE_URL}..."

# Ждем, пока TeamCity запустится
echo "⏳ Waiting for TeamCity to start..."
for i in {1..60}; do
  if curl -fsS -o /dev/null "${BASE_URL}/" 2>/dev/null; then
    echo "✅ TeamCity is responding"
    break
  fi
  echo "Attempt $i/60: Waiting..."
  sleep 5
done

# Проверяем статус
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/")
echo "HTTP status: ${HTTP_CODE}"

if [[ "${HTTP_CODE}" == "503" ]]; then
  echo "⏳ TeamCity is in maintenance mode (first start)"

  # Пытаемся получить CSRF токен
  echo "🔑 Fetching CSRF token..."
  CSRF_TOKEN=$(curl -s "${BASE_URL}/" | grep -o 'name="tc-csrf-token" content="[^"]*"' | cut -d'"' -f4)
  echo "CSRF Token: ${CSRF_TOKEN}"

  if [[ -n "${CSRF_TOKEN}" ]]; then
    echo "📝 Confirming first start..."
    curl -X POST "${BASE_URL}/maintenance/confirmFirstStart" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -H "X-TC-CSRF-Token: ${CSRF_TOKEN}" \
      --data-urlencode "confirm=true" \
      -o /dev/null || true

    echo "⏳ Waiting for TeamCity to complete initialization..."
    sleep 30
  fi
fi

# Проверяем, настроен ли уже TeamCity
if curl -fsS -u "${ADMIN_USER}:${ADMIN_PASS}" \
  -H 'Accept: application/json' \
  "${BASE_URL}/app/rest/server" >/dev/null 2>&1; then
  echo "✅ TeamCity already configured with admin user."
  exit 0
fi

# Создаем администратора
echo "👤 Creating admin user..."
curl -X POST "${BASE_URL}/app/rest/users" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"${ADMIN_USER}\",
    \"password\": \"${ADMIN_PASS}\",
    \"email\": \"admin@example.com\"
  }" -o /dev/null || true

sleep 5

# Проверяем
if curl -fsS -u "${ADMIN_USER}:${ADMIN_PASS}" \
  -H 'Accept: application/json' \
  "${BASE_URL}/app/rest/server" >/dev/null 2>&1; then
  echo "✅ TeamCity bootstrap completed successfully!"
  exit 0
else
  echo "⚠️  Could not verify admin user, but continuing..."
  exit 0
fi