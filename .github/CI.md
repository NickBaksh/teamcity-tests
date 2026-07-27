# CI notes

## Scope
- GitHub Actions workflow with quality gates
- TeamCity bootstrap and authenticated REST readiness
- Smoke API before full API
- Artifacts (retention 7 days) and docker logs
- Telegram notify with surefire summary
- Composite actions for Java setup and artifact upload
- Host CI config (`ci-host.properties`)
- Maven Wrapper (`mvnw`) for reproducible Maven on runners

## Jobs
1. `build_and_lint` — compile + checkstyle (`-Dcheckstyle.skip=false`)
2. `api_tests` — TeamCity up, bootstrap, REST ready, smoke, full API, Allure, artifacts
3. `ui_tests_chrome` / `ui_tests_firefox` — enabled via repo variable `ENABLE_UI_MATRIX=true`
4. `report_notify` — Telegram always

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
- push: `main`, `tests/**`, `ci/**`, `feature/**`
- pull_request → `main`
- ignores markdown/docs-only changes

## Fresh TeamCity
First-start wizard may block REST until admin exists.
CI runs bootstrap, then waits for `/app/rest/server` with `admin:admin`.
If this times out:
1. Complete TeamCity setup once locally against the same image/version
2. Seed a minimal datadir strategy or shared cache
