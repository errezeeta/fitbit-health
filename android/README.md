# Fitbit Health Android app

Native Kotlin + Jetpack Compose client for the private Fitbit Health gateway.

## Local prerequisites

- Android Studio with Android SDK 35
- JDK 17
- A device or emulator

Open this directory as a Gradle project. Configure the gateway URL and token in the app settings once those screens are implemented. Never commit personal Tailscale addresses, tokens, or `local.properties`.

The current module is only the compile-safe project shell. Features are being added incrementally using the implementation plan in `../docs/plans/2026-08-03-fitbit-health-android-implementation.md`.
