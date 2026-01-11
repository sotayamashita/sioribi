# Expand unit tests and add coverage reporting for refactoring

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This plan must be maintained in accordance with `/.agent/PLANS.md`.

## Purpose / Big Picture

After this change, the project will have broader unit test coverage over domain and data logic, and a reproducible way to generate code coverage reports for local JVM tests. A developer can run a single Gradle command to execute tests and produce a coverage report, then use that report to guide safe refactoring. Coverage is used as a signal to find untested code, not as the only indicator of test quality. In CI, GitHub Actions will generate coverage reports and retain them for one week.

## Progress

- [x] (2026-01-06 00:00Z) Created initial ExecPlan document with scoped tasks and validation steps.
- [x] (2026-01-11 20:47JST) Start: Read the ExecPlan end-to-end and prepared to begin the first milestone.
- [x] (2026-01-11 20:56JST) Translate README and this ExecPlan entry to English to meet repo language requirements.
- [x] (2026-01-11 20:52JST) Add JaCoCo configuration and report/verification tasks for local unit tests in `app/build.gradle.kts`.
- [x] (2026-01-11 20:52JST) Expand unit tests in `app/src/test/java/com/example/sioribi/` for domain/data logic and edge cases.
- [x] (2026-01-11 20:52JST) Capture baseline coverage report and set an initial coverage verification rule.
- [x] (2026-01-11 20:52JST) Validate tests and coverage tasks on the project.
- [x] (2026-01-11 21:15JST) Phase 1-3 expansion: added additional data/domain tests, UI sizing edge cases, and a clock-injected scheduler helper with unit tests.
- [x] (2026-01-11 21:20JST) Phase 3 continuation: extracted `writeModelToPreferences` from `WidgetUpdateWorker` and added unit tests.
- [x] (2026-01-11 21:25JST) UI/DI coverage: extracted `resolveRefreshReason` helper and added receiver tests for null/unknown actions.

## Surprises & Discoveries

- Observation: Gradle emitted deprecation warnings about `buildDir` access in the JaCoCo task setup.
  Evidence: `app/build.gradle.kts:86:37: 'getter for buildDir: File!' is deprecated.`

- Observation: After switching to `layout.buildDirectory`, the warning disappeared on the next run.
  Evidence: `./gradlew jacocoTestCoverageVerification` completed with no deprecation warnings.

- Observation: The pre-commit hook failed on `spotlessKotlinCheck` due to formatting in `YearProgressModelTest.kt`.
  Evidence: `spotlessKotlinCheck FAILED ... app/src/test/java/com/example/sioribi/domain/YearProgressModelTest.kt`.

- Observation: The pre-commit hook stashed unstaged changes during the scheduler helper commit.
  Evidence: `[INFO] Stashing unstaged files to .../patch1768133831-30304.`

- Observation: The pre-commit hook stashed unstaged changes during the core test expansion commit.
  Evidence: `[INFO] Stashing unstaged files to .../patch1768133865-31180.`

- Observation: The pre-commit hook stashed unstaged changes during the widget preference writer commit.
  Evidence: `[INFO] Stashing unstaged files to .../patch1768134113-35029.`

- Observation: The pre-commit hook stashed unstaged changes during the JaCoCo baseline update commit.
  Evidence: `[INFO] Stashing unstaged files to .../patch1768134235-36969.`

- Observation: The pre-commit hook stashed unstaged changes during the refresh reason mapping commit.
  Evidence: `[INFO] Stashing unstaged files to .../patch1768134381-39333.`

- Observation: The initial `buildGridSize` expectation was incorrect and caused a unit test failure.
  Evidence: `YearProgressWidgetSizingTest > buildGridSize accounts for padding and footer FAILED`.

- Observation: The pre-commit hook stashed unstaged changes during the widget sizing helper commit.
  Evidence: `[INFO] Stashing unstaged files to .../patch1768134575-42303.`

## Decision Log

- Decision: Focus test expansion on local JVM unit tests for domain and data logic, and avoid new instrumented tests unless Android framework behavior is unavoidable.
  Rationale: Local tests run fast on the JVM and are best for logic; instrumented tests are slower and should be reserved for device-only behaviors.
  Date/Author: 2026-01-06, Codex

- Decision: Add JaCoCo reporting for local unit tests in the app module rather than setting up multi-module aggregation.
  Rationale: This repo has a single `app/` module, so module-level reports are sufficient and simpler.
  Date/Author: 2026-01-06, Codex

