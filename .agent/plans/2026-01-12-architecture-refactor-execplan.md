# Recalibrate Sioribi Architecture and Test Strategy (Consistency Pass)

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This plan must be maintained in accordance with `.agent/PLANS.md`.

## Purpose / Big Picture

After this refactor, the year-progress widget still renders the same visual output, but the codebase aligns more closely with Android’s layered architecture guidance and general software design principles. Business rules live in the data/domain layers, UI formatting and widget state production live in the UI layer, and Android-specific glue is isolated behind interfaces. Logging is intentional and non-noisy. A newcomer can confirm correctness by running JVM unit tests and by manually refreshing the widget and seeing a single, meaningful update log line.

## Progress

- [x] (2026-01-12 12:20Z) Reviewed local architecture guidance for data layer, domain layer, UI layer, and testing.
- [x] (2026-01-12 12:35Z) Audited the current codebase and existing ExecPlan for mismatches, inconsistencies, and noisy logs.
- [x] (2026-01-12 13:05Z) Refined test strategy with concrete add/update/remove test cases for the new model and UI state mapping.
- [x] (2026-01-12 13:25Z) Defined the refined domain model and added the UI state mapper with percent/formatting logic.
- [x] (2026-01-12 13:30Z) Refactored the widget update pipeline to use UI state and updated the dependency graph.
- [x] (2026-01-12 13:35Z) Removed per-render widget debug logging to avoid noisy logs.
- [x] (2026-01-12 13:45Z) Updated unit tests, added mapper coverage, and removed obsolete model tests.
- [x] (2026-01-12 13:50Z) Ran `./gradlew testDebugUnitTest` successfully.

## Surprises & Discoveries

- Observation: The domain model currently includes UI-facing fields (`formattedString`, `progressPercentage`) that are only used by widget rendering and logging.
  Evidence: `app/src/main/java/com/example/sioribi/domain/YearProgressModel.kt` and usages in `app/src/main/java/com/example/sioribi/ui/WidgetUpdateWorker.kt`.
- Observation: UI helper functions live in a worker file, but are used by non-worker classes.
  Evidence: `writeModelToPreferences` is defined in `app/src/main/java/com/example/sioribi/ui/WidgetUpdateWorker.kt` and used in `app/src/main/java/com/example/sioribi/ui/GlanceWidgetAdapters.kt`.
- Observation: Debug logs are emitted during widget composition and size resolution, which can be noisy for normal usage.
  Evidence: `Log.d` calls in `app/src/main/java/com/example/sioribi/ui/YearProgressWidget.kt`.
- Observation: `BuildConfig` is not generated in this module, so log gating needs to avoid referencing it.
  Evidence: Kotlin compile error `Unresolved reference 'BuildConfig'` in `YearProgressWidget.kt` when running `./gradlew testDebugUnitTest`.

## Decision Log

- Decision: Keep manual dependency injection via `AppGraph` instead of migrating to Hilt.
  Rationale: The app is single-module and already uses constructor injection with a small dependency surface area; manual DI remains simpler and consistent with dependency injection guidance.
  Date/Author: 2026-01-12 / Codex

- Decision: Split the domain model from UI formatting by introducing a UI-layer mapper and a UI state type.
  Rationale: The UI layer should be responsible for producing UI-ready state and formatting, while the domain layer should remain reusable and UI-agnostic per the local architecture guidance.
  Date/Author: 2026-01-12 / Codex

- Decision: Remove unused domain fields (such as `progressPercentage`) and compute any derived UI fields in the UI mapper.
  Rationale: Unused or UI-specific data in domain models creates layer leakage and reduces clarity; derived data should be computed where it is consumed.
  Date/Author: 2026-01-12 / Codex

- Decision: Remove low-value data-class tests and add focused mapper tests, preferring fakes over mocks in unit tests.
  Rationale: Tests should validate behavior rather than Kotlin data-class mechanics, and local guidance prefers deterministic fakes over mocks.
  Date/Author: 2026-01-12 / Codex
