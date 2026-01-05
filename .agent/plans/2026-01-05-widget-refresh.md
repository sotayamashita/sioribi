# Design Widget Refresh Orchestration

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This repository contains `.agent/PLANS.md`. This ExecPlan must be maintained in accordance with `.agent/PLANS.md`.

## Purpose / Big Picture

After this change, the Year Progress widget updates its “year” and “X/Y” values quickly and reliably, no matter how it is invoked. A user can add the widget and see the correct values immediately, and the widget will refresh on all relevant lifecycle events (add/update, first render, manual tap, time/date/timezone changes, reboot, app update, and periodic schedule). This improves perceived responsiveness without duplicating update logic across entry points.

## Progress

- [x] (2026-01-05 18:30JST) Created this ExecPlan to capture the multi-trigger refresh architecture before implementation.
- [x] (2026-01-05 19:05JST) Mapped all entry points to a single refresh coordinator and documented the trigger list.
- [x] (2026-01-05 19:05JST) Defined the coordinator API, WorkManager enqueue policy, and input data contract.
- [x] (2026-01-05 19:05JST) Specified manifest changes for system broadcast triggers and widget receiver behaviors.
- [x] (2026-01-05 19:05JST) Defined unit test strategy for coordinator and receiver behaviors.
- [x] (2026-01-05 19:05JST) Chose coordinator placement in `ui` and AppGraph dependency shape for testability.
- [x] (2026-01-05 19:18JST) Confirmed coordinator as class injected via AppGraph and system events handled by a new dedicated receiver.
- [x] (2026-01-05 13:32JST) Started implementation by re-reading ExecPlan and PLANS.md to align with formatting and workflow requirements.
- [x] (2026-01-05 13:38JST) Implemented the coordinator, AppGraph instance wiring, widget/system receiver changes, manifest updates, periodic input data, and worker logging.
- [x] (2026-01-05 13:38JST) Added unit tests for coordinator delegation and system receiver action mapping.
- [x] (2026-01-05 13:42JST) Investigated the Gradle test failure with --stacktrace; confirmed JavaVersion parsing error for `25.0.1` and re-ran tests (still failing).
- [x] (2026-01-05 13:45JST) Investigated local Java toolchain selection; confirmed `java` resolves to OpenJDK 25.0.1 despite mise config specifying Corretto 21.
- [x] (2026-01-05 14:01JST) Ran `mise trust` and attempted activation; `java -version` still reported 25.0.1 (activation command executed in background).
- [x] (2026-01-05 14:03JST) Verified `eval "$(mise activate bash)"` switches to Corretto 21, then re-ran tests and hit a compile error in `WidgetRefreshReceiverTest`.
- [x] (2026-01-05 14:12JST) Reworked `WidgetRefreshReceiverTest` to mock `Intent` and `Context`, then re-ran unit tests successfully under Corretto 21.
- [x] (2026-01-05 14:14JST) Validated on device that values appear immediately after widget add and on time change events.
- [x] (2026-01-05 14:14JST) Built `assembleDebug` successfully under Corretto 21.

## Surprises & Discoveries

- Observation: `./gradlew testDebugUnitTest` failed during settings evaluation with the message `25.0.1`.
  Evidence: `FAILURE: Build failed with an exception. * What went wrong: 25.0.1`

- Observation: The stacktrace shows `java.lang.IllegalArgumentException: 25.0.1` in Kotlin's `JavaVersion.parse`, implying the current JDK reports `25.0.1` which the embedded Kotlin tooling does not accept.
  Evidence: `java.lang.IllegalArgumentException: 25.0.1 at ... JavaVersion.parse(JavaVersion.java:307)`

