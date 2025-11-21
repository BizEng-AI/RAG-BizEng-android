# Android “I Can Ship” Readiness Checklist

Use this to drive a release go/no‑go. Keep items unchecked until verified.

## Go / No‑Go Criteria
- [ ] No known crashes in normal user flows
- [ ] Core flows verified on devices:
  - [ ] Low‑end device (2–4 GB RAM)
  - [ ] Mid/high device
  - [ ] Android 11
  - [ ] Android 12
  - [ ] Android 13+
- [ ] No obvious UI issues on small and large/tablet screens
- [ ] No blocking performance issues (no freezes; no >3–4s lags in key screens)
- [ ] No critical bugs in:
  - [ ] Login/registration
  - [ ] Payments (if applicable)
  - [ ] “Money pages” / most important features

## Automated Testing (Minimum Viable Pyramid)
### Overview
Target: Fast feedback for logic, confidence for data flows, smoke coverage for UI.
Run unit tests on every commit; integration/UI at least before release candidate.

### 1. Unit Tests (fast, pure JVM)
Focus: logic, mapping, validation, business rules.
Core areas:
- [ ] Email/password validation utilities
- [ ] Token refresh mutex logic (single refresh on concurrent 401s)
- [ ] DTO → domain mapping (Auth, Attempts, Admin summaries)
- [ ] Role/permission checks (isAdmin, feature gating)
- [ ] Time/duration formatting (seconds → mm:ss)
- [ ] Score normalization (0–1 → % display) & edge cases (null scores)
- [ ] Attempt aggregation (summary calculations)
- [ ] Offline cache merge logic (Room + remote refresh)
- [ ] Back navigation stack rules (single-level pop, exit on root)
- [ ] AuthStorage / InMemoryAuthStorage behavior (save/clear)

Instrumentation:
- JUnit5/JUnit4 + MockK (spies/mocks) + kotlinx-coroutines-test.
Execution:
```powershell
./gradlew :app:testDebugUnitTest
```

### 2. Integration Tests (JVM + Mock Web Server or Instrumented)
Focus: component interaction (Repository ↔ Network ↔ Cache).
- [ ] Auth flow: expired access token triggers refresh exactly once
- [ ] LoginRepository writes tokens + user info to storage
- [ ] TrackingRepository returns cached attempts then refreshes
- [ ] AdminRepository handles 401 → refresh → retry
- [ ] Room persistence: attempts inserted & queried sorted by time
- [ ] Profile screen repository: summary + attempts combined
- [ ] Roleplay start/finish attempt lifecycle (POST then PATCH) with timing

Tools:
- MockWebServer (simulate 200/401/500)
- In-memory Room (Room.inMemoryDatabaseBuilder)
- Coroutines TestDispatcher.
Execution:
```powershell
./gradlew :app:testDebugUnitTest --tests "*IntegrationTest"
```
(Or instrumented if needed: `./gradlew connectedDebugAndroidTest`)

### 3. UI Tests (Compose/Espresso – smoke only)
Focus: guard rails for critical flows.
- [ ] Login success navigates to main tabs
- [ ] Invalid credentials show user-friendly error (no debug stack)
- [ ] Registration flow (enter data → success → auto-login or redirect)
- [ ] Admin dashboard overview renders totals & refresh icon rotates
- [ ] Student profile shows summary counts & attempts list scrolls
- [ ] Pronunciation practice: record button state changes & result panel expands
- [ ] Roleplay scenario selection scrolls & starts scenario
- [ ] Back button behavior: child → parent; root → exit

Execution (instrumented on emulator/device):
```powershell
./gradlew :app:connectedDebugAndroidTest --tests "*UITest"
```

### 4. Test Data & Utilities
- [ ] Test fixtures for TokenResponse (valid/expired)
- [ ] Factory for AttemptDto (parameterized types/duration)
- [ ] Fake clock (deterministic timestamps)
- [ ] JSON samples for admin endpoints stored in `test/resources/admin/`
- [ ] Helper assert for mm:ss formatting edge cases (0 / <10s / >1h)

