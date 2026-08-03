# Fitbit Health Android App Implementation Plan

> **For implementer:** Use TDD throughout. Write each failing test first, confirm failure, implement the minimum, then confirm green.

**Goal:** Build a public monorepo containing a native Kotlin/Jetpack Compose Fitbit health app and a private-configurable FastAPI gateway for Hermes/Tailscale.

**Architecture:** The Android client talks to a token-protected FastAPI gateway over the user's Tailscale network. The gateway reads the existing Fitbit SQLite database, starts controlled sync jobs, and forwards health-only chat requests to Hermes. Google Health credentials and personal data remain outside Git.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Retrofit/OkHttp, DataStore/Android Keystore, Compose chart library selected during setup, Python FastAPI, Pydantic, pytest, SQLite, Tailscale.

---

## Phase 0 — Repository and contracts

### Task 1: Establish public monorepo boundaries

**Files:**
- Create: `android/`
- Create: `gateway/`
- Create: `docs/api-contract.md`
- Modify: `README.md`, `.gitignore`
- Test: `gateway/tests/test_repository_hygiene.py`

Steps:
1. Add a failing hygiene test asserting no `.env`, token, SQLite, APK, or personal Tailscale config is tracked.
2. Run `pytest gateway/tests/test_repository_hygiene.py -v`; expect failure because paths do not exist.
3. Create module directories, safe example configuration, and ignore rules.
4. Run the test; expect PASS.
5. Document local setup without personal values.
6. Commit: `git add . && git commit -m "chore: establish Fitbit app monorepo"`.

### Task 2: Define API schemas first

**Files:**
- Create: `gateway/app/schemas.py`
- Create: `gateway/tests/test_schemas.py`
- Modify: `docs/api-contract.md`

Steps:
1. Write failing Pydantic tests for dashboard, sleep, metric series, sync status/job, chat request/response, and explicit timestamp offsets.
2. Run `pytest gateway/tests/test_schemas.py -v`; expect FAIL.
3. Implement minimal schemas with strict date/range validation and bounded custom ranges.
4. Run tests; expect PASS.
5. Commit: `git add gateway docs && git commit -m "feat: define health gateway API schemas"`.

## Phase 1 — FastAPI gateway

### Task 3: Add configuration and authentication

**Files:**
- Create: `gateway/app/config.py`
- Create: `gateway/app/auth.py`
- Create: `gateway/tests/test_auth.py`
- Create: `gateway/.env.example`

Steps:
1. Test missing token, invalid bearer token, and valid bearer token.
2. Confirm failure.
3. Implement environment-based config and constant-time bearer-token comparison.
4. Confirm green tests.
5. Commit: `feat: protect gateway with bearer token`.

### Task 4: Implement SQLite repository

**Files:**
- Create: `gateway/app/repository.py`
- Create: `gateway/tests/test_repository.py`

Steps:
1. Build a temporary SQLite fixture matching the existing tables and test dashboard, sleep, daily metric, trend, and intraday queries.
2. Confirm failure.
3. Implement parameterized read-only queries, bounded result sizes, and null-safe mapping.
4. Confirm green tests.
5. Commit: `feat: read Fitbit metrics from SQLite`.

### Task 5: Implement timezone-aware serialization

**Files:**
- Create: `gateway/app/timezone.py`
- Create: `gateway/tests/test_timezone.py`

Steps:
1. Test UTC-to-Europe/Madrid conversion in CET and CEST, including a sleep session crossing midnight.
2. Confirm failure.
3. Implement IANA-zone conversion and ISO-8601 output with offsets.
4. Confirm green tests.
5. Commit: `feat: serialize health timestamps with timezone`.

### Task 6: Add read-only metric endpoints

**Files:**
- Create: `gateway/app/main.py`
- Create: `gateway/app/routes/health.py`
- Create: `gateway/tests/test_health_routes.py`

Steps:
1. Write TestClient tests for `/health`, dashboard, sleep, heart rate, steps, and trends; cover 401, invalid range, empty data, and success.
2. Confirm failure.
3. Wire auth, repository, schemas, and routes.
4. Confirm green tests.
5. Commit: `feat: expose Fitbit health endpoints`.

### Task 7: Add sync job service and manual sync endpoint

