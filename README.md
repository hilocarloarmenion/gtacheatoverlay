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

**v0.2.0 experimental integrated keyboard:** The APK registers its own Android InputMethodService. Enable/select **GTA Cheat Keyboard** under Android keyboard settings, then tap a Tier 3 cheat to attempt to send its individual key-down/up events through the game's InputConnection. Long-press copies the code. **No Shizuku, root or external keyboard app required.** Android does NOT guarantee a non-text game provides an InputConnection, so automatic activation is **not yet verified on the user's GTA SA 2.10 device**; failed dispatch reports an explicit error. Even successfully sent key events don't prove GTA enabled the cheat.

## APK build

Source and GitHub Actions build workflow: [.github/workflows/android-apk.yml](.github/workflows/android-apk.yml).

The Actions workflow builds Rust using Android NDK, builds the Android app using Gradle, and uploads **GTA-Cheat-Overlay-v0.2.0-builtin-keyboard-debug** as a workflow artifact containing `app-debug.apk` (signed by Android's standard debug key, so installable on Android).

**Verified successful CI build:** [Build Android APK run #45327264703](https://github.com/hilocarloarmenion/gtacheatoverlay/actions/runs/45327264703) completed successfully with Rust tests, arm64/armv7 native compilation, Gradle debug APK assembly, and artifact upload all green. Download artifact **GTA-Cheat-Overlay-v0.1.0-installable-debug** from the run. It contains `app-debug.apk`. The APK has not yet been tested on a physical device.

## Controls

1. Install the v0.2.0 APK after a successful build.
2. Grant **Display over other apps**.
3. Enable and select **GTA Cheat Keyboard** in the Android keyboard settings. This is a keyboard service **inside this APK**, not an external dependency. Switch back to your normal keyboard after gaming.
4. Start floating circle and switch to GTA SA.
5. Tap Tier 1 → Tier 2 → Tier 3. Tap a code to attempt its actual key presses, or hold to copy it. The game must supply a keyboard connection; the UI shows an error if it can't.

The app does not ask for root, Accessibility, screen-reading permissions, Shizuku or modification of GTA SA files. As with every Android keyboard, enabling this keyboard gives it the ability to see text in apps when selected; it is intended for GTA only, and the code contains no network permissions.

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

## What is and is not tested

- Android native Rust + overlay APK **v0.1.0** built on GitHub Actions previously.
- **v0.2.0 integrated keyboard** successfully built in [GitHub Actions run 45329379207](https://github.com/hilocarloarmenion/gtacheatoverlay/actions/runs/45329379207). The artifact contains app-debug.apk. Live GTA SA activation has not yet been tested on the user's device.
- Android's InputConnection API supports software keyboards sending key-down and key-up events to the attached app; this is not system-wide privileged injection and cannot work if GTA does not expose a suitable input connection.

## Current tested build

- [Successful v0.2.0 APK build](https://github.com/hilocarloarmenion/gtacheatoverlay/actions/runs/45329379207) — Android SDK, Rust tests, Android native library compilation, Gradle assembly and artifact upload all passed.
- The APK includes its own Android input method, with arm64-v8a and armeabi-v7a Rust libraries.
- No Shizuku dependency. GTA SA accepting automatic input still needs device testing.