- Decision: Remove per-render widget debug logs instead of gating them with `BuildConfig`.
  Rationale: `BuildConfig` is not generated in this module and the logs were considered noisy for normal usage; removing them keeps logs meaningful without extra Gradle changes.
  Date/Author: 2026-01-12 / Codex

## Outcomes & Retrospective

- (2026-01-12) Completed the refactor with a new domain model, UI state mapper, updated coordinator pipeline, and refreshed tests. JVM unit tests pass, and widget update logs remain concise.

## Context and Orientation

This repository is a single Android app module in `app/`. The current structure already has `data/`, `domain/`, `di/`, and `ui/` packages under `app/src/main/java/com/example/sioribi/`. The widget is implemented with Glance and stores its state in DataStore preferences. The update flow is: refresh trigger (WorkManager or broadcast) → `WidgetUpdateWorker` → `WidgetUpdateCoordinator` → `WidgetStateWriter` → `WidgetRenderer`.

Current issues relative to local guidance:

- The domain model (`YearProgressModel`) includes UI-specific formatting and percent values, which makes the data layer effectively produce UI state.
- UI mapping functions (`writeModelToPreferences`, `buildWidgetUpdateLogMessage`) live in the worker file, which mixes concerns and obscures their reuse.
- The widget composition logs layout sizing every render, creating noisy logs and obscuring meaningful update logs.
- Tests include a low-value model “data class holds values” test and use mocking where simple fakes would suffice.

Local guidance that anchors this plan:

- Data layer and repository boundaries, immutability, and avoiding direct data source access. See `docs/guide-to-app-architecture/data-layer-libraries/about-the-data-layer/data-layer.md`.
- Domain layer as reusable business logic without UI dependencies. See `docs/guide-to-app-architecture/domain-layer/domain-layer.md`.
- UI layer as the pipeline that converts domain data into UI state. See `docs/guide-to-app-architecture/ui-layer-libraries/about-the-ui-layer/ui-layer.md`.
- Testing guidance and preference for fakes over mocks. See `docs/guide-to-app-architecture/architecture-recommendations/recommendations-for-android-architecture.md`.

Definitions used in this plan:

- Domain model: A pure, UI-agnostic representation of year progress (raw numbers only).
- UI state: The immutable, UI-ready data used to render the widget (includes formatted strings and display-friendly values).
- Mapper: A UI-layer class that converts the domain model into UI state.
- Widget update pipeline: The path from a refresh trigger to persisted widget state and a render call.

## Plan of Work

First, define a refined domain model that represents only the raw year progress data needed by other layers. Keep this model in the domain layer and update the data layer repository to return it. Replace `GetYearProgressUseCase.execute()` with a more idiomatic `operator fun invoke()` to align with local domain-layer conventions.

Next, create a UI-layer mapper (a simple class or function) that converts the domain model into a widget UI state. This UI state is the only data shape that the widget rendering and preferences writing should use. Move UI-specific helpers (`writeModelToPreferences`, log message formatting) into a dedicated UI file so they are not tied to worker implementation.

Then, refactor the widget update pipeline: the coordinator should fetch the domain model, map it to UI state, write UI state to preferences, render, and log. This preserves unidirectional flow: domain data → UI state → widget rendering. Update the worker to delegate to the coordinator without knowing details of the UI state or mapping.

After that, normalize logging. Keep a single update log line for manual or scheduled refreshes, and gate layout/debug logs behind a debug check or remove them entirely. This ensures logs are meaningful and avoids per-render spam.

Finally, update the unit tests to reflect the new model and mapper and to improve test doubles. Remove the low-value data-class test, add tests for the UI state mapper, update repository and coordinator tests to the new types, and prefer simple fakes over mocks where practical. Keep the existing sizing and drawing tests; they remain valid.

## Concrete Steps

All commands are run from `/Users/sotayamashita/AndroidStudioProjects/sioribi`.

