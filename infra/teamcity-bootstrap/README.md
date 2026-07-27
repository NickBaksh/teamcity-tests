# TeamCity first-start in CI

Fresh `jetbrains/teamcity-server` often shows a setup wizard before REST auth works.

CI flow:
1. `wait-for-url.sh` → `login.html`
2. `bootstrap-teamcity.sh` → wizard/admin form posts
3. `wait-for-teamcity-ready.sh` → authenticated `GET /app/rest/server` as `admin:admin`

If step 3 times out in GitHub Actions:
- complete first-start once locally with the same image tag
- keep/seed `infra/teamcity-server/data` (gitignored by default) via an approved cache/artifact strategy
- or change credentials in `ci-host.properties` to match your seeded admin

Do not commit production secrets into the repo.