- Decision: Use JaCoCo tool version 0.8.11 and include both legacy and AGP 8 execution data paths for unit tests.
  Rationale: 0.8.11 supports Java 11 and is stable; execution data paths vary across AGP versions so dual paths avoid missing coverage.
  Date/Author: 2026-01-11, Codex

- Decision: Add a coverage verification task now with a placeholder minimum of 0.0, then tighten it after capturing the baseline.
  Rationale: This keeps the task wiring in place without failing the build before the baseline is established.
  Date/Author: 2026-01-11, Codex

- Decision: Inject a `Clock` into `SystemTimeDataSource` with a default system clock to enable deterministic JVM tests.
  Rationale: Using a fixed clock avoids flakiness while preserving production behavior.
  Date/Author: 2026-01-11, Codex

- Decision: Set the initial JaCoCo line coverage minimum to 0.33 based on the baseline report (146 covered / 440 total).
  Rationale: The baseline was 0.3318, so 0.33 keeps the verification meaningful without immediate failures.
  Date/Author: 2026-01-11, Codex

- Decision: Increase the JaCoCo line coverage minimum to 0.37 after regenerating the report (168 covered / 443 total).
  Rationale: The new baseline is 0.3792, so 0.37 keeps the gate meaningful while allowing small fluctuations.
  Date/Author: 2026-01-11, Codex

- Decision: Replace `buildDir` usage in JaCoCo task inputs with `layout.buildDirectory`.
  Rationale: Gradle warns that `buildDir` access is deprecated; using the new API avoids future breakage.
  Date/Author: 2026-01-11, Codex

- Decision: For the next Phase 1-3 expansion, avoid introducing Hilt and prefer constructor injection with fakes; add clock-injected helpers for time-based logic.
  Rationale: The testing guidance states Hilt is unnecessary for unit tests and recommends fakes; this keeps tests fast and focused on JVM logic.
  Date/Author: 2026-01-11, Codex

- Decision: Extract `computeInitialDelayMillis` in `WidgetUpdateScheduler` to accept a `Clock` and keep `scheduleDaily` behavior unchanged.
  Rationale: A clock-injected helper makes time-based logic testable in JVM unit tests without Robolectric or Hilt.
  Date/Author: 2026-01-11, Codex

- Decision: Extract `writeModelToPreferences` from `WidgetUpdateWorker` for JVM unit testing of preference updates.
  Rationale: The preference write logic is pure Kotlin and can be tested without Android framework dependencies.
  Date/Author: 2026-01-11, Codex

- Decision: Extract `resolveRefreshReason` from `WidgetRefreshReceiver` for pure Kotlin tests of intent-to-reason mapping.
  Rationale: The mapping logic is deterministic and increases UI/DI coverage without Android framework dependencies.
  Date/Author: 2026-01-11, Codex

- Decision: Expose `resolveEffectiveSize`, `buildGridLayout`, and `buildGridSize` as internal helpers for JVM tests.
  Rationale: These helpers are pure Kotlin sizing logic and can be tested without Android dependencies.
  Date/Author: 2026-01-11, Codex

## Outcomes & Retrospective

Milestone complete (2026-01-11 20:52JST): Added JVM tests around domain/data logic, JaCoCo report + verification tasks, and CI artifact upload. Coverage verification now enforces a 0.33 line ratio baseline while leaving room for future improvements.

## Context and Orientation

This repository is a single Android app module at `app/`. Production Kotlin sources live under `app/src/main/java/com/example/sioribi/` with subpackages:

- `data/` for data sources (for example `TimeDataSource`, `SystemTimeDataSource`).
- `domain/` for core business logic (for example `GetYearProgressUseCase`, `YearProgressModel`).
- `ui/` for widget scheduling, refresh, and Compose widget logic (for example `WidgetRefreshCoordinator`, `WidgetUpdateWorker`).

Local JVM unit tests live under `app/src/test/java/com/example/sioribi/`. Existing tests include `GetYearProgressUseCaseTest`, `WidgetGridSizingTest`, `WidgetRefreshReceiverTest`, and `WidgetRefreshCoordinatorTest`.

In this plan, "local unit test" means a JUnit test that runs on the host JVM without Android framework dependencies. "Instrumented test" means a test that runs on a device or emulator and can use the Android framework.

## Plan of Work