- Observation: The shell resolves `java` to `/usr/bin/java` reporting OpenJDK 25.0.1, while mise reports the current project Java as `corretto-21.0.9.10.1` and `JAVA_HOME` points at a mise install for 25.0.1.
  Evidence: `which java -> /usr/bin/java`, `java -version -> openjdk version "25.0.1"`, `JAVA_HOME=/Users/sotayamashita/.local/share/mise/installs/java/25.0.1`, `mise current java -> corretto-21.0.9.10.1`.

- Observation: `eval "$(mise activate bash)" & java -version` still reported 25.0.1 because the activation ran in the background, so it did not affect the subsequent `java -version` in the current shell.
  Evidence: `java -version -> openjdk version "25.0.1"` after the backgrounded activation command.

- Observation: With Corretto 21 active, unit tests progressed but failed compilation because `android.test.mock.MockContext` is unresolved in JVM unit tests.
  Evidence: `WidgetRefreshReceiverTest.kt:4:16 Unresolved reference: test` and `WidgetRefreshReceiverTest.kt:14:27 Unresolved reference: MockContext`.

- Observation: JVM unit tests cannot rely on Android framework `Intent` implementations; mocking avoids "not mocked" runtime exceptions.
  Evidence: After replacing real `Intent` construction with MockK stubs, `./gradlew testDebugUnitTest` succeeded.

## Decision Log

- Decision: Centralize all refresh triggers into a single coordinator that enqueues the existing worker.
  Rationale: App components are short-lived, so the refresh logic should live in one place, while entry points simply trigger it.
  Date/Author: 2026-01-05, Codex.

- Decision: Place `WidgetRefreshCoordinator` in the `ui` package and inject its dependencies via `AppGraph`.
  Rationale: The coordinator is Android/platform-facing (WorkManager, Context) and should not live in domain; placing it in `ui` keeps domain pure while AppGraph provides testable dependency wiring.
  Date/Author: 2026-01-05, Codex.

- Decision: Implement `WidgetRefreshCoordinator` as a class (not an object) and wire it through `AppGraph`; route system events through a dedicated `WidgetRefreshReceiver`.
  Rationale: A class makes dependency injection and unit testing straightforward, and a dedicated receiver keeps system event handling isolated from Glance widget receiver concerns.
  Date/Author: 2026-01-05, Codex.

- Decision: Treat AppGraph as an application-owned container instance (not a global singleton object) and prefer fake enqueuers in coordinator tests.
  Rationale: The manual DI guide recommends an Application-held container to manage dependencies, and the testing guidance prefers fakes to mocks for unit tests.
  Date/Author: 2026-01-05, Codex.

- Decision: Use an overridable `WidgetRefreshCoordinatorProvider` for receivers/actions to supply the coordinator.
  Rationale: Unit tests can swap in a fake coordinator without requiring a real Application context while production still reads from `KoyomiDotsApplication.appGraph`.
  Date/Author: 2026-01-05, Codex.

- Decision: Trigger the FirstRender refresh from `YearProgressWidget` via `LaunchedEffect` when stored preferences are empty.
  Rationale: The widget can detect missing data on initial render and request a refresh while WorkManager dedupes repeated triggers.
  Date/Author: 2026-01-05, Codex.

## Outcomes & Retrospective

No outcomes yet; this plan documents the design and required work before implementation.

## Context and Orientation

The widget renders its content in `app/src/main/java/com/example/koyomidots/ui/YearProgressWidget.kt` using Glance preferences state. The state is written by `app/src/main/java/com/example/koyomidots/ui/WidgetUpdateWorker.kt`, which runs via WorkManager and currently only updates on the periodic schedule and manual tap. The app initializes dependencies and schedules the daily worker in `app/src/main/java/com/example/koyomidots/KoyomiDotsApplication.kt`. The goal is to add more lifecycle triggers without duplicating logic, by introducing a coordinator layer and new broadcast receiver(s), while keeping the calculation and state update logic in one place.

