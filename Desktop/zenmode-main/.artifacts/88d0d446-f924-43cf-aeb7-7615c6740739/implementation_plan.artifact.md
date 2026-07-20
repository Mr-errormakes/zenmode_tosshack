# Implementation Plan - Fix Gradle Sync and Kover Configuration

The project is currently failing to sync due to two main issues:
1.  **Plugin Alias Resolution Error**: The root `build.gradle.kts` fails to resolve `libs.plugins.android.application` correctly. This is likely due to how hyphenated keys in the Version Catalog (`libs.versions.toml`) are converted to Kotlin accessors.
2.  **Kover DSL Migration**: `app/build.gradle.kts` is using an outdated DSL for Kover (pre-0.7.0), but the project is using Kover 0.7.6.
3.  **Composite Build Catalog Issue**: The `core-api` project (included via `includeBuild`) uses `libs` but doesn't have its own Version Catalog defined, which causes failures when building it as part of the composite build.

## Proposed Changes

### 1. Version Catalog Optimization
Modify [libs.versions.toml](file:///C:/Users/mohith/Desktop/zenmode-main/gradle/libs.versions.toml) to use camelCase for plugin keys. This avoids ambiguity with nested group accessors in Kotlin DSL.

- Rename `android-application` to `androidApplication`
- Rename `android-library` to `androidLibrary`
- Rename `jetbrains-kotlin-android` to `kotlinAndroid`
- Rename `compose-compiler` to `composeCompiler`

### 2. Root Build Script Update
Modify [build.gradle.kts](file:///C:/Users/mohith/Desktop/zenmode-main/build.gradle.kts) to use the new camelCase accessors.

### 3. App Module Update
Modify [app/build.gradle.kts](file:///C:/Users/mohith/Desktop/zenmode-main/app/build.gradle.kts):
- Update plugin aliases to use new camelCase accessors.
- Migrate `kover { ... }` block to the 0.7.x syntax (`kover { reports { ... } }`).

### 4. Core-API Module Update
- Modify [core-api/build.gradle.kts](file:///C:/Users/mohith/Desktop/zenmode-main/core-api/build.gradle.kts) to use the new camelCase accessors.
- Modify [core-api/settings.gradle.kts](file:///C:/Users/mohith/Desktop/zenmode-main/core-api/settings.gradle.kts) to include the Version Catalog, pointing to the root's `libs.versions.toml`.

## Verification Plan

### Automated Tests
- Run `./gradlew sync` (or `gradle_sync` tool) to verify the project syncs successfully.
- Run `./gradlew koverHtmlReport` to verify Kover configuration is correct and reports can be generated.

### Manual Verification
- Verify that the IDE no longer shows red squiggles in the `build.gradle.kts` files.
