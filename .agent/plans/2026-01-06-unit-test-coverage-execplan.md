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

## Surprises & Discoveries

- Observation: Gradle emitted deprecation warnings about `buildDir` access in the JaCoCo task setup.
  Evidence: `app/build.gradle.kts:86:37: 'getter for buildDir: File!' is deprecated.`

- Observation: After switching to `layout.buildDirectory`, the warning disappeared on the next run.
  Evidence: `./gradlew jacocoTestCoverageVerification` completed with no deprecation warnings.

- Observation: The pre-commit hook failed on `spotlessKotlinCheck` due to formatting in `YearProgressModelTest.kt`.
  Evidence: `spotlessKotlinCheck FAILED ... app/src/test/java/com/example/sioribi/domain/YearProgressModelTest.kt`.

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

- Decision: Replace `buildDir` usage in JaCoCo task inputs with `layout.buildDirectory`.
  Rationale: Gradle warns that `buildDir` access is deprecated; using the new API avoids future breakage.
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

## Validation and Acceptance

- Running `./gradlew testDebugUnitTest` succeeds with all unit tests passing.
- Running `./gradlew testDebugUnitTest jacocoTestReport` produces an HTML report under `app/build/reports/jacoco/` and an XML report suitable for CI consumption.
- The coverage verification task fails if the line coverage drops below the configured threshold and passes when coverage meets or exceeds it.
- Newly added unit tests cover at least one edge case per targeted class in `data/` and `domain/`.
- Coverage reports are used to identify gaps, and changes prioritize meaningful assertions over maximizing the percentage.
- GitHub Actions runs on push and pull request and uploads coverage report artifacts that are retained for seven days.

Validation update (2026-01-11 20:52JST): Not executed yet for the JaCoCo tasks; will run `./gradlew testDebugUnitTest jacocoTestReport` after expanding tests and capturing baseline.

Validation update (2026-01-11 20:52JST): `./gradlew testDebugUnitTest` passed, `./gradlew testDebugUnitTest jacocoTestReport` generated reports, and `./gradlew jacocoTestCoverageVerification` passed with a 0.33 line coverage minimum. HTML report confirmed at `app/build/reports/jacoco/jacocoTestReport/html/index.html`.

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

Issue Tracking Note: This plan is tracked in https://github.com/sotayamashita/sioribi/issues/2.
