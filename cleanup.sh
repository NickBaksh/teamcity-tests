#!/bin/bash

# ВНИМАНИЕ! Удаляет ВСЕ проекты с префиксом Project_1783

curl -s -u admin:admin123 \
  "http://localhost:8111/app/rest/projects?locator=count:1000" \
  -H "Accept: application/json" | \
grep -o '"id":"[^"]*"' | \
grep '"id":"Project_1783' | \
cut -d'"' -f4 | \
while read id; do
    echo "🗑️ Удаляем: $id"
    curl -s -X DELETE -u admin:admin123 \
      "http://localhost:8111/app/rest/projects/$id"
done

echo "✅ Готово!"