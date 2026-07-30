#!/usr/bin/env bash
set -euo pipefail

REST="http://localhost:8111/app/rest"

echo "Waiting for TeamCity agent..."

for i in $(seq 1 60); do
    RESPONSE=$(curl -s \
        -u admin:admin \
        -H "Accept: application/json" \
        "$REST/agents")

    ID=$(echo "$RESPONSE" \
        | grep -oE '"id":[0-9]+' \
        | head -1 \
        | grep -oE '[0-9]+' || true)

    CONNECTED=$(echo "$RESPONSE" \
        | grep -o '"connected":true' || true)

    if [[ -n "$ID" && -n "$CONNECTED" ]]; then
        echo "Agent $ID connected."

        HTTP_CODE=$(curl -s \
            -o /dev/null \
            -w "%{http_code}" \
            -u admin:admin \
            -X PUT \
            -H "Content-Type: application/json" \
            -d '{"status":true}' \
            "$REST/agents/id:$ID/authorizedInfo")

        if [[ "$HTTP_CODE" == "200" || "$HTTP_CODE" == "204" ]]; then
            echo "Agent authorized."
            exit 0
        fi

        echo "Failed to authorize agent (HTTP $HTTP_CODE)"
        exit 1
    fi

    sleep 5
done

echo "Timed out waiting for TeamCity agent."
exit 1