1. Introduce the new domain model and update the repository and use case to use it.
2. Add a UI-layer mapper and UI state type, and update `WidgetStateWriter` and the coordinator to use UI state rather than the domain model.
3. Move UI helper functions out of `WidgetUpdateWorker.kt` into a dedicated UI file (for example `WidgetStateMapping.kt`), and update references.
4. Normalize logging in `YearProgressWidget.kt` and `WidgetUpdateCoordinator.kt`, ensuring debug logs are gated and update logs remain concise.
5. Update tests and remove obsolete ones; add new tests for the mapper and updated coordinator behavior.
6. Run formatting and tests:

    ./gradlew spotlessApply
    ./gradlew testDebugUnitTest

If `JavaVersion.parse 25.0.1` appears, run:

    eval "$(mise activate bash)"

and re-run the Gradle command.

## Validation and Acceptance

Validation is complete when the following are true:

- Running `./gradlew testDebugUnitTest` passes with updated tests that cover the data layer calculations, UI state mapping, widget update coordination, and layout/drawing logic.
- Manually refreshing the widget (tap) produces exactly one clear update log line (for example “Updated widget for 2026: 12/365 reason=Manual”) and does not spam per-render debug logs.
- The widget renders the same grid and footer values as before, with no crashes or missing data.

## Idempotence and Recovery

The refactor is safe to apply incrementally. Introduce the new model and mapper first, then update the coordinator and writer to accept the new UI state, keeping compilation green at each step. If a change breaks tests or compilation, revert the most recent file edit and re-run `./gradlew testDebugUnitTest` to confirm baseline behavior before reapplying the change in smaller steps.

## Artifacts and Notes

Planned new or updated files:

- New: `app/src/main/java/com/example/sioribi/domain/YearProgress.kt` (domain model without UI formatting).
- Update: `app/src/main/java/com/example/sioribi/data/YearProgressRepository.kt` and `app/src/main/java/com/example/sioribi/data/DefaultYearProgressRepository.kt` to return the new domain model.
- Update: `app/src/main/java/com/example/sioribi/domain/GetYearProgressUseCase.kt` to use `operator fun invoke()`.
- New: `app/src/main/java/com/example/sioribi/ui/YearProgressUiState.kt` (UI state data class).
- New: `app/src/main/java/com/example/sioribi/ui/YearProgressUiStateMapper.kt` (maps domain → UI state).
- New or updated: `app/src/main/java/com/example/sioribi/ui/WidgetStateMapping.kt` to hold preference write and log message helpers.
- Update: `app/src/main/java/com/example/sioribi/ui/WidgetStateWriter.kt`, `app/src/main/java/com/example/sioribi/ui/GlanceWidgetAdapters.kt`, `app/src/main/java/com/example/sioribi/ui/WidgetUpdateCoordinator.kt`, and `app/src/main/java/com/example/sioribi/ui/WidgetUpdateWorker.kt` to use the UI state.
- Update: `app/src/main/java/com/example/sioribi/ui/YearProgressWidget.kt` to gate or remove debug logs.
- Tests updated/added under `app/src/test/java/com/example/sioribi/...`.

## Interfaces and Dependencies

The following interfaces and types must exist after the refactor, with names and packages as specified:

In `app/src/main/java/com/example/sioribi/domain/YearProgress.kt`, define:

    data class YearProgress(
        val currentDay: Int,
        val totalDays: Int,
        val year: Int,
    )

In `app/src/main/java/com/example/sioribi/data/YearProgressRepository.kt`, define:

    interface YearProgressRepository {
        fun getYearProgress(): YearProgress
    }

In `app/src/main/java/com/example/sioribi/domain/GetYearProgressUseCase.kt`, define:

    class GetYearProgressUseCase(
        private val yearProgressRepository: YearProgressRepository,
    ) {
        operator fun invoke(): YearProgress = yearProgressRepository.getYearProgress()
    }

In `app/src/main/java/com/example/sioribi/ui/YearProgressUiState.kt`, define:

    data class YearProgressUiState(
        val currentDay: Int,
        val totalDays: Int,
        val year: Int,
        val formatted: String,
        val progressPercent: Int,
    )

