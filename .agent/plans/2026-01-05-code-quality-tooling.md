# Add Linter, Formatter, and Git Hooks for Android Code Quality

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This plan follows `/.agent/PLANS.md` and must be maintained in accordance with that file.

## Purpose / Big Picture

After this change, contributors can format Kotlin/XML consistently, run static analysis automatically, and get fast feedback before committing. They will be able to run `./gradlew spotlessApply`, `./gradlew spotlessCheck`, and `./gradlew detekt`, and install a Git pre-commit hook (optional for a solo project) that runs the same checks on staged Kotlin/Gradle Kotlin files. Success is visible when formatting changes apply cleanly, lint checks pass locally and in GitHub Actions, and the pre-commit hook blocks bad formatting or detekt violations when installed.

## Progress

- [x] (2026-01-05 06:30Z) Capture current repo state and decide exact tool versions.
- [x] (2026-01-05 06:30Z) Add Spotless, ktlint, and detekt configuration and wire Gradle tasks.
- [x] (2026-01-05 06:30Z) Add .editorconfig with default ktlint-aligned settings.
- [x] (2026-01-05 06:30Z) Add pre-commit configuration and optional hook installer.
- [x] (2026-01-05 06:32Z) Generate detekt baseline file from current code.
- [x] (2026-01-05 06:46Z) Validate by running Gradle checks and pre-commit on sample changes.

## Surprises & Discoveries

- Observation: Accessing `VersionCatalogsExtension` in `subprojects {}` failed during Gradle configuration.
  Evidence: Gradle error: "Extension of type 'VersionCatalogsExtension' does not exist."

- Observation: Gradle failed with `JavaVersion.parse 25.0.1` until `mise` activation was used.
  Evidence: First `./gradlew detektBaseline` run failed with `* What went wrong: 25.0.1` and succeeded after `eval "$(mise activate bash)"`.

- Observation: Detekt baseline needed regeneration after renaming a composable function and updating imports.
  Evidence: `./gradlew detekt` reported long-method/long-parameter issues until `./gradlew detektBaseline` was re-run.

## Decision Log

- Decision: Use Spotless for formatting, ktlint for Kotlin style rules, and detekt for static analysis.
  Rationale: Spotless centralizes formatting across Kotlin, Gradle Kotlin, and XML, while detekt provides deeper code-quality checks beyond formatting.
  Date/Author: 2026-01-05 / Codex

- Decision: Apply Spotless in `allprojects {}` so the root project and future modules share the same formatting rules.
  Rationale: It keeps the configuration in one place, covers future modules, and includes root-level Markdown/JSON files.
  Date/Author: 2026-01-05 / Codex

- Decision: Use detekt with a baseline for gradual adoption, generated once during initial rollout.
  Rationale: A baseline allows existing issues to be tracked while preventing new issues from being introduced, and creating it once avoids repeated churn.
  Date/Author: 2026-01-05 / Codex

- Decision: Keep pre-commit optional and rely on Gradle tasks and GitHub Actions as the main enforcement.
  Rationale: For a solo project, optional pre-commit reduces setup friction while CI still guarantees consistent checks.
  Date/Author: 2026-01-05 / Codex

- Decision: Include JSON and Markdown in Spotless formatting targets.
  Rationale: It keeps non-code project files consistent and prevents noisy diffs in docs and configuration.
  Date/Author: 2026-01-05 / Codex

- Decision: Use `corretto-21` in GitHub Actions to match `mise.toml`.
  Rationale: Aligning CI with local tooling avoids version drift and Java-related build surprises.
  Date/Author: 2026-01-05 / Codex

- Decision: Hardcode the ktlint version string in the Spotless configuration.
  Rationale: The version catalog extension was unavailable in `subprojects {}` during configuration, so a literal version avoids build failures.
  Date/Author: 2026-01-05 / Codex

## Outcomes & Retrospective

- Pending. Outcomes will be recorded after validation.

## Context and Orientation

