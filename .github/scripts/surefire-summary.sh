#!/usr/bin/env bash
set -euo pipefail

REPORTS_DIR="${1:-target/surefire-reports}"

if [[ ! -d "${REPORTS_DIR}" ]]; then
  echo "surefire: no reports dir (${REPORTS_DIR})"
  exit 0
fi

total=0
failures=0
errors=0
skipped=0
found=0

while IFS= read -r -d '' file; do
  found=1
  line="$(grep -E 'Tests run:' "${file}" | tail -n 1 || true)"
  [[ -z "${line}" ]] && continue
  t="$(echo "${line}" | sed -n 's/.*Tests run: \([0-9]*\).*/\1/p')"
  f="$(echo "${line}" | sed -n 's/.*Failures: \([0-9]*\).*/\1/p')"
  e="$(echo "${line}" | sed -n 's/.*Errors: \([0-9]*\).*/\1/p')"
  s="$(echo "${line}" | sed -n 's/.*Skipped: \([0-9]*\).*/\1/p')"
  total=$((total + ${t:-0}))
  failures=$((failures + ${f:-0}))
  errors=$((errors + ${e:-0}))
  skipped=$((skipped + ${s:-0}))
done < <(find "${REPORTS_DIR}" -type f -name '*.txt' -print0 2>/dev/null || true)

if [[ "${found}" -eq 0 || "${total}" -eq 0 ]]; then
  xml_count="$(find "${REPORTS_DIR}" -type f -name 'TEST-*.xml' | wc -l | tr -d ' ')"
  echo "surefire: files=${xml_count} (no txt summary aggregated)"
  exit 0
fi

echo "Tests run: ${total}, Failures: ${failures}, Errors: ${errors}, Skipped: ${skipped}"