First, add coverage tooling for local unit tests. Apply the Gradle JaCoCo plugin in `app/build.gradle.kts`, then define a `JacocoReport` task that reads the execution data from the `testDebugUnitTest` task and emits HTML and XML reports into `app/build/reports/jacoco/`. Wire the report task so it runs after `testDebugUnitTest` and also depends on it when invoked directly. Add a `JacocoCoverageVerification` task with a rule that uses a baseline coverage threshold. Establish the baseline by running tests once, generating the report, and recording the overall line coverage percentage. Set the initial rule slightly below or equal to the baseline to avoid breaking the build immediately, with a note that the threshold will be raised as tests grow. Treat the coverage number as a guide to find gaps rather than a standalone pass/fail indicator of test quality.

Third, add CI coverage reporting in GitHub Actions. Create or update a workflow under `.github/workflows/` to run `./gradlew testDebugUnitTest jacocoTestReport` on push and pull requests. Upload the JaCoCo HTML and XML outputs as workflow artifacts, and set the artifact retention to seven days so the report can be reviewed for one week after each run.

In the GitHub Actions workflow, after the tests complete, add a step to upload the coverage report artifacts using `@actions/upload-artifact`.

Second, expand unit tests for domain and data logic. In `app/src/test/java/com/example/sioribi/domain/`, add cases for edge conditions for `GetYearProgressUseCase` and `YearProgressModel` (for example, boundaries at start/end of year, leap year day handling, and negative or out-of-range inputs). In `app/src/test/java/com/example/sioribi/data/`, add tests for `SystemTimeDataSource` by injecting a fake `TimeDataSource` or by refactoring to allow a fake clock dependency so that tests do not depend on the system clock. In `app/src/test/java/com/example/sioribi/ui/`, add missing pure-logic tests for classes that do not require Android framework types; for anything that currently depends on Android framework classes, extract small pure Kotlin helpers and unit-test those helpers instead of adding instrumented tests. Keep the overall test mix skewed toward fast local tests rather than slower device tests.

Finally, update documentation notes in the repository (for example in `README.md` or `docs/`) to record how to run unit tests and generate coverage reports, including the Gradle commands and the report output locations.

Phase 1-3 test expansion plan (planning only, no implementation in this step): prioritize data/domain unit tests and pure Kotlin UI logic tests, while avoiding Hilt for unit tests per the architecture guidance. Use constructor injection with fakes to isolate dependencies. For Android framework-bound logic, introduce small pure Kotlin helpers or injectable time sources to make calculations unit-testable. Specifically, add a helper in `app/src/main/java/com/example/sioribi/ui/WidgetUpdateScheduler.kt` that calculates the initial delay using an injected `Clock`, and have `scheduleDaily` call it so that unit tests can cover edge cases around midnight without instrumented tests.

Phase 1 (data/domain, highest priority): add unit tests for `SystemTimeDataSource` covering date boundaries and time zone offsets using injected `Clock` instances (for example UTC vs. local offsets) to validate deterministic behavior. Expand `GetYearProgressUseCase` tests to cover additional year boundaries and rounding behavior (start/end of year, leap-day, and mid-year rounding cases). Add a simple `YearProgressModel` test that verifies values are preserved as-is to protect against unintended validation logic.

Phase 2 (UI pure logic): expand `WidgetGridSizing.kt` tests for edge cases where widget sizes are zero or negative, and validate fallbacks (minimum columns/rows, dot size floor). Add tests for `pickLargestSize` when `sizes` is null or contains invalid entries, ensuring fallback is returned. These tests remain JVM-only and avoid Android framework types beyond `Dp`/`DpSize`.

Phase 3 (light refactor to enable tests): refactor `WidgetUpdateScheduler` by extracting delay calculation into a pure Kotlin helper that accepts a `Clock` and zone (or derives zone from the clock) and returns the initial delay in milliseconds. Add unit tests for that helper to cover midnight boundary cases (just before midnight, exactly at midnight) and verify the delay is clamped to non-negative values. If coverage gaps remain in `WidgetUpdateWorker` and the logic is still Android-bound, extract the preference-write logic into a pure function and unit-test it separately; avoid instrumented tests unless unavoidable.

## Concrete Steps

Run the following from the repository root:

    ./gradlew testDebugUnitTest

After the first run, inspect the unit test execution data and reports locations:

    ls app/build/jacoco
    ls app/build/reports/jacoco

