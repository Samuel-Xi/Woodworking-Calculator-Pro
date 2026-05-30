# Google Play Listing Copy

## App Title
Woodworking Calculator Pro

## Short Description (80 chars max)
Offline workshop calculators — board feet, cut lists, stairs, miter & more.

## Full Description

Woodworking Calculator Pro is a quiet, fast, ad-free toolkit for the bench, the job site, and the cabinet shop. Calculations run **entirely on the device** — the only time the app talks to a Google server is when you tap "Unlock Pro" to make the one-time purchase.

### Free tools
- **Miter angle** — corner, polygon, and compound crown moulding
- **Unit converter** — mm · cm · m · in · ft, with a friendly "ft + in" form
- **Paint estimator** — metric (m² / L) and imperial (ft² / gal)
- **Lumber reference** — North-American softwood S4S and sheet good sizes

### Pro — one-time unlock
- **Board cut optimizer (1D)** with kerf, efficiency and per-stock layout
- **Sheet cut optimizer (2D)** for plywood, MDF and OSB with a visual diagram
- **Board feet & cost** estimator with waste allowance
- **Stair layout** with riser/tread depths, stringer length, pitch and an IRC residential code check
- **Equal spacing** for balusters, slats, screws and shelves
- **Ceiling joists** count and total linear length, with optional cross runners
- **Flooring & tile** estimator with waste, piece count and box rollup
- **Imperial fraction input** — type "5' 6 3/4\"" instead of decimal millimetres
- **Unlimited local history** of saved calculations

### Why a one-time price?
Most calculator apps either lock features behind a subscription or bombard you with ads. We won't do either. Buy Pro once and own it on every Android device tied to your Google account.

## Key Features
- **Offline calculations** — the only network surface is the one-time Pro purchase via Google Play Billing.
- **No ads. No subscription. No analytics SDK. No tracking IDs.**
- **No personal data leaves the device.** History stays in a local Room database.
- **Material 3 UI** — light & dark themes, tablet & landscape ready.
- **English & Simplified Chinese**, more languages coming.

## Suggested Category
Tools

## Content Rating
Everyone

## Pricing Positioning
- **Model:** free download with one-time IAP
- **Pro unlock SKU:** `wcp_pro_unlock`
- **Recommended price:** US $4.99 (the $5 psychological barrier on Google Play)
- **Regional pricing:** use Google Play's regional pricing template to set roughly equivalent prices in EU, UK, AU, CA, JP, BR.

A previous draft of this listing recommended an upfront paid download at $5.99. We deliberately moved to free-with-IAP because Google Play discovery is heavily biased toward free apps, and a one-time IAP lets users evaluate the math and UI quality before paying.

## Data Safety Summary (Google Play Console)
- **Data collected:** None.
- **Data shared:** None.
- **App activity tracking:** No.
- **Location:** No.
- **Personal info:** No.
- **Financial info collected by app:** No (purchase is handled by Google Play Billing — Google collects no app-specific data on our behalf).
- **Ads:** No.
- **In-app purchases:** Yes — one-time, non-consumable "Pro unlock".
- **Network permission:** The app does not declare `android.permission.INTERNET`. The Play Billing Library communicates with the Google Play app via IPC on the same device; no direct network sockets are opened by this app.

## Privacy Policy URL
After publishing `docs/privacy-policy.html` through GitHub Pages or another static host, use that public URL in Google Play Console.

Suggested GitHub Pages URL:
`https://YOUR_GITHUB_USERNAME.github.io/Woodworking-Calculator-Pro/privacy-policy.html`

## Assets Inventory
- `icon-512.png` — Play Store icon
- `feature-graphic-1024x500.png` — Play Store feature graphic
- `phone-01-home-1080x1920.png` … `phone-05-privacy-history-1080x1920.png` — phone screenshots

## TODO before submission
- [ ] Add a phone screenshot for **Stair Layout** (currently missing)
- [ ] Add a phone screenshot for **Sheet Optimizer** (new in 1.0.0)
- [ ] Re-shoot `feature-graphic` with refreshed tagline ("Workshop calculators · offline · one-time Pro")
- [ ] Generate `tablet-7in` and `tablet-10in` screenshots for tablet eligibility
- [ ] Upload a 30-second promo video to YouTube (lifts conversion ~20-30%)
