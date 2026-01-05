# Rename project identifiers to Sioribi (app, package, branding)

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This plan must be maintained according to `.agent/PLANS.md` from the repository root.

## Purpose / Big Picture

We are renaming the Android app and its identifiers to match the Sioribi branding (案B). After this change, the Gradle project name, Android applicationId/namespace, Kotlin package structure, app display name, and basic branding strings will all use `sioribi`. A developer should be able to build the app and see the app label as `しおり日`, and tests should pass with the new package name.

## Progress

- [x] (2026-01-05 00:00Z) Create a working plan and inventory rename targets.
- [x] (2026-01-05 00:20Z) Confirm tagline storage only (no UI surfacing) and record decision.
- [x] (2026-01-05 00:35Z) Update Gradle settings and Android manifest/application class to the new package and app name.
- [x] (2026-01-05 00:40Z) Move Kotlin source and test packages to `com.example.sioribi` and update imports/references.
- [x] (2026-01-05 00:45Z) Update strings, tests, and documentation references to reflect the new branding.
- [x] (2026-01-05 00:55Z) Validate via unit tests or build that the project still compiles and tests pass.

## Surprises & Discoveries

- Observation: Gradle failed with `JavaVersion.parse 25.0.1` until the configured JDK was activated via `mise`.
  Evidence: `./gradlew testDebugUnitTest` failed, but `eval "$(mise activate bash)" && ./gradlew testDebugUnitTest` succeeded.

## Decision Log

- Decision: Use `sioribi` as the canonical identifier, keeping applicationId and namespace aligned as `com.example.sioribi` to minimize confusion.
  Rationale: The user chose 案B and wants `sioribi` as the central branding; keeping IDs aligned reduces mental overhead.
  Date/Author: 2026-01-05 / Codex

- Decision: Rename the Application class to `SioribiApplication` for consistency with the new app name.
  Rationale: Class names surface in logs and manifest; aligning with the new project name avoids mixing old/new identifiers.
  Date/Author: 2026-01-05 / Codex

- Decision: Keep the tagline only as a string resource without UI exposure.
  Rationale: The user explicitly requested that the tagline not be reflected in the UI for now.
  Date/Author: 2026-01-05 / Codex

## Outcomes & Retrospective

Rename completed and unit tests passed after activating the configured JDK. The app now builds with the new identifiers, and the app display name is updated. The only remaining follow-up is optional (assembleDebug) if a full APK build is desired.

## Context and Orientation

This repository is a single-module Android app under `app/`. The current package is `com.example.koyomidots`, and the project name is `koyomidots`. The main application class is `KoyomiDotsApplication` in `app/src/main/java/com/example/koyomidots/`. The Gradle project name is set in `settings.gradle.kts`, and the Android namespace/applicationId are set in `app/build.gradle.kts`. The app label is defined in `app/src/main/res/values/strings.xml` as `app_name`. Tests live under `app/src/test/java/com/example/koyomidots/` and `app/src/androidTest/java/com/example/koyomidots/` and use the same package name.

## Plan of Work

First, update Gradle and manifest identifiers. In `settings.gradle.kts`, change `rootProject.name` to `sioribi`. In `app/build.gradle.kts`, update `namespace` and `applicationId` to `com.example.sioribi`. In `app/src/main/AndroidManifest.xml`, update the application name to the renamed class once it exists and switch the theme reference to `Theme.Sioribi`.

Next, move the Kotlin source tree from `app/src/main/java/com/example/koyomidots/` to `app/src/main/java/com/example/sioribi/` and change all `package` declarations and imports to `com.example.sioribi`. Rename `KoyomiDotsApplication` to `SioribiApplication` and update all references in the UI and DI layers that reference the application class.

Then, move the test packages similarly under `app/src/test/java/com/example/sioribi/` and `app/src/androidTest/java/com/example/sioribi/`, updating package declarations and any hardcoded package name assertions. Update `app/src/main/res/values/strings.xml` so the app name is `しおり日` and add a new `tagline` string with the chosen tag line `日常に、そっと栞を`. The tagline is stored only as a string resource and is not wired into any UI or widget layout. Update `app/src/main/res/values/themes.xml` and `app/src/main/res/values-night/themes.xml` so the style name matches `Theme.Sioribi`.

Finally, update repository documentation such as `AGENTS.md` so the structure references `com.example.sioribi`. Run unit tests or build to confirm the project still compiles after the rename.

## Concrete Steps

Run all commands from the repository root `/Users/sotayamashita/AndroidStudioProjects/koyomidots`.

1) Update Gradle settings and build configuration.
   - Edit `settings.gradle.kts` and set `rootProject.name = "sioribi"`.
   - Edit `app/build.gradle.kts` and set `namespace = "com.example.sioribi"` and `applicationId = "com.example.sioribi"`.
   - Edit `app/src/main/res/values/themes.xml` and `app/src/main/res/values-night/themes.xml` to rename the theme style to `Theme.Sioribi`.

2) Rename the Kotlin application class and package.
   - Move `app/src/main/java/com/example/koyomidots/` to `app/src/main/java/com/example/sioribi/`.
   - Rename `KoyomiDotsApplication.kt` to `SioribiApplication.kt` and update the class name and package declaration.
   - Update all Kotlin source files in the app module to use `com.example.sioribi` in `package` declarations and imports.
   - Update `app/src/main/AndroidManifest.xml` so `android:name` points to `.SioribiApplication`.

3) Rename test packages and assertions.
   - Move `app/src/test/java/com/example/koyomidots/` to `app/src/test/java/com/example/sioribi/`.
   - Move `app/src/androidTest/java/com/example/koyomidots/` to `app/src/androidTest/java/com/example/sioribi/`.
   - Update package declarations and change the instrumentation test assertion from `com.example.koyomidots` to `com.example.sioribi`.

4) Update branding strings and documentation.
   - In `app/src/main/res/values/strings.xml`, set `app_name` to `しおり日` and add `tagline` as `日常に、そっと栞を`.
   - Update `AGENTS.md` package path references to `com.example.sioribi`.

## Validation and Acceptance

Run the unit tests or a debug build and confirm success.

- Command: `./gradlew testDebugUnitTest`
  Expected: Gradle completes without compilation errors; tests pass.

Optionally, build the app:

- Command: `./gradlew assembleDebug`
  Expected: APK builds successfully.

Acceptance criteria:

- The app builds with the new package and applicationId `com.example.sioribi`.
- The application class referenced in the manifest is `SioribiApplication`.
- Tests compile and pass with updated packages and assertions.
- The app display name is `しおり日` in `app/src/main/res/values/strings.xml`.

## Idempotence and Recovery

All steps are safe to re-run. If a rename is partially applied, re-run the package rename and search/replace for `koyomidots` to ensure all references are updated. If Gradle sync fails, verify `namespace`, `applicationId`, and manifest `android:name` match the new package/class names.

## Artifacts and Notes

Example search to verify no old identifiers remain:

    rg -n "koyomidots|KoyomiDots" app settings.gradle.kts AGENTS.md

## Interfaces and Dependencies

No external dependencies are introduced. All changes are in-place refactors. The Kotlin package name remains in the `com.example` namespace. The Android manifest continues to reference the Application class via a leading dot (`.SioribiApplication`) which is resolved against the new package name.

Plan Update Note: Recorded completed progress, the Gradle JDK activation discovery, and test validation outcome after implementing the rename (2026-01-05).
