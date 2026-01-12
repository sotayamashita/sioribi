# Refactor Sioribi Architecture and Test Strategy

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This plan must be maintained in accordance with `.agent/PLANS.md`.

## Purpose / Big Picture

After this refactor, the app’s widget continues to show the year progress, but the codebase is organized around clear Android architecture boundaries so that business logic is reusable, UI logic is isolated, and data access is testable. The result is easier to change and verify: a newcomer can update widget behavior or time calculations by changing a single layer and can prove correctness by running the JVM tests and seeing the widget update logs after a manual refresh. The refactor is guided by the local Android architecture documentation in `docs/guide-to-app-architecture/...`.

## Progress

- [x] (2026-01-12 00:15Z) Read local architecture guidance indexes and core pages for layered architecture, UI layer, data layer, domain layer, and testing.
- [x] (2026-01-12 00:25Z) Surveyed current app structure, widget update flow, dependency wiring, and unit tests.
- [x] (2026-01-12 01:05Z) Defined target architecture boundaries, interfaces, and naming conventions for the refactor, and listed planned file moves.
- [ ] Implement data layer refactor and adjust domain layer use cases.
- [ ] Refactor widget update pipeline and UI state mapping to isolate Android-specific code.
- [ ] Update and expand unit tests and finalize validation commands.

## Surprises & Discoveries

- None yet.

## Decision Log

- Decision: Use the existing manual dependency graph (`app/src/main/java/com/example/sioribi/di/AppGraph.kt`) and improve its structure instead of migrating to Hilt.
  Rationale: The app is a single-module widget app with a small dependency surface; manual DI remains simpler while still meeting the dependency injection guidance as long as constructor injection and interfaces are used consistently.
  Date/Author: 2026-01-12 / Codex

- Decision: Keep the domain layer focused on pure business logic and avoid introducing Android types into domain classes.
  Rationale: The local guidance stresses that UI logic and Android framework dependencies should remain in the UI layer, while domain logic should remain reusable and testable.
  Date/Author: 2026-01-12 / Codex

## Outcomes & Retrospective

- Not started yet.

## Context and Orientation

This repository contains a single Android app module in `app/`. The current architecture is simple but mixed: a `TimeDataSource` lives in `data/`, a `GetYearProgressUseCase` in `domain/` computes the model, and UI/widget classes in `ui/` both render and orchestrate updates using WorkManager. The manual dependency graph is in `app/src/main/java/com/example/sioribi/di/AppGraph.kt`, and the application class (`app/src/main/java/com/example/sioribi/SioribiApplication.kt`) schedules daily updates. Unit tests live in `app/src/test/java/` and include widget layout, sizing, and worker tests.

Key guidance to align with (local docs):

- Layered architecture with repository boundaries and a single source of truth, and using coroutines/flows for layer communication where needed. See `docs/guide-to-app-architecture/architecture-recommendations/recommendations-for-android-architecture.md` and `docs/guide-to-app-architecture/data-layer-libraries/about-the-data-layer/data-layer.md`.
- UI layer should be driven by immutable UI state and unidirectional data flow; UI logic should stay in the UI layer, and ViewModel/state-holder logic should be testable. See `docs/guide-to-app-architecture/ui-layer-libraries/about-the-ui-layer/ui-layer.md`.
- Domain layer is optional but appropriate for reusable or complex business logic; use cases should be small and testable. See `docs/guide-to-app-architecture/domain-layer/domain-layer.md`.
- Testing strategy should emphasize unit tests for data and domain logic and prefer fakes over mocks. See `docs/guide-to-app-architecture/architecture-recommendations/recommendations-for-android-architecture.md`.

Definitions used in this plan:

- Repository: A data layer class that exposes app data to the rest of the app and abstracts data sources, following the guidance in the data layer doc.
- Use case: A domain layer class that encapsulates a single piece of business logic and is reusable across UI consumers.
- UI state: An immutable snapshot of the data required to render the widget UI.
- Widget update pipeline: The flow from a refresh trigger (WorkManager or broadcast) to writing state and calling the widget to update.

