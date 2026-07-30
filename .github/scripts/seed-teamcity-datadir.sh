#!/usr/bin/env bash
# Seeds TeamCity datadir from an official TeamCity backup zip (GitHub Release or local file).
# Must run BEFORE the TeamCity server starts (empty datadir).
set -euo pipefail

DATADIR="${1:-infra/teamcity-server/data}"
BACKUP_ZIP="${2:-}"
TC_IMAGE="${TEAMCITY_IMAGE:-jetbrains/teamcity-server:2026.1.1}"
RELEASE_TAG="${TEAMCITY_BACKUP_TAG:-teamcity-backup-v1}"
# Default user inside jetbrains/teamcity-server
TC_UID="${TEAMCITY_UID:-1000}"
TC_GID="${TEAMCITY_GID:-1000}"

echo "Seeding TeamCity datadir at ${DATADIR}"

mkdir -p "$(dirname "${DATADIR}")"
rm -rf "${DATADIR}"
mkdir -p "${DATADIR}"

WORKDIR="$(mktemp -d)"
cleanup() { rm -rf "${WORKDIR}"; }
trap cleanup EXIT

if [[ -n "${BACKUP_ZIP}" ]]; then
  if [[ ! -f "${BACKUP_ZIP}" ]]; then
    echo "Backup file not found: ${BACKUP_ZIP}" >&2
    exit 1
  fi
  cp "${BACKUP_ZIP}" "${WORKDIR}/backup.zip"
else
  if ! command -v gh >/dev/null 2>&1; then
    echo "gh CLI is required to download release ${RELEASE_TAG} (or pass a local zip as \$2)" >&2
    exit 1
  fi
  echo "Downloading TeamCity backup from release ${RELEASE_TAG}..."
  GH_ARGS=(release download "${RELEASE_TAG}" -p "*.zip" -D "${WORKDIR}")
  if [[ -n "${GITHUB_REPOSITORY:-}" ]]; then
    GH_ARGS+=(--repo "${GITHUB_REPOSITORY}")
  fi
  gh "${GH_ARGS[@]}"
  FOUND="$(find "${WORKDIR}" -maxdepth 1 -type f -name '*.zip' | head -n 1 || true)"
  if [[ -z "${FOUND}" ]]; then
    echo "No .zip asset found in release ${RELEASE_TAG}" >&2
    exit 1
  fi
  mv "${FOUND}" "${WORKDIR}/backup.zip"
fi

DATADIR_ABS="$(cd "${DATADIR}" && pwd)"
BACKUP_ABS="${WORKDIR}/backup.zip"

# Host-created bind mount is owned by the runner user; maintainDB in the image
# runs as a non-root uid and cannot write database.properties unless we restore as root.
echo "Restoring $(basename "${BACKUP_ZIP:-release-asset}") into ${DATADIR_ABS} via ${TC_IMAGE}..."
docker run --rm --user root \
  -v "${DATADIR_ABS}:/data/teamcity_server/datadir" \
  -v "${BACKUP_ABS}:/backup/teamcity-backup.zip:ro" \
  --entrypoint /bin/bash \
  "${TC_IMAGE}" \
  -lc "set -euo pipefail
/opt/teamcity/bin/maintainDB.sh restore \
  -A /data/teamcity_server/datadir \
  -I \
  -F /backup/teamcity-backup.zip
chown -R ${TC_UID}:${TC_GID} /data/teamcity_server/datadir
"

echo "TeamCity datadir seeded successfully."
