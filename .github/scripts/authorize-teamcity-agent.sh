#!/usr/bin/env bash
set -euo pipefail

REST="http://localhost:8111/app/rest"
AGENT_NAME="nbank-agent"

echo "Waiting for TeamCity agent..."

for i in $(seq 1 60); do
URL="$REST/agents?locator=name:$AGENT_NAME&fields=agent(id,name,connected,authorized)"
echo "GET $URL"

    RESPONSE=$(curl -s \
        -u admin:admin \
        -H "Accept: application/json" \
        "$URL")

    echo "$RESPONSE"

    ID=$(echo "$RESPONSE" | jq -r '.agent[0].id // empty')
    CONNECTED=$(echo "$RESPONSE" | jq -r '.agent[0].connected // false')
    AUTHORIZED=$(echo "$RESPONSE" | jq -r '.agent[0].authorized // false')

    echo "id=$ID connected=$CONNECTED authorized=$AUTHORIZED"

    if [[ "$AUTHORIZED" == "true" ]]; then
        echo "Already authorized."
        exit 0
    fi

    if [[ "$CONNECTED" == "true" ]]; then

        HTTP=$(curl -s \
            -o /dev/null \
            -w "%{http_code}" \
            -u admin:admin \
            -X PUT \
            -H "Content-Type: application/json" \
            -d '{"status":true}' \
            "$REST/agents/id:$ID/authorizedInfo")

        echo "HTTP=$HTTP"

        if [[ "$HTTP" == "200" || "$HTTP" == "204" ]]; then
            echo "Agent authorized."
            exit 0
        fi
    fi

    sleep 5
done

echo "Timed out waiting for TeamCity agent."
exit 1