## Plan of Work

First, define a target architecture that cleanly separates the layers and provides explicit interfaces for each external dependency. This includes a data layer repository for year progress, a domain layer use case that consumes the repository, and UI layer adapters that transform the domain model into widget-specific UI state. The goal is that Android-specific classes (WorkManager, Glance, Context) live only in the UI layer, and business rules live only in the domain layer.

Next, refactor the data layer to introduce a repository boundary, keep the current `TimeDataSource` as a data source, and ensure that the data layer exposes immutable models. This refactor should move any “source of truth” decisions into the data layer (for this app, the source of truth is the current date from the clock), and it should make it possible to replace the time source in tests via a fake implementation.

Then, refactor the domain layer so the use case depends on the new repository instead of the data source directly. Keep `YearProgressModel` as a pure model, and if UI-friendly formatting is needed, create a dedicated mapper in the UI layer to avoid mixing UI concerns into domain classes.

After that, refactor the widget update pipeline. Introduce a small UI-layer “state holder” or “coordinator” class that takes a `YearProgressUseCase`, a `WidgetStateWriter`, and a `WidgetRenderer` interface. The worker should delegate to this class rather than directly calling the use case and Glance APIs. This isolates Android framework interactions behind interfaces for testing and supports unidirectional flow: compute domain model → map to UI state → persist widget state → render. The widget composable should consume only the widget UI state and perform rendering without calling the domain layer directly. Keep refresh scheduling logic in a dedicated scheduler class to make it testable and focused.

Finally, update tests. Replace any direct Android framework dependency in unit tests with fakes or test doubles. Add or update tests for the new repository and mapper, rework worker tests to use fake `WidgetStateWriter`/`WidgetRenderer`, and ensure UI state normalization remains covered. Keep existing sizing/drawing tests and update names if files move. The testing strategy should focus on JVM unit tests for pure logic and only use instrumented tests if a direct Android API dependency can’t be abstracted.

## Concrete Steps

All commands are run from `/Users/sotayamashita/AndroidStudioProjects/sioribi`.

1. Identify and list the files to move or split by layer. Capture a short list of current files and their future homes in the plan’s `Artifacts and Notes` section.
2. Create the new data layer repository and update `GetYearProgressUseCase` to depend on it.
3. Introduce the UI-layer interfaces for writing widget state and rendering, and refactor `WidgetUpdateWorker` to use a coordinator that depends on those interfaces and the use case.
4. Update the manual dependency graph in `app/src/main/java/com/example/sioribi/di/AppGraph.kt` to wire the new components and inject them into workers via providers.
5. Update unit tests or add new ones for repository logic, mapper logic, and worker orchestration; keep them in `app/src/test/java/` with names matching the new classes.
6. Run formatting and unit tests:

    ./gradlew spotlessApply
    ./gradlew testDebugUnitTest

If `JavaVersion.parse 25.0.1` appears, run:

    eval "$(mise activate bash)"

and re-run the Gradle command.

## Validation and Acceptance

Validation is complete when the following are true:

- Running `./gradlew testDebugUnitTest` passes with updated tests that cover the new repository, updated use case, widget update coordinator, and existing layout/drawing logic. The tests should demonstrate that a fake time source yields the expected year progress and that widget update orchestration writes and renders state with the correct refresh reason.
- Running the app and manually refreshing the widget triggers a log line from `buildWidgetUpdateLogMessage` showing the correct year and formatted day count. This verifies the end-to-end update pipeline.
- The widget continues to render a grid of dots and footer values without runtime exceptions after the refactor.

## Idempotence and Recovery

The steps are designed to be additive and safe. Refactors should be performed by introducing new interfaces and moving call sites, not by deleting existing code immediately. If a change causes failures, revert the specific file edit and re-run `./gradlew testDebugUnitTest` to confirm the baseline passes before reapplying the change. Keep at least one working path for widget updates until the new coordinator is fully wired and tests pass.

