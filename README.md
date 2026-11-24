# GTA Cheat Overlay

An Android floating overlay designed for **original GTA San Andreas Android v2.10**, not Definitive Edition. Built with **Rust (JNI library for the cheat catalog)** plus a tiny Java Android UI/service layer, because system overlay permissions and windows are Android APIs.

## What is implemented

- Draggable floating gamepad bubble over other apps (once permission is granted)
- Tap bubble → **Tier 1** (6 main cheat categories)
- Choose category → **Tier 2** (18 subcategories)
- Choose subcategory → **Tier 3** (83 individual mobile cheat codes)
- Back arrow moves up one tier; X collapses the menu
- Warning text for certain dangerous/missions-affecting cheats
- Tap Tier 3 item to **COPY** mobile cheat code to clipboard
- Native Rust catalog is compiled for arm64-v8a and armeabi-v7a, loaded through JNI
- Start/stop controls and special-use foreground service notification

**Important: This version does not inject key presses into GTA SA, and tapping a cheat is not one-tap activation.** The Android overlay cannot deliver arbitrary hardware keyboard events to another game. You must still enter the code via a compatible keyboard method that GTA accepts. Do not mistake copied for activated.

## APK build

Source and GitHub Actions build workflow: [.github/workflows/android-apk.yml](.github/workflows/android-apk.yml).

The Actions workflow builds Rust using Android NDK, builds the Android app using Gradle, and uploads **GTA-Cheat-Overlay-v0.1.0-installable-debug** as a workflow artifact containing `app-debug.apk` (signed by Android's standard debug key, so installable on Android).

**The GitHub workflow has been configured but its successful completion has not been independently confirmed.** Open the repo's [Actions tab](../../actions), select the most recent green **Build Android APK** run, and download the build artifact. If no successful run exists, the APK is not yet available.

## Controls

1. Install the APK once there is a successful build.
2. Launch the app and allow **Display over other apps**.
3. Tap **Show floating circle**, then open GTA SA.
4. Drag the circle anywhere. Tap to browse Tier 1 → 2 → 3.
5. Tap a code to copy it; use a game-compatible virtual keyboard to type it into GTA.

The app does not ask for root, Accessibility, screen-reading permissions, or modification of GTA SA files.

## Catalog

- [Three-tier cheat taxonomy](docs/cheat-taxonomy.md)
- [Machine-readable Rust-backed catalog](data/cheats.original-android-2.10.json)
- 83 main mobile cheats; 4 extra experimental/debug commands are omitted from the menu

The user has confirmed `PJYNQCQ` works on GTA SA 2.10 through a virtual keyboard. The other codes have not all been individually validated on the user's device.

## Build locally

Requirements: Android SDK platform 35, NDK 27.2.12479018, JDK 17, Gradle 8.10.2, stable Rust plus `cargo-ndk`, installed targets `aarch64-linux-android` and `armv7-linux-androideabi`.

```sh
export ANDROID_NDK_HOME="$ANDROID_HOME/ndk/27.2.12479018"
cargo ndk -t arm64-v8a -t armeabi-v7a -o app/src/main/jniLibs build --manifest-path rust/Cargo.toml --release
gradle :app:assembleDebug
```

The debug APK is then under `app/build/outputs/apk/debug/app-debug.apk`.

## References

https://gtadb.io/gta-san-andreas/cheats/mobile

Unofficial fan app; not affiliated with Rockstar Games.