Add JaCoCo tasks in `app/build.gradle.kts`, then run:

    ./gradlew testDebugUnitTest jacocoTestReport

If the coverage verification task is added, run:

    ./gradlew jacocoTestCoverageVerification

For CI, add a workflow (or update an existing one) and verify it uploads artifacts with a seven-day retention:

    .github/workflows/coverage.yml
    uses: actions/upload-artifact with retention-days: 7

Before opening the pull request, create a new branch for this work. When creating the PR, include `resolve #2` in the PR description so the issue is linked and closed on merge.

Concrete Steps update (2026-01-11 20:52JST): No Gradle commands run yet for this milestone. Implemented JaCoCo plugin + tasks in `app/build.gradle.kts`.

Concrete Steps update (2026-01-11 20:52JST): Ran unit tests, generated the JaCoCo report, computed baseline coverage, and verified coverage.

    Working directory: /Users/sotayamashita/AndroidStudioProjects/koyomidots
    ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 11s

    ./gradlew testDebugUnitTest jacocoTestReport
    > Task :app:jacocoTestReport
    BUILD SUCCESSFUL in 1s

    python - <<'PY'
    import xml.etree.ElementTree as ET
    from pathlib import Path
    path = Path('app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml')
    root = ET.parse(path).getroot()
    line_counter = next(c for c in root.findall('counter') if c.get('type') == 'LINE')
    missed = int(line_counter.get('missed'))
    covered = int(line_counter.get('covered'))
    ratio = covered / (covered + missed)
    print(f"missed={missed} covered={covered} ratio={ratio:.4f}")
    PY
    missed=294 covered=146 ratio=0.3318

    ./gradlew jacocoTestCoverageVerification
    > Task :app:jacocoTestCoverageVerification
    BUILD SUCCESSFUL in 1s

    (after switching to layout.buildDirectory)
    ./gradlew jacocoTestCoverageVerification
    > Task :app:jacocoTestCoverageVerification UP-TO-DATE
    BUILD SUCCESSFUL in 1s

    ./gradlew spotlessApply
    > Task :app:spotlessApply
    BUILD SUCCESSFUL in 1s

    git commit -m "feat(test): add jacoco coverage and unit test expansions"
    [feat/unit-test-coverage 85ecd29] feat(test): add jacoco coverage and unit test expansions

    ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 2s

    ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 2s

    ./gradlew spotlessApply
    BUILD SUCCESSFUL in 1s

    git commit -m "refactor(ui): extract scheduler delay helper"
    [feat/unit-test-coverage c39070d] refactor(ui): extract scheduler delay helper

    git commit -m "test: expand unit coverage for core logic"
    [feat/unit-test-coverage 3b3133a] test: expand unit coverage for core logic

Concrete Steps update (2026-01-11 21:15JST): Added tests in data/domain/ui, introduced `computeInitialDelayMillis` helper, and ran unit tests after each batch.

    Working directory: /Users/sotayamashita/AndroidStudioProjects/koyomidots
    Edited:
      - app/src/test/java/com/example/sioribi/data/SystemTimeDataSourceTest.kt
      - app/src/test/java/com/example/sioribi/domain/GetYearProgressUseCaseTest.kt
      - app/src/test/java/com/example/sioribi/ui/WidgetGridSizingTest.kt
      - app/src/main/java/com/example/sioribi/ui/WidgetUpdateScheduler.kt
      - app/src/test/java/com/example/sioribi/ui/WidgetUpdateSchedulerTest.kt

    ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 2s

    ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 2s

    ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 2s

    ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 2s

    ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 2s

    ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 2s

Concrete Steps update (2026-01-11 21:20JST): Extracted `writeModelToPreferences` and added `WidgetUpdateWorkerTest`, then reran unit tests.

    Working directory: /Users/sotayamashita/AndroidStudioProjects/koyomidots
    Edited:
      - app/src/main/java/com/example/sioribi/ui/WidgetUpdateWorker.kt
      - app/src/test/java/com/example/sioribi/ui/WidgetUpdateWorkerTest.kt

    ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 2s