## Artifacts and Notes

The following list will be populated during implementation to make the refactor reproducible by a newcomer:

- Planned file moves and new files:
  - New: `app/src/main/java/com/example/sioribi/data/YearProgressRepository.kt`
  - New: `app/src/main/java/com/example/sioribi/data/DefaultYearProgressRepository.kt`
  - New: `app/src/main/java/com/example/sioribi/ui/WidgetStateWriter.kt`
  - New: `app/src/main/java/com/example/sioribi/ui/WidgetRenderer.kt`
  - New: `app/src/main/java/com/example/sioribi/ui/WidgetUpdateCoordinator.kt`
  - Update in place: `app/src/main/java/com/example/sioribi/domain/GetYearProgressUseCase.kt`
  - Update in place: `app/src/main/java/com/example/sioribi/ui/WidgetUpdateWorker.kt`
  - Update in place: `app/src/main/java/com/example/sioribi/di/AppGraph.kt`
- Short example log lines from widget update runs (to be captured once wired).
- Minimal diffs for interface introductions and coordinator wiring (to be captured during implementation).

## Interfaces and Dependencies

The following interfaces and types must exist after the refactor, with names and packages as specified, to enforce boundaries and testability.

In `app/src/main/java/com/example/sioribi/data/YearProgressRepository.kt`, define:

    interface YearProgressRepository {
        fun getYearProgress(): YearProgressModel
    }

In `app/src/main/java/com/example/sioribi/data/DefaultYearProgressRepository.kt`, define a class that depends on `TimeDataSource` and returns a `YearProgressModel` calculated from the current date. Keep the calculation in one place to ensure a single source of truth.

In `app/src/main/java/com/example/sioribi/domain/GetYearProgressUseCase.kt`, update the constructor to accept `YearProgressRepository` and keep a single `execute()` method that returns `YearProgressModel`.

In `app/src/main/java/com/example/sioribi/ui/WidgetStateWriter.kt`, define:

    interface WidgetStateWriter {
        suspend fun write(model: YearProgressModel)
    }

In `app/src/main/java/com/example/sioribi/ui/WidgetRenderer.kt`, define:

    interface WidgetRenderer {
        suspend fun render()
    }

In `app/src/main/java/com/example/sioribi/ui/WidgetUpdateCoordinator.kt`, define a class that accepts `GetYearProgressUseCase`, `WidgetStateWriter`, and `WidgetRenderer` and exposes:

    suspend fun update(reason: RefreshReason)

The coordinator should call the use case, write the model, and then render.

In `app/src/main/java/com/example/sioribi/di/AppGraph.kt`, wire the concrete implementations. The `WidgetUpdateWorker` should request the coordinator from the graph instead of directly using the use case and Glance APIs.

## Testing Strategy

Testing should mirror the layered architecture and the local guidance:

- Data layer: add unit tests for `DefaultYearProgressRepository` with fake `TimeDataSource` implementations, covering leap and non-leap years and boundary days.
- Domain layer: keep or adapt `GetYearProgressUseCaseTest` to verify that the use case delegates to the repository and returns the expected model.
- UI layer (non-UI logic): add unit tests for the widget update coordinator using fake `WidgetStateWriter` and `WidgetRenderer`, verifying that updates happen in the correct order and with the expected model and reason.
- Existing widget layout/drawing tests should remain but may move if the helper functions are relocated; update test names to match class names.

Prefer fakes over mocks, keep tests deterministic, and avoid Android framework dependencies in JVM tests by placing Android-specific operations behind interfaces.

## Notes on Plan Maintenance

When this plan is revised during implementation, add a note at the bottom explaining what changed and why, and update all sections to remain self-contained.

Plan update note (2026-01-12): Marked milestone 1 as complete and recorded the planned file moves/new files in Artifacts and Notes to lock in the target architecture boundaries.
