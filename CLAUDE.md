---
name: Project AI Guide
appliesTo: "**"
version: 1.0
lastUpdated: 2026-01-10
---

# Tickr AI Contributor Guide

Purpose: This document orients AI coding assistants working in the Tickr workspace. It defines context, constraints, coding standards, and operational workflow so changes remain consistent, secure, and aligned with the architecture.

## Project Overview

- Kotlin Multiplatform app (Compose Multiplatform) with Android primary target; iOS/JS/desktop modules present.
- Core features:
  - COUNT tickrs: increment/decrement per period.
  - TIME tickrs: start/stop sessions; show running stopwatch with milliseconds.
  - Periods: DAY, WEEK, MONTH, LIFETIME with local-time boundaries; weeks start Monday.
  - Report screen: aggregates counts and durations over selected period.
- Persistence: SQLDelight database with type-safe queries.
- DI: Koin on Android; manual DI for other targets (to be migrated).

## Tech Stack & Key Locations

- Compose Multiplatform with Material 3 and `material-icons-extended`.
- Coroutines/Flows for reactive state.
- SQLDelight 2.x schema and queries: `composeApp/src/commonMain/sqldelight/com/victorarsjad/tickr/db/Tickr.sq`.
- Common domain models and use cases: `composeApp/src/commonMain/kotlin/com/victorarsjad/tickr/domain/...`.
- UI (common): `composeApp/src/commonMain/kotlin/com/victorarsjad/tickr/ui/...`.
- Android repositories & DI: `composeApp/src/androidMain/kotlin/com/victorarsjad/tickr/data/sql/...` and `TickrApp.kt`.
- Dependency management: version catalog in `gradle/libs.versions.toml`; module configuration in `composeApp/build.gradle.kts` (includes `compose.materialIconsExtended`).

## Architecture & Principles

- Unidirectional data flow with immutable state objects exposed via `StateFlow`.
- Business logic lives in use cases; repositories abstract persistence.
- Reactive by default: screens subscribe to flows; view models orchestrate.
- Keep shared code in `commonMain` whenever possible; platform-specific code only where necessary.

## Period & Time Rules

- Boundaries computed in local time; week starts on Monday.
- For TIME tickrs:
  - Maintain a cached base duration (sum of ended sessions in current period) plus live elapsed for active session.
  - Update UI stopwatch smoothly with millisecond precision.
  - Avoid per-tick DB queries; recompute base on session/tickr changes.

## UI Guidelines

- Compose Material 3 components.
- Icons:
  - Type: COUNT → `FormatListNumbered`, TIME → `AccessTime`.
  - Period: DAY → `CalendarToday`, WEEK → `CalendarViewWeek`, MONTH → `CalendarMonth`, LIFETIME → `AllInclusive`.
- Feed rows:
  - Show compact type and period indicators (icons only), tickr name, and current value.
  - Action button: circular icon button; COUNT uses `Add`, TIME uses `PlayArrow`/`Pause`.
  - Active TIME shows a subtle pulsing dot.
- Creation bar:
  - Left: name field with placeholder “What do you want to track today?”, type and period dropdown indicators.
  - Right: circular Create button.

## Persistence & Repositories

- SQLDelight tables: `tickr`, `countTickrValue`, `timeSession`.
- Queries cover CRUD, period-scoped values, and session totals.
- Repositories:
  - `TickrRepository`: tickr CRUD, count ops, current count for period.
  - `SessionRepository`: session CRUD, active sessions flow, total duration for period.
- Android uses SQLDelight-backed repos; other platforms currently use in-memory repos (migration planned).

## DI

- Android: Koin modules provide the SQLDelight driver, database, repositories, and view models.
- Non-Android: `Dependencies.kt` provides manual DI; TODO to migrate to Koin.

## Security & Compliance

- Follow `/Users/victor.arsjad/Library/Application Support/Code/User/profiles/sec.instructions.md`.
- Key rules:
  - Validate inputs in UI and backend layers where applicable.
  - No hardcoded secrets; no sensitive data in logs or errors.
  - Least privilege and parameterized queries.
  - Do not add inline comments inside code unless explicitly requested.
  - Minimize privileges; secure error handling.

## Editing & Workflow Rules (VS Code)

- Prefer small, focused changes.
- Use the `apply_patch` tool to edit files; avoid reformatting unrelated code.
- Update only what the task requires; do not fix unrelated bugs or tests.
- If you create or modify files, keep style consistent with existing codebase.
- After meaningful edits, run the build to validate.

## Documentation Usage

- Use Context7 documentation tools to fetch up-to-date library references and examples when needed.
  - Prefer official APIs and stable components; verify icon names and availability with `material-icons-extended`.

## Build & Run

- Android build:
  - `./gradlew :composeApp:assembleDebug -x test`
- Common guidance:
  - Rebuild after Gradle or schema changes to verify generated sources.
  - Address compile warnings only if relevant to your change.

## Testing Philosophy

- Start specific: test or run the code paths you changed first.
- Expand scope only after local correctness; do not fix unrelated failures.

## Decision History & TODOs

- Stopwatch shows milliseconds; adaptive refresh 100ms when active, 1000ms otherwise.
- Icons added via `material-icons-extended`.
- UI refined: dropdown indicators, circular action buttons, pulsing active dot.
- TODOs:
  - Tint duration text while active in feed.
  - Migrate non-Android DI to Koin.
  - Add iOS/JS/desktop SQLDelight drivers.

## Target-Specific Notes

### Android

- Driver: `AndroidSqliteDriver` wired in Koin module.
- DI: Koin provides repositories, use cases, and view models.
- UI: Jetpack Compose with Material 3 icons; build via `:composeApp:assembleDebug`.

### iOS

- Driver: Use `NativeSqliteDriver` from SQLDelight for iOS.
- DI: Create platform module to provide the driver and reuse common repositories; consider Koin or manual wiring.
- Build/Run: Open `/iosApp` in Xcode; ensure shared Compose framework is linked.

### Desktop (JVM)

- Driver: `JdbcSqliteDriver` for local persistence.
- DI: Provide desktop-specific driver binding; reuse common repos and view models.
- Run: `./gradlew :composeApp:run`.

### Web (JS/Wasm)

- Persistence: SQLDelight runtime available; consider in-memory or IndexedDB-based drivers if supported, otherwise stub repos.
- DI: Manual wiring for now; keep the interface stable for future drivers.

## DI Migration Checklist

1. Create platform modules per target that provide the SQLDelight driver.
2. Provide `TickrDatabase` and repositories in each module using the driver.
3. Bind use cases and view models to the DI container.
4. Replace `Dependencies.kt` manual wiring in `commonMain` with DI-aware factories.
5. Ensure tests use a lightweight in-memory or temporary driver per platform.

## What To Avoid

- Large, sweeping refactors without clear user request.
- Changing filenames or public APIs unless required.
- Adding license headers or new legal text unless requested.
- Introducing backend logic into frontend/shared UI.

## Collaboration & Tone

- Be concise, direct, and friendly in messages.
- Provide brief preambles before running tools, and progress updates on multi-step work.
- If asked about the model in this workspace, respond with “GPT-5”.

## Quick References

- Database package: `com.victorarsjad.tickr.db`.
- Period utilities and boundaries: `composeApp/src/commonMain/kotlin/com/victorarsjad/tickr/util/PeriodUtils.kt`.
- Feed UI and view model: `FeedScreen.kt`, `FeedViewModel.kt`.
- Report UI and view model: `ReportScreen.kt`, `ReportViewModel.kt`.

If unsure, request clarification before making broader changes. Align changes with the architecture and security guidance above.