This repository is a single-module Android app. The main module is in `app/`, with Kotlin sources under `app/src/main/java/com/example/sioribi/`. The root build configuration is `build.gradle.kts`, and module configuration is `app/build.gradle.kts`. Version catalog entries are in `gradle/libs.versions.toml`. This plan adds `.editorconfig`, `.pre-commit-config.yaml`, `detekt.yml`, `detekt-baseline.xml`, and `.github/workflows/code-quality.yml`, plus Gradle configuration for Spotless and detekt.

Terminology used in this plan:

- “Formatter” means a tool that rewrites files to a consistent style. In this plan, Spotless uses ktlint internally to format Kotlin files.
- “Linter” means a tool that reports style or code-quality problems without necessarily fixing them. In this plan, detekt is the linter.
- “Git pre-commit hook” means a script that runs automatically before each `git commit` to block commits that fail checks.

## Plan of Work

First, add version catalog entries for the required tools (Spotless, detekt, ktlint) in `gradle/libs.versions.toml` and add plugin aliases in the same file. Then update `build.gradle.kts` to declare the Spotless and detekt plugins with `apply false` at the root so projects can opt in. In `app/build.gradle.kts`, apply the detekt plugin and configure it to read a new `detekt.yml` placed at the repository root, plus a baseline file generated once to allow gradual adoption. Add Spotless configuration in the root `build.gradle.kts` under `allprojects { ... }` so that Kotlin, Gradle Kotlin, XML, JSON, and Markdown are formatted consistently, excluding `build/` directories.

Next, add `.editorconfig` at the repository root with only basic whitespace and encoding defaults, keeping ktlint on defaults without Compose-specific overrides or max line length customization. This ensures ktlint and the IDE agree while staying close to upstream defaults.

Then, add a `.pre-commit-config.yaml` at the repository root. Configure two local hooks: one for `./gradlew spotlessCheck` and one for `./gradlew detekt`. Limit them to staged Kotlin, Gradle Kotlin, XML, JSON, and Markdown files for Spotless, and Kotlin/Gradle Kotlin for detekt. Document pre-commit as optional for this solo project, with CI as the primary enforcement. Provide a short `scripts/pre-commit` helper only if needed for more selective file filtering; prefer the simpler `repo: local` configuration unless performance or file-scoping requires a wrapper.

Optionally, add a Gradle task to copy a wrapper script into `.git/hooks/pre-commit` so the hook can be installed from Gradle. If this is added, keep it idempotent (overwrites the same hook file safely) and document that developers can still run `pre-commit install` if they have pre-commit installed. For this solo setup, this optional task can be omitted if you prefer manual pre-commit installation.

Finally, validate by running Gradle tasks and installing the pre-commit hook if desired. Add a GitHub Actions workflow that runs `spotlessCheck`, `detekt`, and Android `lint` on pushes and pull requests, using Java `corretto-21` to match `mise.toml`. Provide expected output cues (Spotless “Clean”/“Up-to-date”, detekt report file creation). Ensure that `./gradlew spotlessApply` formats files without errors, and that `./gradlew spotlessCheck` and `./gradlew detekt` succeed on a clean tree.

## Concrete Steps

All commands are run from the repository root (`/Users/sotayamashita/AndroidStudioProjects/koyomidots`).

1) Add tool versions and plugin aliases in `gradle/libs.versions.toml`.

   - Add versions: Spotless `8.1.0`, detekt `1.23.8`, ktlint `1.8.0`.
   - Add plugins: `com.diffplug.spotless` and `io.gitlab.arturbosch.detekt`.

2) Update root `build.gradle.kts`:

   - Declare Spotless and detekt plugins with `apply false`.
   - Add `allprojects { ... }` configuration for Spotless:
     - Kotlin: `**/*.kt` (exclude `**/build/**`), ktlint `1.8.0`, trim trailing whitespace, end with newline.
     - Gradle Kotlin: `**/*.gradle.kts`, ktlint `1.8.0`.
     - XML: `**/*.xml`, trim trailing whitespace, end with newline.
     - JSON: `**/*.json`, trim trailing whitespace, end with newline.
     - Markdown: `**/*.md`, trim trailing whitespace, end with newline.

3) Update `app/build.gradle.kts`:

   - Apply detekt plugin.
   - Configure detekt to read `detekt.yml` from the repository root and to build on the default configuration.

