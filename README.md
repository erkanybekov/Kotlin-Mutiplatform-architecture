# Kotlin-Mutiplatform-architechture

`Kotlin-Mutiplatform-architechture` is a Kotlin Multiplatform mobile project that shares core business logic across Android and iOS while keeping a native UI on each platform.

If you are reviewing this repo quickly, the short version is:

- one shared codebase supports two mobile platforms
- Android is built with Jetpack Compose
- iOS is built with SwiftUI
- the app currently ships a real-time chat flow with authentication
- the repo also includes a local expense dashboard feature
- Android delivery is supported by GitHub Actions and Firebase App Distribution

## Why This Repo Exists

This repository is a practical showcase of mobile engineering work. It is meant to demonstrate how to:

- build for Android and iOS without duplicating core logic
- keep platform-specific UI native instead of forcing a one-size-fits-all interface
- connect to a live backend for authentication, REST APIs, and WebSocket chat
- store structured data locally on device
- support testing and CI/CD as part of the project, not as an afterthought

## Current Product Snapshot

Today, both the Android and iOS apps open into the chat experience by default.

That chat flow includes:

- sign in and sign up
- session restore and token refresh
- chat room listing and room creation
- real-time messaging over WebSocket

The repository also contains an expense dashboard module with:

- local on-device persistence
- transaction entry and deletion
- category summaries
- simple analytics and charts

The chat feature currently points to a hosted backend at `https://experimentks.onrender.com`.

## What Someone in HR or Hiring Should Notice

This is not just a UI mockup. It shows end-to-end product thinking:

- cross-platform architecture
- native mobile UI work
- backend integration
- local data storage
- automated testing
- automated Android distribution

In other words, this repo is useful as a compact example of shipping-minded mobile development.

## Tech Stack

- Kotlin Multiplatform for shared logic
- Jetpack Compose for Android UI
- SwiftUI for iOS UI
- Ktor for networking and WebSockets
- Koin for dependency injection
- Room with bundled SQLite for local expense storage
- GitHub Actions for Android CI
- Firebase App Distribution for Android builds

## Project Structure

- `shared/`  
  Shared Kotlin code for business logic, networking, data, state holders, and platform integration.

- `androidApp/`  
  Android application built with Jetpack Compose.

- `iosApp/`  
  iOS application built with SwiftUI and connected to the shared module.

- `docs/`  
  Supporting project documentation, including Firebase App Distribution notes.

## Running The Project

### Prerequisites

- JDK 17
- Android Studio for Android builds and emulator runs
- Xcode for iOS builds and simulator runs

### Android

Build a debug APK:

```bash
./gradlew :androidApp:assembleDebug
```

Run the app from Android Studio, or install it on a connected device/emulator:

```bash
./gradlew :androidApp:installDebug
```

### iOS

Open the Xcode project:

```text
iosApp/iosApp.xcodeproj
```

Then run the `iosApp` target on a simulator or device from Xcode.

## Testing

Shared unit tests:

```bash
./gradlew :shared:testDebugUnitTest
```

Android instrumentation UI tests:

```bash
./gradlew :androidApp:connectedDebugAndroidTest
```

Note: instrumentation tests require a connected Android device or booted emulator.

## CI and Distribution

The repository includes an Android GitHub Actions workflow at `.github/workflows/firebase-app-distribution.yml`.

That workflow handles:

- verification
- Android UI testing
- build artifact generation
- Firebase App Distribution uploads

More detail is available in [`docs/firebase-app-distribution-github-actions.md`](docs/firebase-app-distribution-github-actions.md).

## Backend Configuration

The current chat API and WebSocket endpoints are defined in:

- `shared/src/commonMain/kotlin/com/erkan/experimentkmp/network/ExperimentKsApiConfig.kt`

If you need to point the mobile apps to a different backend, update that file.
