# PyWarp Android

PyWarp is an Android VPN client preview with a real WireGuard userspace backend, plus a clearly separated handoff to Cloudflare's official WARP Android app.

## Current functionality

- Import and validate a standard WireGuard .conf file.
- Start and stop a real WireGuard tunnel using the official WireGuard Android tunnel library (wireguard-go).
- Open the in-app Settings dialog.
- If Cloudflare's official 1.1.1.1 + WARP app is installed, launch it; otherwise open its Google Play listing.
- Keep WireGuard tunnel state distinct from peer handshake and end-to-end connectivity verification.

## Cloudflare WARP status

This repository does **not** yet implement Cloudflare account registration, device enrollment, MASQUE/Connect-IP, or a Cloudflare WARP tunnel. The Settings screen explains this and offers a handoff to Cloudflare's official Android app, which does not require a user-supplied .conf file.

Cloudflare documents consumer WARP traffic tunneling over MASQUE, and the Linux CLI also documents WireGuard/MASQUE protocol selection. That does not provide a supported Android embedding SDK or authorize this app to claim it has an official Cloudflare integration. We do not fabricate credentials, endpoint keys, or a successful connection state.

A legitimate future MASQUE implementation would need a reviewed, license-compatible core, a real Android TUN-to-MASQUE bridge, account/device enrollment handling, DNS and IPv4/IPv6 routing, lifecycle/reconnect handling, and device-level tests. The current app must not be described as a complete WARP client.

## Build

- JDK 17
- Android SDK platform 35
- Gradle 8.11.1

    gradle :app:assembleDebug

The GitHub Actions workflow builds and uploads the debug APK as pywarp-debug-apk.

## Licensing

The WireGuard Android tunnel library is distributed under its upstream license. PyWarp-specific code is maintained in this repository. Do not copy third-party Android app code or prebuilt binaries without verifying their license and notices.