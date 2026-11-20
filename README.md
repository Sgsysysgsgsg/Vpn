# PyWarp Mobile

Android-native mobile project using Kotlin, Jetpack Compose and the official WireGuard Android userspace tunnel library.

## Implemented
- Native Android dashboard.
- Import and parse a standard WireGuard `.conf` file.
- Store imported configuration in app-private internal storage (app backup is disabled).
- Start/stop a real userspace WireGuard tunnel through WireGuard's `GoBackend` / `wireguard-go`.
- Backend state is checked before the UI reports Connected.
- WireGuard configuration routes, DNS and IPv4/IPv6 behavior are applied by the WireGuard backend from the imported configuration.

## Important Cloudflare WARP scope
This is **not yet a Cloudflare WARP client**. The upstream PyWarp project is a Python/PySide6 controller for Cloudflare's official desktop `warp-cli`; it does not contain an Android embeddable WARP engine. Cloudflare's consumer Android app is an official supported product, but no public Android WARP SDK/API for third-party embedding was identified during this implementation.

This release therefore implements the public WireGuard protocol with user-supplied configuration. It does not register Cloudflare accounts, mint WARP credentials, imitate an official Cloudflare client, or claim WARP/MASQUE compatibility. A WARP-specific profile can only be used if it is legitimately obtained and parses as a standard WireGuard configuration. MASQUE remains unsupported until Cloudflare provides a supported integration surface or a separately reviewed interoperable implementation is available.

## Build
Requires JDK 17 and Android SDK 35:
`gradle :app:assembleDebug`

APK:
`app/build/outputs/apk/debug/app-debug.apk`

## Dependencies and notices
- WireGuard Android tunnel library: Apache-2.0, `com.wireguard.android:tunnel:1.0.20260102`.
- WireGuard userspace engine is provided by the official library; see https://git.zx2c4.com/wireguard-android and its license notices.
- PyWarp upstream is AGPL-3.0; this Android implementation does not copy its Python/QML source code. See https://github.com/saeedmasoudie/pywarp.

## Package
`com.pywarp.mobile`
