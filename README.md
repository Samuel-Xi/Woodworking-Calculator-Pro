# Woodworking Calculator Pro

A premium woodworking workshop companion for Android. All calculations run
locally — no analytics, no ads, no third-party tracking. The only time the
app touches a Google server is the one-time **Pro unlock** in-app purchase,
which is handled by the Google Play Billing Library.

**Distribution model:** free download with a one-time IAP. See
`play-store-assets/listing-copy.md` for the listing copy and
`play-store-assets/launch-countries.md` for the staged rollout plan.

## Features

| #  | Tool                          | Tier | Highlights                                                                            |
|----|-------------------------------|------|---------------------------------------------------------------------------------------|
| 1  | **Miter Angle**               | Free | Corner / regular polygon / compound (crown) — instant blade & bevel angles            |
| 2  | **Unit Converter**            | Free | Live mm · cm · m · in · ft + a "ft + in" pretty form                                  |
| 3  | **Paint Estimator**           | Free | Metric (m² · L) **and** imperial (ft² · gal); coats; round-up to whole cans           |
| 4  | **Lumber Reference**          | Free | North-American softwood S4S nominal vs. actual + sheet goods (plywood / MDF / OSB)    |
| 5  | **Board Cut Optimizer** (1D)  | Pro  | Cutting-stock with kerf using First-Fit-Decreasing; visual layout per stock           |
| 6  | **Sheet Cut Optimizer** (2D)  | Pro  | Plywood / MDF guillotine packer with rotation, kerf, and a per-sheet diagram          |
| 7  | **Board Feet & Cost**         | Pro  | Lumber volume, waste allowance, and price-per-bd-ft estimator                         |
| 8  | **Stair Layout**              | Pro  | Risers, treads, riser height, tread depth, stringer length, pitch, IRC code check     |
| 9  | **Equal Spacing**             | Pro  | Balusters / slats / shelves with marked centre positions                              |
| 10 | **Ceiling Joists**            | Pro  | On-center spacing → joist count, total linear length, optional cross runners          |
| 11 | **Flooring & Tile**           | Pro  | Live area, piece count, custom waste %, optional pieces-per-box → boxes needed        |
| 12 | **Local History**             | Free | Room database, on-device only; copy or delete entries; full clear                     |
| 13 | **Fraction input**            | All  | Length fields accept "3 1/8", "5' 6 3/4\"", 1.5, 1,5 — anywhere in the app            |

## Design

- **Palette**: Saddle brown `#8B4513`, warm tan `#C68642`, charcoal `#333333`,
  soft cream surface `#FAFAFA`. Mirrored dark scheme keeps the brand identity.
- **Type**: System sans, carefully tuned weights/letter-spacing for an editorial feel.
- **Shapes**: 16 dp / 20 dp rounded corners; 0.6 dp hairline borders.
- **Motion**: Spring scale on press, vertical slide for value updates,
  horizontal slide for navigation, content-resize spring on cards.

## Architecture

```
app/src/main/java/com/woodworking/calculatorpro/
├── WoodworkingApp.kt           Application + DB/repo/billing singletons
├── MainActivity.kt             Single-activity Compose host; Billing lifecycle
├── billing/                    Entitlements + Google Play Billing wrapper
├── data/                       Room (entity, DAO, DB, repository)
├── domain/                     Pure-Kotlin calculators (no Android deps)
├── ui/
│   ├── theme/                  Color · Type · Shape · Theme
│   ├── components/             WCard · WField · WSegmented · WResultRow · …
│   ├── nav/                    Routes + NavGraph (Pro gate enforced here)
│   └── screens/                Home + 11 tools + History + Paywall
└── util/                       Format · Parse · Fraction · Clipboard
app/src/test/java/...           JVM unit tests for every calculator + parsers
```

The domain layer has zero Android imports, so every formula is unit-testable
on plain JVM. Run the suite with `./gradlew testDebugUnitTest`.

## Privacy posture

- Manifest declares **no `INTERNET` permission**.
- `android:allowBackup="false"` plus exclusion rules in
  `res/xml/backup_rules.xml` and `res/xml/data_extraction_rules.xml`.
- Only third-party library that does any IPC is the Google Play Billing
  Library (used solely for the one-time Pro unlock). No ads, analytics,
  crash-reporting, or social SDKs.

## Build & run

This project ships without the binary `gradle-wrapper.jar`. Pick one of:

### A) Android Studio (easiest)
1. **Hedgehog | 2023.1.1** or newer.
2. *File → Open…* → select this directory.
3. Studio will offer to download Gradle 8.9 and generate the wrapper.
4. Hit **Run ▶**.

### B) Command line
1. Install Gradle 8.9 once: `brew install gradle` (macOS) or see
   <https://gradle.org/install/>.
2. `bash scripts/bootstrap.sh` — this calls `gradle wrapper` to create the
   missing `gradle-wrapper.jar`.
3. `./gradlew assembleDebug` (or `installDebug` if a device is plugged in).

### Release build
```bash
./gradlew assembleRelease
```
Signing config is intentionally not included — add your own keystore in
`app/build.gradle.kts` before publishing.

## Tested on

- compileSdk 34, minSdk 24
- Phones (portrait & landscape) and 7"/10" tablets
- Light & dark system themes
- English and Simplified Chinese locales (`values/`, `values-zh/`)

## License

Copyright © Woodworking Calculator Pro. All rights reserved.
