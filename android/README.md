# RetailMedia Sample — Android

Android e-commerce sample app demonstrating Yandex RetailMedia integration.

**Stack:** Kotlin, Android View, Kotlin Coroutines, Retrofit + OkHttp, Kotlinx serialization,
Hilt DI, Jetpack Navigation 2 (single Activity). Single-module architecture with Presentation
(MVVM) and Data layers; talks only to a local backend.

## Running

1. Start the backend locally (see [`../backend`](../backend)).
2. Open this directory in Android Studio and run the `app` configuration on an Android
   emulator, or install it from the command line:

   ```bash
   ./gradlew :app:installDebug
   ```

The app reaches the backend at `http://10.0.2.2:8080/` — the host loopback as seen from the
emulator (`BASE_URL` in `app/build.gradle.kts`).

## Where the integration lives

Search the code for `[Step` — see the root [`README.md`](../README.md#where-the-integration-lives).
