# Vakit Application Development Guide for Codex Agent

This document outlines the core principles, architecture, and rules for developing the 'Vakit' Android application. As the development agent, you must adhere to these guidelines at all times.

## 1. Project Structure & Module Organization
`app/` hosts the single Android application module. Core code lives in `app/src/main/java/com/halitbarut/vakit`. Resources reside under `app/src/main/res`.

- **Package Structure:** You must create and adhere to the following package structure inside `com.halitbarut.vakit`: `data`, `di`, `domain`, `ui`, and `navigation`.
- **UI Themes & Composables:** UI themes are in `ui/theme`. Place feature-scoped composable screens inside a `ui/screens` sub-package.
- **Testing:** Unit tests are in `app/src/test/java` and instrumentation tests are in `app/src/androidTest/java`.
- **Dependencies:** Shared Gradle metadata is tracked in `gradle/libs.versions.toml`.
- **Visual References:** High-fidelity screen mocks, which are your primary visual guide, live in `screens_reference/`.

## 2. Core Architecture & Technology Stack
The application MUST be built using a clean MVVM (Model-View-ViewModel) architecture.

- **Technology Stack:**
    - **Language:** Kotlin (K2) targeting Java 17.
    - **UI:** Jetpack Compose with **Material 3** components.
    - **Dependency Injection:** Hilt. All dependencies must be provided via Hilt.
    - **Local Database:** Room.
    - **Navigation:** Navigation Compose.
- **Data Flow:** UI (Compose Screens) -> ViewModel -> Repository -> DataSource (Room DAO).

## 3. UI Implementation Rules
This is your primary guide for building the user interface.

- **Visual Reference Source:** The sole source of truth for UI structure and layout are the `code.html` files located in the `screens_reference/` subdirectories. You must follow this mapping:
    - **Onboarding Screen:** Use `screens_reference/welcome_to_vakit/code.html`
    - **Dashboard Screen:** Use `screens_reference/dashboard/code.html`
    - **Statistics Screen:** Use `screens_reference/statistics_overview/code.html`
    - **Settings Screen:** Use `screens_reference/settings/code.html`

- **Design Elevation Principle:** Do NOT simply replicate the basic HTML. You MUST **elevate the design** by using modern **Material 3** components (`Scaffold`, `Card`, `OutlinedTextField`, `Button`, `Switch`, etc.). The goal is a polished, native Android app that *implements the structure* of the HTML references with a superior look and feel.

- **Navigation Rules:**
    - Use Jetpack Navigation Compose with a central `NavGraph.kt` in the `navigation` package.
    - The starting destination is the Onboarding screen.
    - After initial data submission on the Onboarding screen, navigate to the Dashboard and clear the back stack.
    - The Dashboard and Statistics screens are the main tabs in a `BottomNavigationBar`.
    - The Settings screen is accessed from the Dashboard.

## 4. Data Layer Specification (`data` package)
The database schema is non-negotiable.

- **Entity:** A single `PrayerEntity` class must be used. It will contain integer fields for both debt and completed counts for each prayer (e.g., `fajr_debt`, `fajr_completed`, etc.) and a boolean `is_witr_tracked`.
- **DAO:** A `PrayerDao` interface with `suspend` functions for getting, updating, and deleting data.
- **Database:** An abstract `VakitDatabase` class.

## 5. Coding Style & Naming Conventions
Code is written in Kotlin (K2) targeting Java 17. Use 4-space indentation, trailing commas for multi-line argument lists, and favor expression-style functions when they improve readability. Choose PascalCase for classes and Composables (`PrayerListScreen`), camelCase for functions and properties, and SCREAMING_CASE for constants. Namespace new files inside `com.halitbarut.vakit` and keep composables in feature-scoped packages to maintain clarity.

## 6. Testing Guidelines
Prefer deterministic unit tests with JUnit4 in `src/test/java`, naming files after the subject (`PrayerRepositoryTest`). Compose UI scenarios should be covered with instrumentation tests in `src/androidTest/java` using `createAndroidComposeRule`. Mirror production package structure so tests discover dependencies automatically. Aim for meaningful assertions and gate new features with at least one positive and one error-path test.

## 7. Build, Test & Development Commands
- `./gradlew assembleDebug` builds the debug APK and runs KSP code generation.
- `./gradlew installDebug` deploys the latest debug build to a connected device or emulator.
- `./gradlew testDebugUnitTest` executes JVM unit tests under `src/test`.
- `./gradlew connectedAndroidTest` launches instrumentation tests on the active device.
- `./gradlew lint` runs Android lint and Compose checks; fix failures before review.

## 8. Commit & Pull Request Guidelines
Write commit subjects in the imperative mood, ≤50 characters when possible (`Add prayer timetable repository`). Provide context in the body if multiple decisions were made or migrations performed. For pull requests, include a concise summary, test evidence (`./gradlew testDebugUnitTest` output), and screenshots or screen recordings when UI changes affect Compose surfaces.