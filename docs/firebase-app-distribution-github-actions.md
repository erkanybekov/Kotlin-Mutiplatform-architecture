# Android CI and Firebase App Distribution

This project now uses a staged GitHub Actions pipeline for Android verification and Firebase App Distribution.

## Pipeline stages

1. `prepare`
   Resolves the build variant, artifact type, Gradle tasks, and artifact names.
2. `verify-android`
   Runs unit tests, lint, and the Android build.
3. `ui-tests-android`
   Runs `connectedDebugAndroidTest` on an emulator for `debug` builds.
4. `distribute-android`
   Runs only after successful verification and, for `debug`, successful UI tests. It downloads the built artifact and uploads it to Firebase App Distribution.

## Best-practice choices in this pipeline

- Uses the Firebase App Distribution Gradle plugin, which Firebase recommends for Android CI/CD.
- Uses a service account instead of legacy `FIREBASE_TOKEN`.
- Uses `google-github-actions/auth` to create and clean up temporary credentials during the distribution job.
- Verifies code before distribution instead of building and uploading in one opaque step.
- Runs Android instrumentation UI tests in a separate emulator-backed job.
- Uploads verification reports and the built distribution artifact as workflow artifacts.
- Limits automatic push-based distributions to Android and Gradle-related file changes.
- Runs verification on pull requests, but skips Firebase distribution there.

## Workflow behavior

- `pull_request`
  Runs Android verification and `debug` UI tests only.
- `push` to `master-multiplatform`
  Runs verification, `debug` UI tests, then distributes a `debug` APK to Firebase App Distribution.
- `workflow_dispatch`
  Lets you choose:
  - `debug` or `release`
  - `APK` or `AAB`

## Verification tasks

The verification job runs variant-specific tasks:

- `:shared:test<Variant>UnitTest`
- `:androidApp:test<Variant>UnitTest`
- `:shared:lint<Variant>`
- `:androidApp:lint<Variant>`
- `assemble<Variant>` or `bundle<Variant>`

The UI test job runs:

- `:androidApp:connectedDebugAndroidTest`

For the current project:

- `shared` unit tests are active.
- `androidApp` unit tests currently resolve to `NO-SOURCE`, which is valid until app-level tests are added.
- `androidApp` now has initial Compose instrumentation tests for the dashboard screen.

## Distribution flow

The distribution job does not rebuild the app.

It:

1. Downloads the artifact produced by `verify-android`
2. Authenticates with Google Cloud using the Firebase service account
3. Generates release notes from the GitHub run metadata and latest commit message
4. Uploads the already-built APK or AAB to Firebase App Distribution

For `debug`, this happens only after emulator-based UI tests succeed.

## Required GitHub Secrets

Set at least one tester target:

- `FIREBASE_APP_DIST_GROUPS`
- `FIREBASE_APP_DIST_TESTERS`

Required for Firebase authentication:

- `FIREBASE_SERVICE_ACCOUNT_JSON`

Optional for `release` verification and distribution:

- `ANDROID_RELEASE_KEYSTORE_BASE64`
- `ANDROID_RELEASE_KEYSTORE_PASSWORD`
- `ANDROID_RELEASE_KEY_ALIAS`
- `ANDROID_RELEASE_KEY_PASSWORD`

## Current minimal setup for this repository

For your current use case, only these secrets are required:

- `FIREBASE_SERVICE_ACCOUNT_JSON`
- `FIREBASE_APP_DIST_TESTERS`

Example value:

```text
someoneelse@gmail.com
```

## Artifacts produced by the workflow

- Distribution package:
  - `android-debug-apk`
  - `android-debug-aab`
  - `android-release-apk`
  - `android-release-aab`
- Verification reports:
  - `android-verification-reports-debug`
  - `android-verification-reports-release`
- UI test reports:
  - `android-ui-test-reports-debug`

## Notes for KMP

This workflow is intentionally Android-focused because Firebase App Distribution is part of the Android delivery path.

It does not run iOS simulator tests on GitHub-hosted Ubuntu runners. If you want full KMP coverage, add a separate macOS workflow for:

- `:shared:iosSimulatorArm64Test`
  or
- `:shared:iosX64Test`

## Local commands

Debug verification and build:

```bash
./gradlew \
  :shared:testDebugUnitTest \
  :androidApp:testDebugUnitTest \
  :shared:lintDebug \
  :androidApp:lintDebug \
  :androidApp:assembleDebug
```

Debug UI tests:

```bash
./gradlew :androidApp:connectedDebugAndroidTest
```

This command requires a connected device or booted emulator.

Debug distribution:

```bash
./gradlew :androidApp:appDistributionUploadDebug
```

If you run local distribution, export the same environment variables used by CI:

- `GOOGLE_APPLICATION_CREDENTIALS`
- `FIREBASE_APP_DIST_GROUPS` or `FIREBASE_APP_DIST_TESTERS`
- `FIREBASE_APP_DIST_RELEASE_NOTES` or `FIREBASE_APP_DIST_RELEASE_NOTES_FILE`

For `release`, also export:

- `ANDROID_RELEASE_KEYSTORE_PATH`
- `ANDROID_RELEASE_KEYSTORE_PASSWORD`
- `ANDROID_RELEASE_KEY_ALIAS`
- `ANDROID_RELEASE_KEY_PASSWORD`
