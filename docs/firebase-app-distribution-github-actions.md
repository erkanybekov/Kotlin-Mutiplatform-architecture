# Firebase App Distribution via GitHub Actions

This project is configured to upload Android builds to Firebase App Distribution from GitHub Actions.

## What was added

- Firebase App Distribution Gradle plugin in `androidApp`
- `debug` and `release` upload tasks:
  - `appDistributionUploadDebug`
  - `appDistributionUploadRelease`
- GitHub Actions workflow at `.github/workflows/firebase-app-distribution.yml`

## Required GitHub Secrets

Set at least one tester target:

- `FIREBASE_APP_DIST_GROUPS`
- `FIREBASE_APP_DIST_TESTERS`

Required for Firebase authentication:

- `FIREBASE_SERVICE_ACCOUNT_JSON`

Optional for `release` distribution:

- `ANDROID_RELEASE_KEYSTORE_BASE64`
- `ANDROID_RELEASE_KEYSTORE_PASSWORD`
- `ANDROID_RELEASE_KEY_ALIAS`
- `ANDROID_RELEASE_KEY_PASSWORD`

## Recommended setup

1. Create a Firebase service account with permission to distribute builds in App Distribution.
2. Save the full JSON key as the `FIREBASE_SERVICE_ACCOUNT_JSON` GitHub Secret.
3. Create a tester group in Firebase App Distribution and put its alias into `FIREBASE_APP_DIST_GROUPS`.

## Workflow behavior

- Push to `master-multiplatform` uploads a `debug` APK to Firebase App Distribution.
- Manual `workflow_dispatch` lets you choose:
  - `debug` or `release`
  - `APK` or `AAB`

## Local commands

Debug APK:

```bash
./gradlew assembleDebug appDistributionUploadDebug
```

Release APK:

```bash
./gradlew assembleRelease appDistributionUploadRelease
```

If you run locally, export the same environment variables used by the workflow:

- `GOOGLE_APPLICATION_CREDENTIALS`
- `FIREBASE_APP_DIST_GROUPS` or `FIREBASE_APP_DIST_TESTERS`
- `FIREBASE_APP_DIST_RELEASE_NOTES` or `FIREBASE_APP_DIST_RELEASE_NOTES_FILE`

For `release`, also export:

- `ANDROID_RELEASE_KEYSTORE_PATH`
- `ANDROID_RELEASE_KEYSTORE_PASSWORD`
- `ANDROID_RELEASE_KEY_ALIAS`
- `ANDROID_RELEASE_KEY_PASSWORD`
