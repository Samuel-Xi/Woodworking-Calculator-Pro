# Launch Country Plan — Woodworking Calculator Pro

This file is **not** consumed by Gradle or the app. It documents the exact set of countries to enable in the Google Play Console at launch, and which to defer.

The goal is **high-ARPU launch first, broader rollout later**, so the first cohort of reviews comes from buyers who genuinely value the app, not from low-engagement free-tier installs that drag down our rating average during the critical first 30 days.

## Phase 1 — Initial release (week 0)

Enable in **Google Play Console → Production → Countries / regions**:

### English-speaking high-ARPU
- 🇺🇸 United States *(primary target — North American lumber sizing is built in)*
- 🇨🇦 Canada
- 🇬🇧 United Kingdom
- 🇮🇪 Ireland
- 🇦🇺 Australia
- 🇳🇿 New Zealand

### Western Europe (strong DIY culture)
- 🇩🇪 Germany
- 🇳🇱 Netherlands
- 🇧🇪 Belgium
- 🇦🇹 Austria
- 🇨🇭 Switzerland
- 🇸🇪 Sweden
- 🇳🇴 Norway
- 🇩🇰 Denmark
- 🇫🇮 Finland

That's 15 countries. All of them have:
- A real woodworking / cabinetmaking trade culture
- High Google Play paid-app ARPU
- Either English fluency or a near-future planned localisation

## Phase 2 — Week 4+ (after first 1.0.x bug-fix release)

Once the rating has stabilised at ≥ 4.2★ in Phase 1, add:

- 🇫🇷 France · 🇪🇸 Spain · 🇮🇹 Italy · 🇵🇹 Portugal (after `fr`, `es`, `it`, `pt` translations land)
- 🇯🇵 Japan (after `ja` translation lands; very high paid-app ARPU)
- 🇸🇬 Singapore · 🇲🇾 Malaysia · 🇮🇳 India (English listing acceptable)
- 🇲🇽 Mexico · 🇧🇷 Brazil (after `pt-rBR` + `es` translations land)
- 🇿🇦 South Africa

## Deliberately deferred / excluded at launch

- **Mainland China** — Google Play unavailable.
- **Russia / Belarus** — Play payouts disabled / sanctioned.
- **Countries where the local DIY / lumber retail ecosystem is too thin to justify a translation effort** (most of Central Asia, parts of Africa).

You can always extend later from Console without re-uploading the AAB.

## Pricing template
- Set the base price in USD: **$4.99**.
- Use Google Play's "Set prices for other countries" → "Use template" → "Convert from USD" with the default exchange-rate rounding. Spot-check the resulting prices for:
  - GB → £3.99
  - EU → €4.99
  - AU → A$7.99
  - JP → ¥600
  - IN → ₹399
  - BR → R$24,90

## Localised store listing strategy
- **At launch**: English listing translated only to Simplified Chinese (already in the app).
- **Phase 2**: add localised store listings for `de`, `fr`, `es`, `ja`. The localised listing dramatically lifts conversion in those locales — far more impact than localising the app UI alone.

## Tags / categories
- Primary category: **Tools**
- Tags (Console → Store presence → Store listing → Tags): "Construction calculator", "Carpentry calculator", "Woodworking", "Cut list optimizer"

## Risk notes
- Some markets (e.g. Germany) have strict price-display rules — Google handles the VAT-inclusive display automatically, but verify the displayed price before the listing goes live.
- IRC stair code messaging in the app is North-American specific; treat it as informational only in EU listings (UK Building Regs Part K, BS 5395 differ in numbers but the calculator is still useful as a starting point).