4) Add `detekt.yml` at the repository root using detekt defaults plus any minimal, clearly justified tweaks. Generate a `detekt-baseline.xml` once (run `./gradlew detektBaseline`) so existing issues do not block the build.

5) Add `.editorconfig` at the repository root with only standard settings for indent, EOL, charset, trimming, and final newline. Do not add Kotlin-specific overrides so ktlint stays at defaults.

6) Add `.pre-commit-config.yaml` at the repository root:

   - Use `repo: local` with hooks for Spotless and detekt.
   - `entry` uses `./gradlew spotlessCheck` and `./gradlew detekt`.
   - `files` limits to `\\.kt$|\\.kts$|\\.xml$|\\.json$|\\.md$` for Spotless and `\\.kt$|\\.kts$` for detekt.

7) (Optional) Add a Gradle task in `app/build.gradle.kts` or root `build.gradle.kts` to copy `scripts/pre-commit` into `.git/hooks/pre-commit` with executable mode. Provide a small script in `scripts/pre-commit` that runs `./gradlew spotlessCheck` and `./gradlew detekt`.

8) Add GitHub Actions workflow in `.github/workflows/code-quality.yml` to run `./gradlew spotlessCheck`, `./gradlew detekt`, and `./gradlew lint` on push and pull_request, using Java `corretto-21` to match `mise.toml`.

9) Run checks:

   - `./gradlew spotlessApply`
   - `./gradlew spotlessCheck`
   - `./gradlew detekt`
   - `./gradlew lint`

   If Gradle fails with a `JavaVersion.parse 25.0.1` error, run:

       eval "$(mise activate bash)"

   Then re-run the Gradle command.

## Validation and Acceptance

- After running `./gradlew spotlessApply`, any formatting changes are applied and the command exits with status 0.
- `./gradlew spotlessCheck` succeeds with no violations. If it fails, it must report the exact files that need formatting.
- `./gradlew detekt` succeeds and produces a detekt report file (default report location under `build/reports/detekt/`).
- GitHub Actions runs `spotlessCheck`, `detekt`, and `lint` successfully on a clean push.
- Installing the hook with `pre-commit install` (if pre-commit is installed) causes `git commit` to run Spotless and detekt automatically.
- If the optional Gradle hook installer is used, running its task results in `.git/hooks/pre-commit` existing and executable.

## Idempotence and Recovery

All changes are additive and safe to re-run. Re-running Spotless or detekt tasks is safe and does not alter logic; it only formats code or reports issues. Re-running the hook installer overwrites the hook file with the same content. If pre-commit is not installed, skip hook installation and rely on Gradle tasks until it is available.

## Artifacts and Notes

Example expected outputs:

    > ./gradlew spotlessCheck
    BUILD SUCCESSFUL in 10s
    5 actionable tasks: 5 executed

    > ./gradlew detekt
    BUILD SUCCESSFUL in 12s
    6 actionable tasks: 6 executed

    > pre-commit install
    pre-commit installed at .git/hooks/pre-commit

## Interfaces and Dependencies

Dependencies to introduce and why:

- Spotless Gradle plugin (com.diffplug.spotless) to format Kotlin, Gradle Kotlin, and XML with a single configuration.
- detekt Gradle plugin (io.gitlab.arturbosch.detekt) for Kotlin static analysis with a repository-owned `detekt.yml` configuration.
- ktlint (used by Spotless) to enforce Kotlin code style rules and align with `.editorconfig`.
- pre-commit (external developer tool) to run Gradle checks before commits. It will be configured only via `.pre-commit-config.yaml` and does not require project code changes.

Plan Change Notes: Initial creation of the plan based on the requested best-practices summary and current repository state. Updated the plan to use detekt baseline once at rollout, include JSON/Markdown in Spotless, align CI JDK with `mise.toml` (corretto-21), keep ktlint defaults with a minimal `.editorconfig`, hardcode ktlint version in Spotless to avoid `VersionCatalogsExtension` access errors, and apply Spotless in `allprojects {}` so root docs are formatted too. Validation steps were executed and detekt baseline regenerated after formatting-related edits.