Concrete Steps update (2026-01-11 21:23JST): Regenerated JaCoCo report, recalculated the baseline, updated the coverage threshold, and verified coverage.

    Working directory: /Users/sotayamashita/AndroidStudioProjects/koyomidots
    ./gradlew testDebugUnitTest jacocoTestReport
    > Task :app:jacocoTestReport
    BUILD SUCCESSFUL in 1s

    python - <<'PY'
    import xml.etree.ElementTree as ET
    from pathlib import Path
    path = Path('app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml')
    root = ET.parse(path).getroot()
    line_counter = next(c for c in root.findall('counter') if c.get('type') == 'LINE')
    missed = int(line_counter.get('missed'))
    covered = int(line_counter.get('covered'))
    ratio = covered / (covered + missed)
    print(f"missed={missed} covered={covered} ratio={ratio:.4f}")
    PY
    missed=275 covered=168 ratio=0.3792

    ./gradlew jacocoTestCoverageVerification
    > Task :app:jacocoTestCoverageVerification
    BUILD SUCCESSFUL in 2s

    git commit -m "chore: raise jacoco coverage baseline"
    [feat/unit-test-coverage cd6a520] chore: raise jacoco coverage baseline

Concrete Steps update (2026-01-11 21:25JST): Extracted `resolveRefreshReason`, expanded receiver tests, and reran unit tests.

    Working directory: /Users/sotayamashita/AndroidStudioProjects/koyomidots
    Edited:
      - app/src/main/java/com/example/sioribi/ui/WidgetRefreshReceiver.kt
      - app/src/test/java/com/example/sioribi/ui/WidgetRefreshReceiverTest.kt

    ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 3s

    ./gradlew spotlessApply
    BUILD SUCCESSFUL in 1s

    git commit -m "test(ui): cover refresh reason mapping"
    [feat/unit-test-coverage 38b57e6] test(ui): cover refresh reason mapping

Concrete Steps update (2026-01-11 21:28JST): Exposed widget sizing helpers, added sizing tests, fixed expectation, and reran unit tests.

    Working directory: /Users/sotayamashita/AndroidStudioProjects/koyomidots
    Edited:
      - app/src/main/java/com/example/sioribi/ui/YearProgressWidget.kt
      - app/src/test/java/com/example/sioribi/ui/YearProgressWidgetSizingTest.kt

    ./gradlew testDebugUnitTest
    BUILD FAILED in 3s

    ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 2s

    ./gradlew spotlessApply
    BUILD SUCCESSFUL in 1s

    git commit -m "test(ui): add widget sizing helpers coverage"
    [feat/unit-test-coverage 9360dfc] test(ui): add widget sizing helpers coverage

    git commit -m "refactor(ui): extract widget preference writer"
    [feat/unit-test-coverage 23f4497] refactor(ui): extract widget preference writer

## Validation and Acceptance

- Running `./gradlew testDebugUnitTest` succeeds with all unit tests passing.
- Running `./gradlew testDebugUnitTest jacocoTestReport` produces an HTML report under `app/build/reports/jacoco/` and an XML report suitable for CI consumption.
- The coverage verification task fails if the line coverage drops below the configured threshold and passes when coverage meets or exceeds it.
- Newly added unit tests cover at least one edge case per targeted class in `data/` and `domain/`.
- Coverage reports are used to identify gaps, and changes prioritize meaningful assertions over maximizing the percentage.
- GitHub Actions runs on push and pull request and uploads coverage report artifacts that are retained for seven days.

Validation update (2026-01-11 20:52JST): Not executed yet for the JaCoCo tasks; will run `./gradlew testDebugUnitTest jacocoTestReport` after expanding tests and capturing baseline.

Validation update (2026-01-11 20:52JST): `./gradlew testDebugUnitTest` passed, `./gradlew testDebugUnitTest jacocoTestReport` generated reports, and `./gradlew jacocoTestCoverageVerification` passed with a 0.33 line coverage minimum. HTML report confirmed at `app/build/reports/jacoco/jacocoTestReport/html/index.html`.

Validation update (2026-01-11 21:15JST): Re-ran `./gradlew testDebugUnitTest` after each added test batch; all runs passed.

Validation update (2026-01-11 21:20JST): `./gradlew testDebugUnitTest` passed after extracting `writeModelToPreferences` and adding `WidgetUpdateWorkerTest`.

Validation update (2026-01-11 21:23JST): `./gradlew testDebugUnitTest jacocoTestReport` produced updated reports and `./gradlew jacocoTestCoverageVerification` passed with a 0.37 line coverage minimum.

Validation update (2026-01-11 21:25JST): `./gradlew testDebugUnitTest` passed after adding `resolveRefreshReason` and receiver tests.

