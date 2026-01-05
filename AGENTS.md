# Repository Guidelines

## Project Structure & Module Organization

- Single Android app module: `app/`.
- Main Kotlin sources: `app/src/main/java/com/example/sioribi/` (packages include `data/`, `domain/`, `di/`, `ui/`).
- Unit tests: `app/src/test/java/`.
- Instrumented tests: `app/src/androidTest/java/`.
- Resources: `app/src/main/res/` (notably widget config in `app/src/main/res/xml/year_progress_widget.xml`).
- Agent planning docs: `.agent/PLANS.md` and `.agent/plans/` (ExecPlans and implementation logs).

## ExecPlans

When writing complex features or significant refactors, use an ExecPlan (as described in .agent/PLANS.md) from design to implementation.

## Build, Test, and Development Commands

Run these from the repo root:

- `./gradlew spotlessApply` — formats Kotlin/Gradle Kotlin/XML/JSON/Markdown.
- `./gradlew spotlessCheck` — checks formatting (Formatter).
- `./gradlew detekt` — runs Kotlin static analysis (Linter).
- `./gradlew lint` — runs Android Lint (Linter).
- `./gradlew assembleDebug` — builds the debug APK.
- `./gradlew testDebugUnitTest` — runs JVM unit tests.
- `./gradlew connectedDebugAndroidTest` — runs instrumented tests on a device/emulator.

If Gradle fails with a `JavaVersion.parse 25.0.1` error, activate the configured JDK before running Gradle:

- `eval "$(mise activate bash)"` (then re-run the Gradle command).

## Coding Style & Naming Conventions

- Kotlin and Gradle Kotlin DSL are used throughout.
- Indentation: 4 spaces.
- Classes: `UpperCamelCase`; functions/properties: `lowerCamelCase`.
- Packages follow `com.example.sioribi.<layer>` and map to folder names under `app/src/main/java/`.

## Testing Guidelines

- Frameworks: JUnit, Truth, MockK (see `app/build.gradle.kts`).
- Name tests to match the class under test (e.g., `WidgetRefreshCoordinatorTest`).
- Prefer JVM unit tests for logic; use instrumented tests only when Android APIs are required.

## Commit & Pull Request Guidelines

- Commits follow Conventional Commits (e.g., `feat:`, `fix(widget):`, `test:`) as seen in `git log`.
- PRs should include: a short description of behavior change, test results, and screenshots for UI/widget changes.

## Agent-Specific Instructions

- For complex features or significant refactors, create and maintain an ExecPlan per `.agent/PLANS.md` and store it under `.agent/plans/`.