**Files:**
- Create: `gateway/app/sync_service.py`
- Create: `gateway/app/routes/sync.py`
- Create: `gateway/tests/test_sync_routes.py`
- Modify: `gateway/app/main.py`

Steps:
1. Test that POST creates a job, duplicate running requests are rejected, status is queryable, and subprocess errors are sanitized.
2. Confirm failure.
3. Implement an allowlisted invocation of the existing Google Health sync script with a bounded background job and status store.
4. Confirm green tests.
5. Commit: `feat: add manual Fitbit synchronization`.

### Task 8: Add health-only Hermes chat adapter

**Files:**
- Create: `gateway/app/chat_service.py`
- Create: `gateway/app/routes/chat.py`
- Create: `gateway/tests/test_chat.py`

Steps:
1. Test prompt construction with selected metrics, refusal of non-health tool requests, timeout handling, and redaction of secrets.
2. Confirm failure.
3. Implement a narrow adapter to the configured Hermes health flow; never expose arbitrary shell/tool execution through the mobile API.
4. Confirm green tests.
5. Commit: `feat: add Fitbit health chat endpoint`.

### Task 9: Gateway operational setup

**Files:**
- Create: `gateway/pyproject.toml`
- Create: `gateway/README.md`
- Create: `gateway/scripts/run_gateway.ps1`
- Create: `gateway/tests/test_config.py`
- Modify: `.github/workflows/ci.yml`

Steps:
1. Test safe defaults and required production token.
2. Confirm failure.
3. Add packaging, local run instructions, Tailscale bind guidance, Windows firewall notes, and CI pytest/lint commands.
4. Confirm green tests and run `pytest gateway/tests -q`.
5. Commit: `chore: package health gateway`.

## Phase 2 — Native Android app

### Task 10: Create Android project shell

**Files:**
- Create: `android/settings.gradle.kts`
- Create: `android/build.gradle.kts`
- Create: `android/app/build.gradle.kts`
- Create: `android/app/src/main/AndroidManifest.xml`
- Create: `android/app/src/main/java/.../MainActivity.kt`
- Create: `android/app/src/test/.../ProjectSmokeTest.kt`

Steps:
1. Add a failing JVM smoke test for the application module.
2. Confirm failure because the project is absent.
3. Create a Kotlin/Compose Material 3 project using a current stable Android SDK and compile-safe dependency versions.
4. Run `./gradlew test`; expect PASS.
5. Commit: `chore: scaffold native Android app`.

### Task 11: Implement API client and secure settings

**Files:**
- Create: `android/app/src/main/java/.../data/api/HealthApi.kt`
- Create: `android/app/src/main/java/.../data/api/Models.kt`
- Create: `android/app/src/main/java/.../data/settings/AppSettings.kt`
- Create: `android/app/src/test/.../ApiModelsTest.kt`
- Create: `android/app/src/test/.../SettingsTest.kt`

Steps:
1. Test JSON parsing, URL normalization, and settings persistence without logging tokens.
2. Confirm failure.
3. Implement Retrofit DTOs, repository, encrypted settings abstraction, and connectivity error mapping.
4. Confirm green tests.
5. Commit: `feat: add Android gateway client`.

### Task 12: Build dashboard state and UI

**Files:**
- Create: `android/app/src/main/java/.../ui/dashboard/DashboardViewModel.kt`
- Create: `android/app/src/main/java/.../ui/dashboard/DashboardScreen.kt`
- Create: `android/app/src/test/.../DashboardViewModelTest.kt`
- Create: `android/app/src/androidTest/.../DashboardScreenTest.kt`

Steps:
1. Test loading, stale data, offline error, sync action, and successful dashboard state.
2. Confirm failure.
3. Implement StateFlow ViewModel and Compose cards with accessible labels.
4. Confirm JVM and Compose tests pass.
5. Commit: `feat: add health dashboard`.

### Task 13: Add charts and range selection

**Files:**
- Create: `android/app/src/main/java/.../ui/metrics/MetricDetailScreen.kt`
- Create: `android/app/src/main/java/.../ui/metrics/RangeSelector.kt`
- Create: `android/app/src/main/java/.../ui/charts/HealthCharts.kt`
- Create: `android/app/src/test/.../RangeSelectorTest.kt`
- Create: `android/app/src/androidTest/.../MetricDetailScreenTest.kt`