Key paths:

    app/src/main/java/com/example/koyomidots/ui/YearProgressWidget.kt
    app/src/main/java/com/example/koyomidots/ui/WidgetUpdateWorker.kt
    app/src/main/java/com/example/koyomidots/ui/WidgetUpdateScheduler.kt
    app/src/main/java/com/example/koyomidots/ui/YearProgressWidgetReceiver.kt
    app/src/main/java/com/example/koyomidots/KoyomiDotsApplication.kt
    app/src/main/AndroidManifest.xml

In this plan, “entry point” means any Android component or event that can initiate a widget refresh (for example, widget add/update, system date change, or manual tap). “Coordinator” means the single class responsible for translating those entry points into a WorkManager refresh request. “Worker” means the existing `WidgetUpdateWorker` that computes year progress and writes Glance state.

## Plan of Work

First, define the final list of refresh triggers and map them to entry points. These include widget lifecycle events (add/update/options changed, first render), user interaction (manual tap), system changes (date/time/timezone changes, reboot, package replaced), and periodic schedule. Document which component receives each event.

Next, add a `WidgetRefreshCoordinator` that exposes a simple API like `requestRefresh(reason)`. The coordinator will enqueue a unique WorkManager request and pass a `RefreshReason` into input data. The worker remains the single place that performs calculation and state update. To keep tests simple, have the coordinator depend on a small enqueuer abstraction rather than calling WorkManager directly.

Then, add a new broadcast receiver for system events (time/date/timezone, boot, package replaced). The receiver will map intent actions to `RefreshReason` values and call the coordinator. Update the existing Glance widget receiver to call the coordinator on widget lifecycle events.

After that, update the manifest to register the new receiver and required permissions (for example, `RECEIVE_BOOT_COMPLETED`) and ensure the widget receiver continues to be registered correctly. Ensure `exported` values follow platform requirements for system broadcasts.

Finally, add unit tests for the coordinator (verifying enqueue policy and input data) and for the receiver (verifying that intent actions trigger coordinator calls). Keep worker tests minimal because the existing domain tests already validate calculation logic.

## Concrete Steps

Work from the repository root `…/AndroidStudioProjects/koyomidots`.

1) Confirm the `RefreshReason` enum and coordinator API surface before implementation.
2) Create or update the receiver classes to forward their triggers to the coordinator.
3) Update `AndroidManifest.xml` with system broadcast receiver entries and permissions.
4) Add unit tests for coordinator and receiver behavior.
5) Run unit tests and manual widget verification.

Example commands to run later:

    (repo root) ./gradlew testDebugUnitTest
    (repo root) ./gradlew assembleDebug