### 5. Coverage Priorities (do first)
1. Auth refresh mutex
2. Summary aggregation accuracy
3. Offline-first repository emission order (cache → remote)
4. Back navigation stack integrity
5. Admin overview numbers mapping

### 6. Failure Handling Tests
- [ ] Network timeout surfaces retry UI state
- [ ] 401 w/out refresh token → forced logout
- [ ] 403 on admin endpoint → access restricted view
- [ ] Empty attempts list → placeholder text
- [ ] Null pronunciation score → N/A display

### 7. Performance Guard (lightweight)
- [ ] Repository returns first cached emission <150ms
- [ ] Large attempts list (200 items) scroll test – no jank (manual + optional benchmark)

### 8. CI Readiness (future)
- [ ] Script to run unit tests on pre-push
- [ ] Aggregate test report exported (JUnit XML)
- [ ] Flaky test quarantine list maintained

## Manual Testing Scenarios
- [ ] New user: install → register → login → first exercise
- [ ] Returning user: resume → logout
- [ ] Wrong password handling
- [ ] Permission checks (grant/deny/never ask again)
- [ ] Dirty environment
  - [ ] Lock/unlock mid-task; rotate; app switch
  - [ ] Airplane mode toggle during load/send
  - [ ] Kill app & reopen
  - [ ] Low battery
  - [ ] Low storage
- [ ] Data/state
  - [ ] Login persists across restarts
  - [ ] Logout clears tokens and caches
  - [ ] Exercise results persist after kill/restart
  - [ ] Settings persist (including after device restart)

## Device & OS Coverage
- [ ] Small/low‑end phone
- [ ] Midrange phone
- [ ] Large/tablet (if applicable)
- [ ] Android 11 / 12 / 13 / 14 covered
- [ ] Fonts not overlapping/cut off; buttons fully on‑screen
- [ ] Localized strings render correctly
- [ ] Font scale/accessibility OK (1.5x–2.0x, talkback basic pass)

## Performance & Stability
- [ ] Logcat monitored during stress (no ANRs, no repeated big stack traces)
- [ ] Lists: fast scroll is smooth; no jank
- [ ] Network timeouts handled with retry/backoff
- [ ] No main‑thread blocking (strict mode/ANR safe)
- [ ] Cold start acceptable; key screens load <3s on low‑end device

## Security Basics
- [ ] Secure token storage (e.g., EncryptedSharedPreferences/Keystore)
- [ ] Auth enforced server‑side for protected endpoints
- [ ] Rate limits on auth endpoints (login/OTP/password reset)

## Beta Testing
- [ ] Play internal/closed testing track set up
- [ ] 5–20 testers added with clear instructions
- [ ] Crash capture enabled; feedback form for confusing UX
- [ ] Feedback review cadence defined pre‑release

## Release & Monitoring
- [ ] Crashlytics integrated and reporting in test tracks
- [ ] Top crash fix loop defined (SLA, owner)
- [ ] API error logging/alerting in place
- [ ] Post‑launch monitoring plan (first 48–72h) with on‑call owner

## How to run tests locally (Windows/PowerShell)
- Ensure one emulator is running or a device is connected (adb authorized).
- From project root, run:
```powershell
# Clean + unit tests (all modules)
./gradlew.bat clean test

# Instrumented UI/integration tests on connected device/emulator
./gradlew.bat connectedAndroidTest

# Optional: run only app module unit tests
./gradlew.bat :app:testDebugUnitTest

# Build a debug APK for manual testing
./gradlew.bat :app:assembleDebug
```

## Status (Fill before go/no‑go)
| Area | Owner | Status (☐/☑) | Notes |
| --- | --- | --- | --- |
| Core flows stability |  | ☐ |  |
| Device & OS coverage |  | ☐ |  |
| UI on small/large screens |  | ☐ |  |
| Performance & ANR checks |  | ☐ |  |
| Unit tests |  | ☐ |  |
| Integration tests |  | ☐ |  |
| UI tests |  | ☐ |  |
| Manual scenarios |  | ☐ |  |
| Security basics |  | ☐ |  |
| Beta testing |  | ☐ |  |
| Crashlytics & monitoring |  | ☐ |  |

### Decision
- [ ] Ready to ship (meets all mandatory gates)
