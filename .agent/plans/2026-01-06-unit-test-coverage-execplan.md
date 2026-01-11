# Expand unit tests and add coverage reporting for refactoring

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This plan must be maintained in accordance with `/.agent/PLANS.md`.

## Purpose / Big Picture

After this change, the project will have broader unit test coverage over domain and data logic, and a reproducible way to generate code coverage reports for local JVM tests. A developer can run a single Gradle command to execute tests and produce a coverage report, then use that report to guide safe refactoring. Coverage is used as a signal to find untested code, not as the only indicator of test quality. In CI, GitHub Actions will generate coverage reports and retain them for one week.

## Progress

- [x] (2026-01-06 00:00Z) Created initial ExecPlan document with scoped tasks and validation steps.
- [ ] Add JaCoCo configuration and report tasks for local unit tests in `app/build.gradle.kts`.
- [ ] Expand unit tests in `app/src/test/java/com/example/sioribi/` for domain/data logic and edge cases.
- [ ] Capture baseline coverage report and set an initial coverage verification rule.
- [ ] Validate tests and coverage tasks on the project.

## Surprises & Discoveries

None yet.

## Decision Log

- Decision: Focus test expansion on local JVM unit tests for domain and data logic, and avoid new instrumented tests unless Android framework behavior is unavoidable.
  Rationale: Local tests run fast on the JVM and are best for logic; instrumented tests are slower and should be reserved for device-only behaviors.
  Date/Author: 2026-01-06, Codex

- Decision: Add JaCoCo reporting for local unit tests in the app module rather than setting up multi-module aggregation.
  Rationale: This repo has a single `app/` module, so module-level reports are sufficient and simpler.
  Date/Author: 2026-01-06, Codex

## Outcomes & Retrospective

Not completed yet.

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

## Validation and Acceptance

- Running `./gradlew testDebugUnitTest` succeeds with all unit tests passing.
- Running `./gradlew testDebugUnitTest jacocoTestReport` produces an HTML report under `app/build/reports/jacoco/` and an XML report suitable for CI consumption.
- The coverage verification task fails if the line coverage drops below the configured threshold and passes when coverage meets or exceeds it.
- Newly added unit tests cover at least one edge case per targeted class in `data/` and `domain/`.
- Coverage reports are used to identify gaps, and changes prioritize meaningful assertions over maximizing the percentage.
- GitHub Actions runs on push and pull request and uploads coverage report artifacts that are retained for seven days.

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

Issue Tracking Note: This plan is tracked in https://github.com/sotayamashita/sioribi/issues/2.