Executed commands and outputs during implementation:

    (repo root) ls
    AGENTS.md  CLAUDE.md  app  build.gradle.kts  docs  gradle  gradle.properties  gradlew  gradlew.bat  local.properties  mise.toml  settings.gradle.kts

    (repo root) cat .agent/plans/2026-01-05-widget-refresh.md
    # Design Widget Refresh Orchestration
    ... (plan contents reviewed)

    (repo root) cat .agent/PLANS.md
    # Codex Execution Plans (ExecPlans):
    ... (requirements and formatting reviewed)

    (repo root) rg -n "WidgetUpdateWorker|WidgetUpdateScheduler|YearProgressWidget|YearProgressWidgetReceiver|AppGraph|AppContainer|ManualRefreshAction|Refresh" app/src/main/java
    app/src/main/java/com/example/koyomidots/KoyomiDotsApplication.kt
    app/src/main/java/com/example/koyomidots/di/AppGraph.kt
    app/src/main/java/com/example/koyomidots/ui/WidgetUpdateWorker.kt
    app/src/main/java/com/example/koyomidots/ui/YearProgressWidget.kt
    app/src/main/java/com/example/koyomidots/ui/WidgetUpdateScheduler.kt

    (repo root) git commit -m "feat: centralize widget refresh triggers"
    [main ...] feat: centralize widget refresh triggers
    9 files changed, 146 insertions(+), 27 deletions(-)

    (repo root) git commit -m "test: cover refresh coordinator and receiver"
    [main ...] test: cover refresh coordinator and receiver
    2 files changed, 100 insertions(+)

    (repo root) ./gradlew testDebugUnitTest
    FAILURE: Build failed with an exception.
    * What went wrong:
    25.0.1
    BUILD FAILED in 2s

    (repo root) ./gradlew testDebugUnitTest --stacktrace
    FAILURE: Build failed with an exception.
    * What went wrong:
    25.0.1
    * Exception is:
    java.lang.IllegalArgumentException: 25.0.1
        at org.jetbrains.kotlin.com.intellij.util.lang.JavaVersion.parse(JavaVersion.java:307)
    BUILD FAILED in 319ms

    (repo root) ./gradlew testDebugUnitTest
    FAILURE: Build failed with an exception.
    * What went wrong:
    25.0.1
    BUILD FAILED in 293ms

    (repo root) which java && java -version
    /usr/bin/java
    openjdk version "25.0.1" 2025-10-21
    OpenJDK Runtime Environment (build 25.0.1+8-27)
    OpenJDK 64-Bit Server VM (build 25.0.1+8-27, mixed mode, sharing)

    (repo root) echo $JAVA_HOME
    /Users/sotayamashita/.local/share/mise/installs/java/25.0.1

    (repo root) mise current java
    corretto-21.0.9.10.1

    (repo root) mise trust
    (no output)

    (repo root) eval "$(mise activate bash)" & java -version
    openjdk version "25.0.1" 2025-10-21

    (repo root) eval "$(mise activate bash)" && java -version
    openjdk version "21.0.9" 2025-10-21 LTS
    OpenJDK Runtime Environment Corretto-21.0.9.10.1 (build 21.0.9+10-LTS)
    OpenJDK 64-Bit Server VM Corretto-21.0.9.10.1 (build 21.0.9+10-LTS, mixed mode, sharing)

    (repo root) eval "$(mise activate bash)" && ./gradlew testDebugUnitTest
    > Task :app:compileDebugUnitTestKotlin FAILED
    e: .../WidgetRefreshReceiverTest.kt:4:16 Unresolved reference: test
    e: .../WidgetRefreshReceiverTest.kt:14:27 Unresolved reference: MockContext
    FAILURE: Build failed with an exception.
    Execution failed for task ':app:compileDebugUnitTestKotlin'.

    (repo root) eval "$(mise activate bash)" && ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 2s
    25 actionable tasks: 3 executed, 22 up-to-date

    (repo root) eval "$(mise activate bash)" && ./gradlew assembleDebug
    BUILD SUCCESSFUL in 2s
    37 actionable tasks: 8 executed, 29 up-to-date

## Validation and Acceptance

Validation is successful when all of the following are observed:

1) Adding the widget immediately displays the correct year and “X/Y” without waiting for the next periodic schedule.
2) Tapping the widget triggers an immediate refresh.
3) Changing the system date/time/timezone results in an updated “X/Y” display within one refresh cycle.
4) Rebooting the device triggers a refresh after boot completion.
5) Updating the app package triggers a refresh.
6) Unit tests for the coordinator and receiver pass.

Validation status:

Manual device validation completed for widget add and time change; unit tests and assembleDebug now pass with Corretto 21 active. Remaining manual checks include reboot and package replace verification if needed.

## Idempotence and Recovery

All steps should be additive and safe to rerun. The coordinator must use a unique WorkManager name with a consistent policy (such as REPLACE) so repeated triggers do not create duplicate work. If a broadcast receiver is misconfigured, disable the new receiver and re-run tests to isolate failures.

## Artifacts and Notes

Keep a concise log entry in the worker that includes the `RefreshReason` so manual testing can confirm which entry point triggered the refresh.

Current log line (search in logcat for "WidgetUpdateWorker"):

    Updated widget for <year>: <formatted> reason=<RefreshReason>

