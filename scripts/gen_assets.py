"""
Generate / regenerate Play Store graphic assets.
Run:  python3 scripts/gen_assets.py
Requires: Pillow  (pip3 install Pillow)
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import math, os, sys

ROOT = Path(__file__).parent.parent
OUT  = ROOT / "play-store-assets"
OUT.mkdir(exist_ok=True)

# ── colour palette (brand) ────────────────────────────────────────────────────
SADDLE  = (139, 69,  19)   # #8B4513
TAN     = (198, 134, 66)   # #C68642
CREAM   = (250, 246, 241)  # #FAF6F1
WHITE   = (255, 255, 255)
INK     = (51,  34,  17)   # #332211
GREEN   = (74,  130, 90)   # accent green for checkmarks
LIGHT   = (245, 240, 234)

def font(size, bold=False):
    """Best-effort system font; falls back to default if none found."""
    candidates_bold = [
        "/System/Library/Fonts/Helvetica.ttc",
        "/System/Library/Fonts/SFPro-Bold.ttf",
        "/Library/Fonts/Arial Bold.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
    ]
    candidates_reg = [
        "/System/Library/Fonts/Helvetica.ttc",
        "/System/Library/Fonts/SFPro-Regular.ttf",
        "/Library/Fonts/Arial.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
    ]
    for path in (candidates_bold if bold else candidates_reg):
        if os.path.exists(path):
            try:
                return ImageFont.truetype(path, size)
            except Exception:
                pass
    return ImageFont.load_default()

def rounded_rect(draw, xy, radius, fill):
    x0,y0,x1,y1 = xy
    draw.rounded_rectangle([x0,y0,x1,y1], radius=radius, fill=fill)

def pill(draw, xy, fill, outline=None, width=2):
    x0,y0,x1,y1 = xy
    r = (y1-y0)//2
    if outline:
        draw.rounded_rectangle([x0,y0,x1,y1], radius=r, fill=outline)
        draw.rounded_rectangle([x0+width,y0+width,x1-width,y1-width], radius=r-width, fill=fill)
    else:
        draw.rounded_rectangle([x0,y0,x1,y1], radius=r, fill=fill)

def text_center(draw, xy, txt, fnt, fill):
    bb = draw.textbbox((0,0), txt, font=fnt)
    w,h = bb[2]-bb[0], bb[3]-bb[1]
    cx,cy = xy
    draw.text((cx-w//2, cy-h//2), txt, font=fnt, fill=fill)

# ─────────────────────────────────────────────────────────────────────────────
# 1.  FEATURE GRAPHIC  1024 × 500
# ─────────────────────────────────────────────────────────────────────────────
def gen_feature_graphic():
    W, H = 1024, 500
    img  = Image.new("RGB", (W, H), CREAM)
    draw = ImageDraw.Draw(img)

    # warm right-side wash
    for x in range(W//2, W):
        alpha = (x - W//2) / (W//2)
        r = int(CREAM[0] + (TAN[0]-CREAM[0]) * alpha * 0.18)
        g = int(CREAM[1] + (TAN[1]-CREAM[1]) * alpha * 0.18)
        b = int(CREAM[2] + (TAN[2]-CREAM[2]) * alpha * 0.18)
        draw.line([(x,0),(x,H)], fill=(r,g,b))

    # decorative wood-grain lines (very subtle)
    for i in range(8):
        y = 60 + i * 55
        draw.line([(480, y),(W, y)], fill=(220,210,195), width=1)

    # ── LEFT: text block ──────────────────────────────────────────
    f_title1 = font(72, bold=True)
    f_title2 = font(72, bold=True)
    f_sub    = font(26)
    f_bullet = font(27)
    f_badge  = font(22, bold=True)

    draw.text((52, 48),  "Woodworking",     font=f_title1, fill=SADDLE)
    draw.text((52, 128), "Calculator Pro",  font=f_title2, fill=SADDLE)

    # pill badge
    badge_txt = "OFFLINE · NO ADS · ONE-TIME PRO"
    pill(draw, (52, 222, 52+len(badge_txt)*13+24, 260), fill=LIGHT, outline=TAN, width=2)
    draw.text((64, 228), badge_txt, font=f_badge, fill=SADDLE)

    bullets = [
        "11 offline workshop calculators",
        "2D sheet & 1D board cut optimizers",
        "Stairs, miter, spacing, flooring & more",
    ]
    for i, b in enumerate(bullets):
        y = 290 + i*52
        draw.ellipse([52, y+8, 68, y+24], fill=GREEN)
        draw.text((82, y), b, font=f_bullet, fill=INK)

    # ── RIGHT: phone mockup ───────────────────────────────────────
    px, py, pw, ph = 620, 28, 360, 444
    # phone body
    rounded_rect(draw, [px, py, px+pw, py+ph], radius=36, fill=INK)
    rounded_rect(draw, [px+6, py+6, px+pw-6, py+ph-6], radius=30, fill=WHITE)
    # notch
    draw.rounded_rectangle([px+pw//2-28, py+6, px+pw//2+28, py+24], radius=8, fill=INK)

    # screen content — home grid (simplified)
    sx, sy = px+14, py+30
    sw = pw-28

    draw.text((sx+8, sy+4), "Woodworking Calculator Pro", font=font(11, bold=True), fill=INK)
    # "Unlock Pro" banner
    rounded_rect(draw, [sx+4, sy+26, sx+sw-4, sy+48], radius=8, fill=(255,248,235))
    draw.text((sx+10, sy+31), "✦  Unlock Pro — $4.99 one-time", font=font(10), fill=SADDLE)

    tools = [
        ("Miter Angle","Free"), ("Board Cut","Pro"),
        ("Sheet Cut","Pro"),    ("Board Feet","Pro"),
        ("Stair Layout","Pro"), ("Equal Spacing","Pro"),
    ]
    cols, rows = 2, 3
    tw = (sw-12)//cols
    th = 62
    for idx, (name, tier) in enumerate(tools):
        c, r = idx % cols, idx // cols
        tx0 = sx + 4 + c*(tw+4)
        ty0 = sy + 54 + r*(th+4)
        rounded_rect(draw, [tx0, ty0, tx0+tw, ty0+th], radius=10, fill=LIGHT)
        dot_color = GREEN if tier=="Free" else TAN
        draw.ellipse([tx0+10, ty0+10, tx0+22, ty0+22], fill=dot_color)
        draw.text((tx0+28, ty0+8), name, font=font(10, bold=True), fill=INK)
        badge_col = (220,240,220) if tier=="Free" else (255,240,218)
        badge_fc  = (40,100,50)   if tier=="Free" else SADDLE
        rounded_rect(draw, [tx0+10, ty0+28, tx0+10+len(tier)*7+8, ty0+44], radius=6, fill=badge_col)
        draw.text((tx0+14, ty0+30), tier, font=font(9), fill=badge_fc)

    img.save(OUT/"feature-graphic-1024x500.png", "PNG", optimize=True)
    print("✓  feature-graphic-1024x500.png")


# ─────────────────────────────────────────────────────────────────────────────
# 2.  PHONE-05  — Privacy (updated: remove "No in-app purchases")
# ─────────────────────────────────────────────────────────────────────────────
def phone_base(title_top, title_bot, sub):
    W, H = 1080, 1920
    img  = Image.new("RGB", (W, H), CREAM)
    draw = ImageDraw.Draw(img)
    draw.text((72, 90),  title_top, font=font(80, bold=True), fill=SADDLE)
    draw.text((72, 180), title_bot, font=font(80, bold=True), fill=SADDLE)
    draw.text((72, 284), sub,       font=font(40),            fill=INK)
    return img, draw

def card(draw, x, y, w, h, r=28):
    rounded_rect(draw, [x, y, x+w, y+h], radius=r, fill=WHITE)

def gen_phone05_privacy():
    img, draw = phone_base(
        "Private by design", "",
        "Save calculations locally. No tracking,\nno ads, no internet permission."
    )
    W = 1080
    cx = 72

    # history card
    card(draw, cx, 390, W-cx*2, 480)
    draw.text((cx+40, 420), "Local History", font=font(52, bold=True), fill=SADDLE)
    entries = [
        ("Board Feet & Cost",  "Total board feet: 39.930 bd ft · Est. cost: $169.70"),
        ("Board Optimizer",    "Stock pieces: 3 · Efficiency: 91.4%"),
        ("Equal Spacing",      "Gap: 10.688 in · Center-to-center: 12.188 in"),
    ]
    for i, (name, detail) in enumerate(entries):
        ey = 500 + i*110
        rounded_rect(draw, [cx+24, ey, W-cx*2-24+cx, ey+88], radius=16, fill=LIGHT)
        draw.text((cx+44, ey+12), name,   font=font(34, bold=True), fill=INK)
        draw.text((cx+44, ey+52), detail, font=font(28),            fill=(100,80,60))

    # privacy bullets (updated — no mention of in-app purchases)
    card(draw, cx, 910, W-cx*2, 400)
    bullets = [
        "No INTERNET permission",
        "No analytics SDK",
        "No advertising SDK",
        "One-time Pro unlock via Play Billing",
    ]
    for i, b in enumerate(bullets):
        by = 954 + i * 84
        draw.ellipse([cx+36, by+8, cx+60, by+32], fill=GREEN)
        draw.text((cx+76, by+2), b, font=font(38), fill=INK)

    img.save(OUT/"phone-05-privacy-history-1080x1920.png", "PNG", optimize=True)
    print("✓  phone-05-privacy-history-1080x1920.png")


# ─────────────────────────────────────────────────────────────────────────────
# 3.  PHONE-06  — Sheet Cut Optimizer (new Pro feature)
# ─────────────────────────────────────────────────────────────────────────────
def gen_phone06_sheetcut():
    W, H = 1080, 1920
    img  = Image.new("RGB", (W, H), CREAM)
    draw = ImageDraw.Draw(img)

    draw.text((72, 90),  "2D Sheet Cut",     font=font(80, bold=True), fill=SADDLE)
    draw.text((72, 180), "Optimizer",        font=font(80, bold=True), fill=SADDLE)
    draw.text((72, 284), "Plywood, MDF & OSB cut lists with\nrotation and a visual layout diagram.",
              font=font(38), fill=INK)

    cx, cw = 72, W-144

    # inputs card
    card(draw, cx, 390, cw, 360)
    draw.text((cx+40, 418), "Sheet size", font=font(40, bold=True), fill=SADDLE)

    fields = [
        ("Sheet length", "2440 mm"), ("Sheet width", "1220 mm"),
        ("Saw kerf", "3 mm"),        ("Parts", "4 parts entered"),
    ]
    for i, (label, val) in enumerate(fields):
        fy = 480 + i*64
        rounded_rect(draw, [cx+24, fy, cx+cw-24, fy+50], radius=12, fill=LIGHT)
        draw.text((cx+40, fy+10), label, font=font(28), fill=(120,90,60))
        bb = draw.textbbox((0,0), val, font=font(28, bold=True))
        draw.text((cx+cw-48-(bb[2]-bb[0]), fy+10), val, font=font(28, bold=True), fill=INK)

    # results card
    card(draw, cx, 800, cw, 200)
    draw.text((cx+40, 830), "Results", font=font(44, bold=True), fill=SADDLE)
    res = [("Sheets needed", "2"), ("Efficiency", "87.3%"), ("Parts placed", "10 / 10")]
    for i, (k,v) in enumerate(res):
        ry = 894 + i*60
        draw.text((cx+40, ry), k, font=font(34), fill=INK)
        bb = draw.textbbox((0,0), v, font=font(34, bold=True))
        draw.text((cx+cw-48-(bb[2]-bb[0]), ry), v, font=font(34, bold=True), fill=SADDLE)

    # diagram card — Sheet #1
    card(draw, cx, 1060, cw, 560)
    draw.text((cx+40, 1086), "Sheet #1 layout", font=font(40, bold=True), fill=SADDLE)

    # draw scaled sheet diagram
    dx, dy = cx+40, 1148
    dw, dh = cw-80, 420
    scale_x = dw / 2440
    scale_y = dh / 1220

    # sheet background
    rounded_rect(draw, [dx, dy, dx+dw, dy+dh], radius=10, fill=(240,235,228))
    draw.rounded_rectangle([dx, dy, dx+dw, dy+dh], radius=10,
                           outline=(180,160,140), width=2)

    # sample placements (representative, not exact)
    placements = [
        (0,   0,   760, 560, TAN,   "Side"),
        (763, 0,   760, 560, TAN,   "Side"),
        (0,   563, 900, 560, (180,140,90), "Top"),
        (903, 563, 900, 560, (180,140,90), "Top"),
    ]
    for px2, py2, pw2, ph2, col, lbl in placements:
        x0 = dx + int(px2 * scale_x)
        y0 = dy + int(py2 * scale_y)
        x1 = x0 + int(pw2 * scale_x) - 1
        y1 = y0 + int(ph2 * scale_y) - 1
        draw.rectangle([x0,y0,x1,y1], fill=col+(180,) if len(col)==3 else col)
        draw.rectangle([x0,y0,x1,y1], outline=WHITE, width=1)
        # label if wide enough
        if x1-x0 > 40:
            draw.text((x0+4, y0+4), lbl, font=font(18), fill=WHITE)

    img.save(OUT/"phone-06-sheet-optimizer-1080x1920.png", "PNG", optimize=True)
    print("✓  phone-06-sheet-optimizer-1080x1920.png")


# ─────────────────────────────────────────────────────────────────────────────
# 4.  PHONE-07  — Paywall / Pro Upgrade screen
# ─────────────────────────────────────────────────────────────────────────────
def gen_phone07_paywall():
    W, H = 1080, 1920
    img  = Image.new("RGB", (W, H), CREAM)
    draw = ImageDraw.Draw(img)

    draw.text((72, 90),  "One-time unlock.",  font=font(74, bold=True), fill=SADDLE)
    draw.text((72, 174), "Own it forever.",   font=font(74, bold=True), fill=SADDLE)
    draw.text((72, 278), "No subscription. No ads. No tracking.\nBuy once on any Android device.",
              font=font(38), fill=INK)

    cx, cw = 72, W-144

    # feature list card
    card(draw, cx, 380, cw, 860)
    draw.text((cx+40, 410), "Pro includes", font=font(46, bold=True), fill=SADDLE)

    pro_features = [
        ("2D Sheet Cut Optimizer",      "Plywood, MDF & OSB with visual diagram"),
        ("1D Board Cut Optimizer",      "Kerf-aware FFD with efficiency report"),
        ("Board Feet & Cost",           "Volume, waste & price estimator"),
        ("Stair Layout",                "Risers, treads, stringer & IRC check"),
        ("Equal Spacing",               "Balusters, slats, screws & shelves"),
        ("Ceiling Joists",              "Count & linear length + cross runners"),
        ("Flooring & Tile",             "Piece count, waste & box rollup"),
        ("Imperial Fraction Input",     "Type 5' 6 3/4\" anywhere"),
        ("Unlimited Local History",     "Save, copy & review past calculations"),
    ]
    for i, (name, desc) in enumerate(pro_features):
        fy = 484 + i*82
        draw.ellipse([cx+36, fy+12, cx+58, fy+34], fill=GREEN)
        draw.text((cx+72, fy+4),  name, font=font(30, bold=True), fill=INK)
        draw.text((cx+72, fy+40), desc, font=font(24),            fill=(120,90,60))

    # price button
    rounded_rect(draw, [cx, 1296, cx+cw, 1392], radius=32, fill=SADDLE)
    text_center(draw, (W//2, 1344), "Unlock Pro  —  $4.99", font(48, bold=True), WHITE)

    # restore link
    text_center(draw, (W//2, 1430), "Restore purchase", font(34), (140,100,60))

    # bottom assurance
    card(draw, cx, 1490, cw, 220)
    assurances = ["One-time purchase", "Works offline", "No subscription ever"]
    aw = cw // len(assurances)
    for i, a in enumerate(assurances):
        ax = cx + i*aw + aw//2
        draw.ellipse([ax-18, 1528, ax+18, 1564], fill=GREEN)
        text_center(draw, (ax, 1610), a, font(28), INK)

    img.save(OUT/"phone-07-paywall-1080x1920.png", "PNG", optimize=True)
    print("✓  phone-07-paywall-1080x1920.png")


if __name__ == "__main__":
    gen_feature_graphic()
    gen_phone05_privacy()
    gen_phone06_sheetcut()
    gen_phone07_paywall()
    print("\nAll assets written to", OUT)