Steps:
1. Test presets 7d/30d/90d, custom date validation, empty and partial series.
2. Confirm failure.
3. Implement the selected maintained Compose chart library and line/bar charts with accessible summaries.
4. Confirm tests and debug APK build.
5. Commit: `feat: add health metric charts`.

### Task 14: Add sleep detail screen

**Files:**
- Create: `android/app/src/main/java/.../ui/sleep/SleepScreen.kt`
- Create: `android/app/src/main/java/.../ui/sleep/SleepViewModel.kt`
- Create: `android/app/src/test/.../SleepViewModelTest.kt`
- Create: `android/app/src/androidTest/.../SleepScreenTest.kt`

Steps:
1. Test local timezone display, phase totals, missing phase values, and cross-midnight session.
2. Confirm failure.
3. Implement sleep summary, phase chart, and trend view.
4. Confirm tests pass.
5. Commit: `feat: add sleep detail view`.

### Task 15: Add health-only chat screen

**Files:**
- Create: `android/app/src/main/java/.../ui/chat/HealthChatScreen.kt`
- Create: `android/app/src/main/java/.../ui/chat/HealthChatViewModel.kt`
- Create: `android/app/src/test/.../HealthChatViewModelTest.kt`
- Create: `android/app/src/androidTest/.../HealthChatScreenTest.kt`

Steps:
1. Test message sending, loading, retry, offline state, and medical disclaimer rendering.
2. Confirm failure.
3. Implement chat UI, bounded message history, source-date indicator, and no general Hermes tools.
4. Confirm tests pass.
5. Commit: `feat: add Fitbit health chat`.

### Task 16: Add settings and navigation

**Files:**
- Create: `android/app/src/main/java/.../ui/settings/SettingsScreen.kt`
- Create: `android/app/src/main/java/.../ui/navigation/AppNavigation.kt`
- Create: `android/app/src/androidTest/.../NavigationTest.kt`
- Modify: `MainActivity.kt`

Steps:
1. Test navigation destinations, save/test connection, clear credentials, and theme/unit preferences.
2. Confirm failure.
3. Implement navigation, settings, and secure credential clearing.
4. Confirm tests pass.
5. Commit: `feat: add app navigation and settings`.

## Phase 3 — Integration and release quality

### Task 17: Add end-to-end local test environment

**Files:**
- Create: `gateway/tests/fixtures/health.db`
- Create: `android/app/src/test/.../FakeGatewayTest.kt`
- Create: `docs/tailscale-setup.md`

Steps:
1. Test app against a deterministic fake gateway and verify dashboard/sleep/chat flows.
2. Confirm failure.
3. Implement fixture server and documented Tailscale setup with placeholders only.
4. Confirm gateway tests, Android tests, and debug build pass.
5. Commit: `test: add gateway and Android integration fixtures`.

### Task 18: CI, privacy scan, and release documentation

**Files:**
- Modify: `.github/workflows/ci.yml`, `README.md`
- Create: `SECURITY.md`, `CONTRIBUTING.md`
- Create: `scripts/check-secrets.ps1`

Steps:
1. Add failing CI/privacy checks for tracked secrets, build, backend tests, and Android tests.
2. Confirm failure where checks are absent.
3. Implement CI, secret scanning, contribution guide, threat model, and local release instructions.
4. Run all verification commands and confirm green.
5. Commit: `chore: add CI and privacy safeguards`.

### Task 19: Final review and branch handoff

Run:

```text
pytest gateway/tests -q
cd android && ./gradlew test lintDebug assembleDebug
```

Inspect the diff for personal data, tokens, fixed Tailscale addresses, unsafe logs, and undocumented assumptions. Then follow the finishing-branch workflow and present merge/push options.

## Execution order

Tasks 1–2 establish contracts; tasks 3–9 build and test the gateway; tasks 10–16 build the native app; tasks 17–19 verify integration and public-repository hygiene. No production credentials are required for CI.

## Open decisions before implementation

- Final public repository name/remote.
- Exact chart dependency version after checking current Android compatibility.
- Whether the Hermes chat adapter uses a local HTTP endpoint or a dedicated health-only CLI process.
- Whether to use a separate `gateway` repository later.

Until those decisions are confirmed, do not create application code or invoke subagents.