## Interfaces and Dependencies

Define the following interface and types (final paths to be confirmed):

In `app/src/main/java/com/example/koyomidots/ui/RefreshReason.kt`:

    enum class RefreshReason {
        Added,
        Updated,
        OptionsChanged,
        FirstRender,
        Manual,
        TimeChanged,
        Boot,
        PackageReplaced,
        Periodic
    }

In `app/src/main/java/com/example/koyomidots/ui/WidgetRefreshCoordinator.kt`:

    class WidgetRefreshCoordinator(
        private val enqueuer: WidgetRefreshEnqueuer
    ) {
        fun requestRefresh(reason: RefreshReason)
    }

In `app/src/main/java/com/example/koyomidots/ui/WidgetRefreshReceiver.kt`:

    class WidgetRefreshReceiver : BroadcastReceiver { ... }

The coordinator should enqueue `WidgetUpdateWorker` with a unique name and policy, and pass `RefreshReason` as input data. The worker should read the reason and log it, but should not change its core calculation behavior based on the reason.

Coordinator placement and dependency injection guidance:

- Place `WidgetRefreshCoordinator` in the `ui` package because it is platform-facing and owns WorkManager orchestration.
- Keep `GetYearProgressUseCase` in `domain`, and do not move any calculation logic into the coordinator.
- Prefer a testable dependency boundary by injecting a small enqueuer abstraction into the coordinator (for example, `WidgetRefreshEnqueuer`) rather than calling WorkManager directly in tests.
- Add the coordinator (and its enqueuer implementation) to an application-owned dependency container (AppGraph/AppContainer instance) so entry points can call a shared instance without holding state in app components.
- In unit tests for the coordinator, use a fake enqueuer implementation rather than mocks to align with testing guidance.

Entry points and trigger mapping (final list):

- Widget lifecycle (Glance receiver): `onUpdate` / `ACTION_APPWIDGET_UPDATE` / `ACTION_APPWIDGET_OPTIONS_CHANGED` / `onEnabled` → `Added`, `Updated`, `OptionsChanged`.
- First render: `provideGlance` when preferences are empty → `FirstRender`.
- Manual tap: `ManualRefreshAction` → `Manual`.
- System events (new receiver): `DATE_CHANGED`, `TIME_CHANGED`, `TIMEZONE_CHANGED` → `TimeChanged`.
- Boot complete: `BOOT_COMPLETED` → `Boot`.
- Package replaced: `MY_PACKAGE_REPLACED` → `PackageReplaced`.
- Periodic WorkManager schedule: existing daily worker → `Periodic`.

WorkManager policy:

- Use one unique immediate work name (for example, `year_progress_refresh`) with `ExistingWorkPolicy.REPLACE`.
- Pass `RefreshReason` as a string in input data under `KEY_REFRESH_REASON`.
- Keep the periodic work name separate from the immediate refresh work name.

Change note: Created this ExecPlan to capture the multi-trigger refresh design before implementation so the work stays organized and recoverable (2026-01-05, Codex).
Change note: Recorded coordinator placement in `ui`, AppGraph injection approach, trigger mapping, and WorkManager policy details (2026-01-05, Codex).
Change note: Confirmed coordinator as a class wired via AppGraph and system events handled by a dedicated receiver (2026-01-05, Codex).
Change note: Clarified AppGraph as an Application-owned container instance and noted fake enqueuers as the preferred test double (2026-01-05, Codex).
Change note: Logged implementation steps, decisions, and validation status after adding the coordinator, receivers, and tests (2026-01-05, Codex).
Change note: Recorded the Gradle test failure and added follow-up to resolve it before validation (2026-01-05, Codex).
Change note: Captured the stacktrace evidence for the JDK `25.0.1` parsing failure and re-run result (2026-01-05, Codex).
Change note: Logged the unit test fix (mocked Intents) and the successful JVM unit test run (2026-01-05, Codex).
