# GTA Cheat Overlay — Three-Tier Cheat Organization

**Target:** Original GTA San Andreas for Android v2.10 (not Definitive Edition).

## Navigation rule

1. Tier 1: Main category
2. Tier 2: Subcategory
3. Tier 3: Individual cheat (label + Android code)

Tap floating circle → choose Tier 1 → choose Tier 2 → tap Tier 3 cheat.

The catalog is stored in [`data/cheats.original-android-2.10.json`](../data/cheats.original-android-2.10.json). Treat it as the single source of truth for the app UI. **This is a cheat catalog, not proof that code injection works.**

## Inventory

CJ & Abilities: 15 cheats / 3 subcategories
Weapons & Gear: 8 cheats / 3 subcategories
Vehicles & Traffic: 26 cheats / 4 subcategories
People & Police: 14 cheats / 3 subcategories
Weather & Time: 11 cheats / 2 subcategories
World & Gameplay: 9 cheats / 3 subcategories

**Total: 83 core cheats, 6 main categories, 18 subcategories.**

## Complete cheat tree

### CJ & Abilities

- **Health & Survival**
  - Health, armor & $250,000 — `PJYNQCQ` (tested)
  - Infinite health — `BLOKKIK`
  - Complete invincibility — `GONPXWR`
  - Infinite lung capacity — `POOOJOX`
  - Never hungry — `KBTMUVH`
  - Suicide — `SLOTSFK`
- **Body & Attributes**
  - Maximum fat — `AESHXWQI`
  - Maximum muscle — `SGVDSQW`
  - Skinny CJ — `AWXDCRJ`
  - Maximum stamina — `AEZLCKXU`
  - Maximum respect — `MTGIISR`
  - Maximum sex appeal — `APGZLQR`
- **Superhuman Abilities**
  - Super punch — `LRMYOJM`
  - Super jump — `BFMANNZP`
  - Adrenaline mode — `SLSNRKK`

### Weapons & Gear

- **Weapon Loadouts**
  - Weapon set 1 — `BEFWKSBQ` ⚠
  - Weapon set 2 — `SHHIHJG` ⚠
  - Weapon set 3 — `GOIZSSX`
  - Weapon set 4 — `BIEUHQY`
- **Ammo & Aiming**
  - Infinite ammo — `NECUMZ`
  - Full aim while driving — `RYSMRM`
- **Portable Equipment**
  - Jetpack — `CDGUDEP`
  - Parachute — `GSUMLEG`

### Vehicles & Traffic

- **Spawn Vehicles**
  - Rhino tank — `AYNVQVK`
  - Hydra jet — `AWPTMIIQ`
  - Bloodring Banger — `BIGLWCDD`
  - Hotring Racer A — `BGJPSYC`
  - Hotring Racer B — `BIEAVBAY`
  - Rancher — `HPGPIJZ`
  - Romero hearse — `PSPNATX`
  - Stretch limousine — `IXSMWCQ`
  - Trashmaster — `QPOLSVK`
  - Caddy — `DAHESZY`
- **Performance & Durability**
  - Invincible car that wrecks others — `BXBTUBTI`
  - Perfect handling — `DLNNHZJ`
  - All cars have nitro — `WUSDOTO`
- **Vehicle Physics & Destruction**
  - Destroy all vehicles — `BKFONFE` ⚠
  - Flying cars — `DOTBSFK` ⚠
  - Flying boats — `PTHSEO`
  - Cars drive on water — `BDBGXNJI`
  - Cars float away when hit — `JBVIJXA`
  - Huge bicycle bunny hops — `TDBKCEH`
- **Road Traffic**
  - Always green traffic lights — `ENQCFMA`
  - Aggressive drivers — `IOKXTFJ`
  - Black traffic — `GOYDVAO`
  - Pink traffic — `GYKVYTR`
  - Sports car traffic — `FRIUBIL`
  - Cheap car traffic — `JTBCSN`
  - Reduced traffic — `DEHDRX`

### People & Police

- **Wanted Level**
  - Never wanted — `BYKGOAB` ⚠
  - Raise wanted level — `NCBXXDX` ⚠
  - Remove wanted level — `KDTZNHO`
  - Six-star wanted level — `GWJZWC` ⚠
- **Pedestrian Behavior**
  - Pedestrians riot — `NJXPCYE` ⚠
  - Chaos mode — `AKOZBCH` ⚠
  - Pedestrians attack CJ — `FARYJHZ` ⚠
  - Pedestrians attack with guns — `ERFBFNI` ⚠
  - Pedestrians have weapons — `GDNXHDK`
- **Gangs & Recruitment**
  - Gang members everywhere — `EAMLJNN`
  - Gangs control the streets — `HAPOHXR`
  - Recruit anyone (pistols) — `JEZRPI`
  - Recruit anyone (AK-47) — `AWIOMPH`
  - Recruit anyone (rockets) — `QAONHOH`

### Weather & Time

- **Weather**
  - Sunny weather — `AAEXPPQC`
  - Very sunny weather — `HTRTTVJ`
  - Cloudy weather — `VBWEMQX`
  - Rainy weather — `TAVPIER`
  - Stormy weather — `EAKILHM`
  - Foggy weather — `EGCEBVM`
  - Sandstorm — `JBWDWWO`
- **Time & Sky**
  - Orange sky (21:00) — `KTGDLXY`
  - Always midnight — `AWUJNBB`
  - Faster clock — `OWAKIJ`
  - Advance clock 4 hours — `YACKMWS`

### World & Gameplay

- **World Themes**
  - Beach party theme — `BWCMMTD` ⚠
  - Funhouse theme — `HDLIWGB` ⚠
  - Kinky theme — `BNIZWSB` ⚠
  - Country theme — `OAXCCRI` ⚠
  - Ninja theme — `AAUSQP`
  - Elvis is everywhere — `LNHVOAE`
- **Game Speed**
  - Faster gameplay — `EHWBWDS`
  - Slow motion — `FNJFCZC`
- **Mission Tools**
  - Complete current mission — `BYIXZIY`

## Extra: developer/debug commands

Not included in the 83 confirmed-source standard codes. Keep behind an Advanced/Experimental toggle until individually tested on 2.10.

- Show mobile control mappings: `KRRIHBT`
- Show tap-to-target controls: `MIZYXHZ`
- Show target debug information: `JQFUDUB`
- Modify achievement-related stats (developer): `XBOX_HELPER`

## App behavior and safety

- Floating bubble opens the six top-level categories, not a flat list.
- Breadcrumb and Back support traversal; never force a return to the root after opening a cheat.
- Show an individual cheat's label, mobile code, and warning; provide search and favorites as shortcuts, not replacements for the tree.
- Important: a normal Android overlay **cannot automatically activate** cheats in GTA SA. Implement and test a real input method with the game before promising one-tap activation.
- Use separate saves. Riot and hostile pedestrian cheats can persist on a saved game, and some cheats can break mission flows.
- Initial validation: `PJYNQCQ` works on the user's 2.10 installation. Every other code has **not** been individually verified on the user's device.

## References

- https://gtadb.io/gta-san-andreas/cheats/mobile
- https://gta.com.ua/en/articles/gta-san-andreas/cheats-mobile
- https://www.gtaboom.com/gta-san-andreas-cheats-2d0b/
