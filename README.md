# GTA Cheat Overlay

An Android floating-overlay cheat browser for **original GTA San Andreas Android v2.10** (not the Definitive Edition).

## Intended UX

Tap a movable floating circle over GTA SA, then browse a strict three-tier hierarchy:

**Tier 1: Main category** → **Tier 2: Subcategory** → **Tier 3: Individual cheat** (its name, mobile code and safety warning).

Search, Favorites and Back can be shortcuts, without replacing the hierarchy.

## Current state

**Phase 1 complete: cheat catalog organized and committed. No APK or overlay UI exists yet.**

- [Read the full three-tier taxonomy](docs/cheat-taxonomy.md)
- [Machine-readable Android cheat catalog](data/cheats.original-android-2.10.json) — 83 core mobile codes, 6 main categories, 18 subcategories
- 4 additional developer/debug codes are separated and disabled by default pending on-device confirmation

The user confirmed that `PJYNQCQ` works with a virtual keyboard in original GTA SA Android v2.10. Other codes are documented but haven't all been tested on their particular device.

## Next development phase

1. Build the Android floating bubble and a compact browse/search interface powered by the catalog.
2. Experimentally validate that cheat code keystrokes can actually reach the foreground GTA SA game. An overlay alone does not supply game key events; don't claim a cheat activated unless confirmed.
3. Add warning prompts for potentially permanent NPC/riot effects, keep an easy way to close the bubble, and avoid disrupting native game controls.
4. Package and test an installable APK on Android 15; keep the user's original game install and saves separate.

References: https://gtadb.io/gta-san-andreas/cheats/mobile and https://gta.com.ua/en/articles/gta-san-andreas/cheats-mobile

Unofficial community project, not affiliated with Rockstar Games.
