# PyWarp Mobile

Android-native companion project for the PyWarp mobile port.

## Stack
- Kotlin
- Jetpack Compose / Material 3
- Android VpnService integration point
- GitHub Actions APK build

## Current status (0.1.0 preview)
- Native Android app shell and dark dashboard
- VPN permission preparation flow
- VPN service declaration
- **No WARP tunnel engine is bundled yet.** The app deliberately does not claim a connection or route traffic until a real, licensed and compatible WireGuard/MASQUE engine is integrated and tested.

## Build
Open this repository in Android Studio (JDK 17, Android SDK 35), sync Gradle, then build:
`gradle :app:assembleDebug`

APK output:
`app/build/outputs/apk/debug/app-debug.apk`

## Package
`com.pywarp.mobile`
