# CI notes

## Scope
- GitHub Actions workflow with quality gates
- TeamCity seeded from release backup (skips first-start wizard)
- Smoke API before full API
- Artifacts (retention 7 days) and docker logs
- Telegram notify with surefire summary
- Composite actions for Java setup and artifact upload
- Host CI config (`ci-host.properties`)
- Maven Wrapper (`mvnw`) for reproducible Maven on runners

## Jobs
1. `build_and_lint` — compile + checkstyle (`-Dcheckstyle.skip=false`)
2. `api_tests` — seed datadir, TeamCity up, REST ready, smoke, full API, Allure, artifacts
3. `ui_tests_chrome` / `ui_tests_firefox` — enabled via repo variable `ENABLE_UI_MATRIX=true`
4. `report_notify` — Telegram always

## TeamCity seed (option 1)
CI downloads the official TeamCity backup from GitHub Release tag `teamcity-backup-v1` and restores it into `infra/teamcity-server/data` with `maintainDB.sh` **before** `docker compose up`.
Volumes `data` + `logs` are chown'd to `1000:1000` (`tcuser`).

If backup admin password is not `admin`/`admin`, bootstrap reads the Super user token from server logs and resets the password via REST.

Script: `.github/scripts/seed-teamcity-datadir.sh`, `.github/scripts/bootstrap-teamcity.sh`

## TeamCity agent
Backup restore keeps stale agent records. CI:
1. Purges all agents from the backup (`authorize-teamcity-agent.sh purge`) before starting a fresh agent
2. Starts the agent container (it creates `buildAgent.properties` itself — do not pre-seed)
3. Authorizes + enables the connected agent; if stuck `upgrading`, appends `teamcity.agent.upgrade.disabled=true` and restarts
4. Runs Maven with `-Dsurefire.parallel=none -Djunit.parallel.enabled=false` so one agent is not overloaded

Without this, builds stay queued with `There are no idle compatible agents which can run this build`.

```bash
# Local (needs Docker + gh auth):
.github/scripts/seed-teamcity-datadir.sh infra/teamcity-server/data

# Or from a local zip:
.github/scripts/seed-teamcity-datadir.sh infra/teamcity-server/data /path/to/TeamCity_Backup.zip
```

Env overrides:
- `TEAMCITY_BACKUP_TAG` (default `teamcity-backup-v1`)
- `TEAMCITY_IMAGE` (default `jetbrains/teamcity-server:2026.1.1`)

### Refreshing the backup
1. Start TeamCity locally (`infra/docker-compose.yml`), complete wizard once (`admin` / `admin`).
2. Administration → Backup → create backup zip (same TeamCity version as compose image).
3. Publish / replace asset on release `teamcity-backup-v1` (or bump tag and update `TEAMCITY_BACKUP_TAG` in `ci.yml`).

Current release: https://github.com/NickBaksh/teamcity-tests/releases/tag/teamcity-backup-v1

## GitHub setup
### Secrets
- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_CHAT_ID`

### Variables
- `ENABLE_UI_MATRIX=true` — enable UI matrix jobs

### Branch protection
Settings → Branches → Protect `main`:
- Require status checks: `Build + Checkstyle`, `API tests`
- Require branches to be up to date before merging

## Triggers
- `workflow_dispatch`
- push: `main`, `tests/**`, `ci/**`, `feature/**`
- pull_request → `main`
- ignores markdown/docs-only changes