Validation update (2026-01-11 21:28JST): `./gradlew testDebugUnitTest` passed after correcting the sizing test expectation.

## Idempotence and Recovery

All Gradle tasks are safe to run repeatedly. If the JaCoCo report task is skipped due to missing execution data, re-run `./gradlew testDebugUnitTest` and then the report task. If coverage verification fails after adding the rule, regenerate the report and adjust the threshold to the recorded baseline before re-running verification.

## Artifacts and Notes

Example of a successful report task output:

    > Task :app:jacocoTestReport
    BUILD SUCCESSFUL in 20s

Example of where to find the HTML report:

    app/build/reports/jacoco/jacocoTestReport/html/index.html

## Interfaces and Dependencies

Testing uses JUnit 4 with Truth and MockK (already declared in `app/build.gradle.kts`). Coverage reporting uses the Gradle JaCoCo plugin applied in the app module. The report task should depend on `testDebugUnitTest` so that the coverage data exists, and the verification task should enforce a simple line coverage rule at the bundle level. CI coverage artifacts should be uploaded with GitHub Actions using `actions/upload-artifact` and `retention-days: 7`.

Plan Change Note: Updated the plan to explicitly state that coverage is a gap-finding signal rather than a sole quality metric, and to mention skewing toward fast local tests. Added a CI coverage artifact retention requirement (seven days) for GitHub Actions.

Plan Change Note (2026-01-11 20:52JST): Logged milestone-1 completion, added JaCoCo versioning/exec-path decisions, and noted that no validation commands have been run yet.

Plan Change Note (2026-01-11 20:52JST): Recorded added unit tests, baseline coverage results, coverage verification threshold update, CI artifact upload changes, and validation outcomes.

Plan Change Note (2026-01-11 20:52JST): Updated JaCoCo task inputs to use `layout.buildDirectory`, captured the warning disappearance, and recorded the decision.

Plan Change Note (2026-01-11 20:52JST): Recorded the Spotless formatting fix required by the git hook and the resulting `spotlessApply` run.

Plan Change Note (2026-01-11 20:52JST): Added the git commit entry to Concrete Steps after the successful hook run.

Plan Change Note (2026-01-11 20:56JST): Translated README.md and this ExecPlan entry to English per language requirements.

Plan Change Note (2026-01-11 21:10JST): Added the Phase 1-3 test expansion plan aligned with the local testing and Hilt guidance.

Plan Change Note (2026-01-11 21:13JST): Added concrete Phase 1-3 test targets and refactor steps (data/domain, UI sizing, and clock-injected scheduler helper) as requested.

Plan Change Note (2026-01-11 21:15JST): Recorded Phase 1-3 implementation progress, scheduler helper decision, and repeated unit test validations.

Plan Change Note (2026-01-11 21:16JST): Ran Spotless to ensure formatting after the latest test additions.

Plan Change Note (2026-01-11 21:17JST): Recorded the scheduler helper commit and noted the pre-commit stash behavior.

Plan Change Note (2026-01-11 21:17JST): Recorded the core unit test expansion commit and noted the pre-commit stash behavior.

Plan Change Note (2026-01-11 21:20JST): Recorded the `WidgetUpdateWorker` helper extraction, new test addition, and validation run.

Plan Change Note (2026-01-11 21:22JST): Recorded the widget preference writer commit and pre-commit stash behavior.

Plan Change Note (2026-01-11 21:23JST): Updated the JaCoCo baseline and raised the coverage threshold to 0.37 after regenerating the report.

Plan Change Note (2026-01-11 21:24JST): Recorded the JaCoCo baseline commit and pre-commit stash behavior.

Plan Change Note (2026-01-11 21:25JST): Added the `resolveRefreshReason` helper and related tests for UI/DI coverage.

Plan Change Note (2026-01-11 21:26JST): Ran Spotless after updating receiver tests.

Plan Change Note (2026-01-11 21:26JST): Recorded the refresh reason mapping commit and pre-commit stash behavior.

Plan Change Note (2026-01-11 21:28JST): Added widget sizing helper exposure and tests, plus the corrected expectation after the initial failure.

Plan Change Note (2026-01-11 21:29JST): Ran Spotless after adding widget sizing tests.

Plan Change Note (2026-01-11 21:29JST): Recorded the widget sizing helper commit and pre-commit stash behavior.

Issue Tracking Note: This plan is tracked in repository issue #2.