In `app/src/main/java/com/example/sioribi/ui/YearProgressUiStateMapper.kt`, define:

    class YearProgressUiStateMapper {
        fun map(progress: YearProgress): YearProgressUiState { /* format + percent */ }
    }

In `app/src/main/java/com/example/sioribi/ui/WidgetStateWriter.kt`, update:

    interface WidgetStateWriter {
        suspend fun write(state: YearProgressUiState)
    }

In `app/src/main/java/com/example/sioribi/ui/WidgetUpdateCoordinator.kt`, update:

    class WidgetUpdateCoordinator(
        private val getYearProgressUseCase: GetYearProgressUseCase,
        private val mapper: YearProgressUiStateMapper,
        private val stateWriter: WidgetStateWriter,
        private val renderer: WidgetRenderer,
        private val logger: WidgetUpdateLogger = DefaultWidgetUpdateLogger(),
    ) {
        suspend fun update(reason: RefreshReason)
    }

In `app/src/main/java/com/example/sioribi/ui/WidgetStateMapping.kt`, define helpers:

    fun writeUiStateToPreferences(preferences: MutablePreferences, state: YearProgressUiState)
    fun buildWidgetUpdateLogMessage(state: YearProgressUiState, reason: String): String

## Testing Strategy

The test plan below is prescriptive and should be followed to align with the new model and UI state separation.

Remove:

- Delete `app/src/test/java/com/example/sioribi/domain/YearProgressModelTest.kt` (low-value data-class test for the old domain model).

Add:

- Add `app/src/test/java/com/example/sioribi/ui/YearProgressUiStateMapperTest.kt` covering:
  - Non-leap year mapping: 2026-01-04 → formatted “4/365”, percent 1.
  - Leap day mapping: 2024-02-29 → formatted “60/366”, percent 16.
  - Boundary mapping: 2026-01-01 → percent 0; 2026-12-31 → percent 100.
  - If input sanitization is introduced in the mapper, add explicit tests for it.

Update:

- Update `app/src/test/java/com/example/sioribi/data/DefaultYearProgressRepositoryTest.kt` to assert only `currentDay`, `totalDays`, and `year` on `YearProgress` (no formatted/percent assertions).
- Update `app/src/test/java/com/example/sioribi/domain/GetYearProgressUseCaseTest.kt` to use `invoke()` and `YearProgress`.
- Update `app/src/test/java/com/example/sioribi/ui/WidgetUpdateCoordinatorTest.kt` to inject a fake `YearProgressUiStateMapper` and assert ordering of write → render → log with UI state.
- Update `app/src/test/java/com/example/sioribi/ui/WidgetUpdateWorkerTest.kt` to use `YearProgressUiState` and `writeUiStateToPreferences` helper.
- Update `app/src/test/java/com/example/sioribi/ui/WidgetRefreshReceiverTest.kt` to use real `Intent(action)` objects instead of mocking.

Keep:

- All sizing/drawing tests (`WidgetGridSizingTest`, `YearProgressWidgetDotPositionsTest`, `YearProgressWidgetBitmapTest`, `YearProgressWidgetSizingTest`, `YearProgressWidgetStateTest`) unless the helpers they reference are moved.

Test double preference:

- Prefer simple fakes over mocks across JVM unit tests wherever feasible.

## Notes on Plan Maintenance

When this plan is revised during implementation, add a note at the bottom explaining what changed and why, and update all sections to remain self-contained.

Plan update note (2026-01-12): Replaced the previous ExecPlan with a new consistency-focused refactor plan after auditing current code and local architecture guidance, incorporating model/UI state separation, logging normalization, and test strategy updates.
Plan update note (2026-01-12): Added a concrete, file-level test plan specifying which tests to add, update, and remove, including mapper coverage and fake-first guidance.
Plan update note (2026-01-12): Marked implementation steps complete, recorded the BuildConfig discovery, and documented the logging decision after finishing code changes and JVM tests.
