from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

out = Path(__file__).resolve().parents[1] / "play-store-assets"
out.mkdir(exist_ok=True)

WOOD = (139, 69, 19)
WOOD_DARK = (82, 39, 11)
TAN = (198, 134, 66)
CREAM = (250, 246, 241)
CHARCOAL = (51, 51, 51)
MUTED = (107, 97, 87)
GREEN = (74, 103, 65)
OUTLINE = (226, 217, 205)
WHITE = (255, 255, 255)

font_candidates = [
    "/System/Library/Fonts/Supplemental/Arial.ttf",
    "/System/Library/Fonts/Supplemental/Helvetica.ttf",
    "/Library/Fonts/Arial.ttf",
]
bold_candidates = [
    "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
    "/System/Library/Fonts/Supplemental/Helvetica Bold.ttf",
    "/Library/Fonts/Arial Bold.ttf",
]

def font(size, bold=False):
    paths = bold_candidates if bold else font_candidates
    for path in paths:
        try:
            return ImageFont.truetype(path, size)
        except Exception:
            pass
    return ImageFont.load_default()

def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

def gradient(size, top, bottom):
    width, height = size
    image = Image.new("RGB", size, top)
    pixels = image.load()
    for y in range(height):
        color = lerp(top, bottom, y / max(1, height - 1))
        for x in range(width):
            pixels[x, y] = color
    return image

def text(draw, xy, value, size=42, fill=CHARCOAL, bold=False, anchor=None, max_width=None, line_gap=8):
    current_font = font(size, bold)
    if max_width is None:
        draw.text(xy, value, font=current_font, fill=fill, anchor=anchor)
        return
    words = value.split()
    lines = []
    current = ""
    for word in words:
        trial = (current + " " + word).strip()
        if draw.textlength(trial, font=current_font) <= max_width or not current:
            current = trial
        else:
            lines.append(current)
            current = word
    if current:
        lines.append(current)
    x, y = xy
    for line in lines:
        draw.text((x, y), line, font=current_font, fill=fill)
        y += size + line_gap

def pill(draw, xy, value, fill=(244, 231, 215), fg=WOOD_DARK, size=26):
    current_font = font(size, True)
    x, y = xy
    width = int(draw.textlength(value, font=current_font) + 36)
    height = size + 24
    draw.rounded_rectangle((x, y, x + width, y + height), radius=height // 2, fill=fill)
    draw.text((x + 18, y + 11), value, font=current_font, fill=fg)
    return width, height


# ----------------------------------------------------------------------------
# 3D / depth helpers — applied across every asset to keep a consistent feel.
# ----------------------------------------------------------------------------

def _ensure_rgba(color):
    if len(color) == 3:
        return color + (255,)
    return color


def vertical_gradient_image(size, top, bottom):
    """RGBA vertical gradient. Used to fill cards/buttons with depth."""
    top = _ensure_rgba(top)
    bottom = _ensure_rgba(bottom)
    width, height = size
    image = Image.new("RGBA", size, top)
    pixels = image.load()
    for y in range(height):
        t = y / max(1, height - 1)
        color = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(4))
        for x in range(width):
            pixels[x, y] = color
    return image


def drop_shadow(image, box, radius=24, blur=18, offset=(0, 10), color=(60, 35, 18, 90)):
    """Single soft drop shadow under a rounded rect."""
    layer = Image.new("RGBA", image.size, (0, 0, 0, 0))
    drawer = ImageDraw.Draw(layer)
    x1, y1, x2, y2 = box
    drawer.rounded_rectangle(
        (x1 + offset[0], y1 + offset[1], x2 + offset[0], y2 + offset[1]),
        radius=radius,
        fill=color,
    )
    layer = layer.filter(ImageFilter.GaussianBlur(blur))
    image.alpha_composite(layer)


def card_3d(
    image,
    box,
    radius=28,
    fill=WHITE,
    fill_dark=None,
    outline=OUTLINE,
    outline_width=1,
    shadow_alpha=80,
    shadow_blur=16,
    shadow_offset=(0, 10),
    top_highlight=True,
):
    """Premium card: drop shadow + vertical gradient fill + top glass highlight."""
    drop_shadow(image, box, radius=radius, blur=shadow_blur, offset=shadow_offset, color=(60, 35, 18, shadow_alpha))
    x1, y1, x2, y2 = box
    width = x2 - x1
    height = y2 - y1
    if fill_dark is None:
        light = _ensure_rgba(tuple(min(255, c + 4) for c in fill[:3]))
        dark = _ensure_rgba(tuple(max(0, c - 8) for c in fill[:3]))
    else:
        light = _ensure_rgba(fill)
        dark = _ensure_rgba(fill_dark)
    canvas = vertical_gradient_image((width, height), light, dark)
    mask = Image.new("L", (width, height), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, width, height), radius=radius, fill=255)
    image.paste(canvas, (x1, y1), mask)
    drawer = ImageDraw.Draw(image)
    if outline is not None:
        drawer.rounded_rectangle(box, radius=radius, outline=outline, width=outline_width)
    if top_highlight and width > 24:
        hl = Image.new("RGBA", (width, 4), (0, 0, 0, 0))
        ImageDraw.Draw(hl).rounded_rectangle((10, 0, width - 10, 3), radius=2, fill=(255, 255, 255, 130))
        image.alpha_composite(hl, dest=(x1, y1 + 1))


def gradient_pill(image, xy, value, top_color, bottom_color, fg=WHITE, size=22, padding_x=18, shadow=True):
    """Pill-shaped chip with a vertical gradient and optional drop shadow."""
    drawer = ImageDraw.Draw(image)
    f = font(size, True)
    x, y = xy
    width = int(drawer.textlength(value, font=f) + padding_x * 2)
    height = size + 20
    box = (x, y, x + width, y + height)
    if shadow:
        drop_shadow(image, box, radius=height // 2, blur=8, offset=(0, 4), color=(60, 30, 10, 90))
    canvas = vertical_gradient_image((width, height), top_color, bottom_color)
    mask = Image.new("L", (width, height), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, width, height), radius=height // 2, fill=255)
    image.paste(canvas, (x, y), mask)
    drawer.text((x + padding_x, y + 9), value, font=f, fill=fg)
    return width, height


def wood_grain_overlay(image, alpha=14, spacing=46):
    """Subtle warm arc lines reminiscent of wood grain, used as overlay."""
    width, height = image.size
    layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    drawer = ImageDraw.Draw(layer)
    for y in range(-40, height + 80, spacing):
        drawer.arc((-160, y, width + 160, y + 110), 200, 340, fill=(160, 110, 70, alpha), width=2)
    layer = layer.filter(ImageFilter.GaussianBlur(0.6))
    image.alpha_composite(layer)


def emboss_text(
    draw,
    xy,
    value,
    size,
    fill,
    bold=True,
    shadow_color=(60, 30, 12, 110),
    shadow_offset=(0, 2),
    max_width=None,
    line_gap=10,
):
    """Text with a soft drop shadow underneath the glyphs for a 3D feel.

    Supports automatic word-wrapping when ``max_width`` is provided.
    """
    f = font(size, bold)
    if max_width is None:
        x, y = xy
        draw.text((x + shadow_offset[0], y + shadow_offset[1]), value, font=f, fill=shadow_color)
        draw.text((x, y), value, font=f, fill=fill)
        return
    words = value.split()
    lines = []
    current = ""
    for word in words:
        trial = (current + " " + word).strip()
        if draw.textlength(trial, font=f) <= max_width or not current:
            current = trial
        else:
            lines.append(current)
            current = word
    if current:
        lines.append(current)
    x, y = xy
    for line in lines:
        draw.text((x + shadow_offset[0], y + shadow_offset[1]), line, font=f, fill=shadow_color)
        draw.text((x, y), line, font=f, fill=fill)
        y += size + line_gap

def shadow_card(base, box, radius=34, fill=WHITE, outline=OUTLINE, shadow=(70, 45, 25, 34), blur=22, offset=(0, 10)):
    layer = Image.new("RGBA", base.size, (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(layer)
    x1, y1, x2, y2 = box
    shadow_draw.rounded_rectangle((x1 + offset[0], y1 + offset[1], x2 + offset[0], y2 + offset[1]), radius=radius, fill=shadow)
    layer = layer.filter(ImageFilter.GaussianBlur(blur))
    base.alpha_composite(layer)
    draw = ImageDraw.Draw(base)
    draw.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=2)

def draw_ruler(draw, x, y, width, height, fill=WOOD, tick=CREAM):
    draw.rounded_rectangle((x, y, x + width, y + height), radius=16, fill=fill)
    for index in range(11):
        tick_x = x + 18 + index * (width - 36) / 10
        tick_height = 34 if index % 2 == 0 else 22
        draw.line((tick_x, y + 8, tick_x, y + 8 + tick_height), fill=tick, width=4)

def draw_board(draw, x, y, width, height, fill=TAN):
    draw.rounded_rectangle((x, y, x + width, y + height), radius=22, fill=fill)
    for index in range(4):
        arc_y = y + 18 + index * height / 5
        draw.arc((x + 20, arc_y - 26, x + width - 20, arc_y + 58), 180, 350, fill=(160, 93, 38), width=3)

def draw_tool_glyph(image, cx, cy, kind, color):
    """Render a small monochrome tool glyph centered at (cx, cy).

    Keeps the visual language consistent with the new app icon (carpenter's
    square aesthetic) but varies per category so the home grid feels alive.
    """
    drawer = ImageDraw.Draw(image)
    if kind == "miter":
        # Open angle / chevron.
        drawer.line((cx - 16, cy + 12, cx, cy - 12), fill=color, width=5)
        drawer.line((cx, cy - 12, cx + 16, cy + 12), fill=color, width=5)
    elif kind == "cut":
        # Board with two cut marks.
        drawer.rounded_rectangle((cx - 18, cy - 8, cx + 18, cy + 8), radius=3, fill=color)
        drawer.rectangle((cx - 7, cy - 14, cx - 5, cy + 14), fill=color)
        drawer.rectangle((cx + 5, cy - 14, cx + 7, cy + 14), fill=color)
    elif kind == "board":
        # Solid wide rounded board.
        drawer.rounded_rectangle((cx - 18, cy - 10, cx + 18, cy + 10), radius=4, fill=color)
    elif kind == "spacing":
        # Three evenly spaced dots.
        for dx in (-14, 0, 14):
            drawer.ellipse((cx + dx - 5, cy - 5, cx + dx + 5, cy + 5), fill=color)
    elif kind == "stairs":
        # Three stepped blocks ascending right.
        drawer.rectangle((cx - 16, cy + 6, cx - 6, cy + 14), fill=color)
        drawer.rectangle((cx - 6, cy - 2, cx + 6, cy + 14), fill=color)
        drawer.rectangle((cx + 6, cy - 12, cx + 16, cy + 14), fill=color)
    elif kind == "lumber":
        # Stack of horizontal lines (lumber reference).
        for dy in (-10, -3, 4, 11):
            drawer.line((cx - 16, cy + dy, cx + 16, cy + dy), fill=color, width=3)
    else:
        drawer.rounded_rectangle((cx - 12, cy - 12, cx + 12, cy + 12), radius=4, fill=color)


def mini_card(image, x, y, width, height, title, description, accent=TAN, glyph="board"):
    """Material 3-styled tool tile: solid white surface, soft elevation, a
    tinted rounded-square icon container with a tool glyph, and clear
    title/description typography. No glow, no accent bar — keeps the design
    quiet so the typography does the work.
    """
    box = (x, y, x + width, y + height)
    radius = 28

    # Subtle drop shadow under card (M3 elevation 1).
    drop_shadow(image, box, radius=radius, blur=14, offset=(0, 6), color=(60, 35, 18, 60))

    drawer = ImageDraw.Draw(image)
    # Solid white surface with a hairline outline.
    drawer.rounded_rectangle(box, radius=radius, fill=(255, 255, 255), outline=(228, 220, 208), width=1)

    # Icon container: rounded square in brand tint (~22% alpha).
    icon_size = 60
    icon_left = x + 28
    icon_top = y + 30
    tint_layer = Image.new("RGBA", (icon_size, icon_size), (0, 0, 0, 0))
    ImageDraw.Draw(tint_layer).rounded_rectangle(
        (0, 0, icon_size, icon_size), radius=16, fill=(*accent[:3], 56)
    )
    image.alpha_composite(tint_layer, dest=(icon_left, icon_top))

    # Inner glyph in solid brand color.
    draw_tool_glyph(
        image,
        icon_left + icon_size // 2,
        icon_top + icon_size // 2,
        glyph,
        accent[:3] + (255,),
    )

    # Title + description.
    text(drawer, (x + 110, y + 38), title, 32, CHARCOAL, True, max_width=width - 138)
    text(drawer, (x + 30, y + 118), description, 24, MUTED, False, max_width=width - 60, line_gap=6)

def result_row(image, y, label, value, accent=False):
    """Single key/value row inside a result card. Accent rows get a flat
    brand-tinted pill so the most important number stands out without the
    busy gradient look.
    """
    drawer = ImageDraw.Draw(image)
    text(drawer, (112, y), label, 30, MUTED, False)
    if accent:
        pill_left = 600
        pill_right = 952
        drawer.rounded_rectangle(
            (pill_left, y - 10, pill_right, y + 54),
            radius=32,
            fill=(248, 232, 208, 255),
        )
        text(drawer, (930, y), value, 32, WOOD_DARK, True, anchor="ra")
    else:
        text(drawer, (930, y), value, 30, CHARCOAL, True, anchor="ra")

def _wrap_lines(value, font_obj, max_width):
    tmp = Image.new("RGBA", (1, 1), (0, 0, 0, 0))
    drawer = ImageDraw.Draw(tmp)
    words = value.split()
    lines = []
    current = ""
    for word in words:
        trial = (current + " " + word).strip()
        if drawer.textlength(trial, font=font_obj) <= max_width or not current:
            current = trial
        else:
            lines.append(current)
            current = word
    if current:
        lines.append(current)
    return lines


def screenshot_base(title, subtitle):
    # Clean, neutral cream gradient. No wood grain, no arc decorations —
    # follows mainstream Material 3 / Google Play paid-utility screenshots.
    image = gradient((1080, 1920), (252, 248, 242), (240, 232, 220)).convert("RGBA")
    draw = ImageDraw.Draw(image)

    # Auto-shrink title to keep it on a single line. Keeps the layouts below
    # (pills, cards, results) at fixed y values so the screenshots stay
    # visually consistent across the set.
    title_size = 58
    title_font = font(title_size, True)
    while title_size > 38 and draw.textlength(title, font=title_font) > 940:
        title_size -= 3
        title_font = font(title_size, True)
    draw.text((74, 82), title, font=title_font, fill=WOOD_DARK)

    # Auto-shrink subtitle so it fits within two lines.
    sub_size = 32
    sub_font = font(sub_size, False)
    sub_lines = _wrap_lines(subtitle, sub_font, max_width=900)
    while sub_size > 22 and len(sub_lines) > 2:
        sub_size -= 2
        sub_font = font(sub_size, False)
        sub_lines = _wrap_lines(subtitle, sub_font, max_width=900)
    text(draw, (76, 188), subtitle, sub_size, MUTED, False, max_width=900, line_gap=8)
    return image, draw

def create_icon():
    """Premium app icon: a solid wood-toned canvas filled edge-to-edge with a
    diagonal gradient, with a clean white carpenter's square (right-angle
    ruler) centered on top.

    No letters, no PRO chip, no transparent corners — designed in the spirit
    of mainstream paid utilities (think Material 3 / Google Play tier).
    """
    image = Image.new("RGB", (512, 512), (180, 124, 56))
    pixels = image.load()
    top_left = (208, 152, 86)
    bottom_right = (108, 56, 18)
    for y in range(512):
        for x in range(512):
            t = ((x + y) / 1023) ** 0.92
            pixels[x, y] = (
                int(top_left[0] + (bottom_right[0] - top_left[0]) * t),
                int(top_left[1] + (bottom_right[1] - top_left[1]) * t),
                int(top_left[2] + (bottom_right[2] - top_left[2]) * t),
            )
    image = image.convert("RGBA")

    # Soft top-left light wash for subtle depth (no obvious gradient seams).
    wash = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
    ImageDraw.Draw(wash).ellipse((-220, -220, 380, 380), fill=(255, 240, 210, 70))
    wash = wash.filter(ImageFilter.GaussianBlur(140))
    image.alpha_composite(wash)

    # ---------- Carpenter's square (white L-shape with tick marks) ----------
    arm_thickness = 60
    arm_length = 308
    # Anchor the L so that its bounding box is centered on the canvas.
    pivot_x = (512 - arm_length) // 2
    pivot_y = (512 + arm_length) // 2

    # Drop shadow for the L (single soft layer).
    shadow_layer = Image.new("RGBA", image.size, (0, 0, 0, 0))
    sd = ImageDraw.Draw(shadow_layer)
    sd.rounded_rectangle(
        (pivot_x + 6, pivot_y - arm_thickness + 12, pivot_x + arm_length + 6, pivot_y + 12),
        radius=10,
        fill=(40, 20, 6, 150),
    )
    sd.rounded_rectangle(
        (pivot_x + 6, pivot_y - arm_length + 12, pivot_x + arm_thickness + 6, pivot_y + 12),
        radius=10,
        fill=(40, 20, 6, 150),
    )
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(10))
    image.alpha_composite(shadow_layer)

    draw = ImageDraw.Draw(image)
    arm_color = (255, 252, 245, 255)
    # Horizontal arm.
    draw.rounded_rectangle(
        (pivot_x, pivot_y - arm_thickness, pivot_x + arm_length, pivot_y),
        radius=10,
        fill=arm_color,
    )
    # Vertical arm.
    draw.rounded_rectangle(
        (pivot_x, pivot_y - arm_length, pivot_x + arm_thickness, pivot_y),
        radius=10,
        fill=arm_color,
    )

    # Tick marks on the inner edges (single brown tone, evenly spaced).
    tick_color = (110, 56, 16, 230)
    inner_top_y = pivot_y - arm_thickness
    for i in range(1, 11):
        tick_x = pivot_x + arm_thickness + 8 + i * 22
        if tick_x > pivot_x + arm_length - 16:
            break
        tick_h = 14 if i % 2 == 0 else 9
        draw.line((tick_x, inner_top_y + 6, tick_x, inner_top_y + 6 + tick_h), fill=tick_color, width=4)

    inner_right_x = pivot_x + arm_thickness
    for i in range(1, 11):
        tick_y = pivot_y - arm_thickness - 8 - i * 22
        if tick_y < pivot_y - arm_length + 16:
            break
        tick_w = 14 if i % 2 == 0 else 9
        draw.line((inner_right_x - 6, tick_y, inner_right_x - 6 - tick_w, tick_y), fill=tick_color, width=4)

    # Pivot dot at the inner corner — small visual anchor.
    pivot_dot_cx = pivot_x + arm_thickness - 14
    pivot_dot_cy = pivot_y - arm_thickness + 14
    draw.ellipse(
        (pivot_dot_cx - 8, pivot_dot_cy - 8, pivot_dot_cx + 8, pivot_dot_cy + 8),
        fill=(110, 56, 16, 230),
    )

    image.convert("RGB").save(out / "icon-512.png")

def draw_realistic_phone_frame(image, x, y, width, height):
    """Draw a modern phone body and a clean screen background. Returns screen bounds."""
    draw = ImageDraw.Draw(image)

    # Soft drop shadow underneath the phone.
    shadow_layer = Image.new("RGBA", image.size, (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow_layer)
    shadow_draw.rounded_rectangle(
        (x + 10, y + 22, x + width + 10, y + height + 22),
        radius=46,
        fill=(60, 35, 18, 110),
    )
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(22))
    image.alpha_composite(shadow_layer)

    # Outer titanium-style frame.
    body_radius = 44
    draw.rounded_rectangle(
        (x - 2, y - 2, x + width + 2, y + height + 2),
        radius=body_radius + 2,
        fill=(70, 70, 74),
    )
    draw.rounded_rectangle(
        (x, y, x + width, y + height),
        radius=body_radius,
        fill=(22, 22, 24),
    )

    # Glass highlight along the top edge.
    highlight = Image.new("RGBA", (width, 6), (0, 0, 0, 0))
    ImageDraw.Draw(highlight).rounded_rectangle(
        (12, 0, width - 12, 5), radius=2, fill=(255, 255, 255, 60)
    )
    image.alpha_composite(highlight, dest=(x, y + 3))

    # Side buttons (mute, volume up, volume down on the left, power on the right).
    btn_color = (40, 40, 42)
    draw.rounded_rectangle((x - 3, y + 78, x + 1, y + 102), radius=2, fill=btn_color)
    draw.rounded_rectangle((x - 3, y + 132, x + 1, y + 174), radius=2, fill=btn_color)
    draw.rounded_rectangle((x - 3, y + 188, x + 1, y + 230), radius=2, fill=btn_color)
    draw.rounded_rectangle((x + width - 1, y + 158, x + width + 3, y + 218), radius=2, fill=btn_color)

    # Inner screen with a small uniform bezel.
    bezel = 6
    sx, sy = x + bezel, y + bezel
    sw, sh = width - bezel * 2, height - bezel * 2
    screen_radius = body_radius - bezel - 2
    draw.rounded_rectangle((sx, sy, sx + sw, sy + sh), radius=screen_radius, fill=CREAM)
    return sx, sy, sw, sh, screen_radius


def draw_phone_chrome_overlay(image, sx, sy, sw, sh):
    """Dynamic island plus home indicator drawn on top of the screen content."""
    draw = ImageDraw.Draw(image)

    island_w, island_h = 78, 22
    ix = sx + (sw - island_w) // 2
    iy = sy + 12
    draw.rounded_rectangle(
        (ix, iy, ix + island_w, iy + island_h),
        radius=island_h // 2,
        fill=(10, 10, 12),
    )
    lens_r = 4
    lens_cx = ix + island_w - 14
    lens_cy = iy + island_h // 2
    draw.ellipse(
        (lens_cx - lens_r, lens_cy - lens_r, lens_cx + lens_r, lens_cy + lens_r),
        fill=(34, 34, 40),
    )

    bar_w = 96
    draw.rounded_rectangle(
        (
            sx + (sw - bar_w) // 2,
            sy + sh - 12,
            sx + (sw + bar_w) // 2,
            sy + sh - 8,
        ),
        radius=2,
        fill=(40, 40, 40),
    )


def draw_inline_app_ui(image, sx, sy, sw, sh):
    """Tailored mini app UI rendered for the small phone preview area."""
    draw = ImageDraw.Draw(image)

    pad = 20
    title_y = sy + 50
    draw.text((sx + pad, title_y), "Woodworking", font=font(18, True), fill=WOOD_DARK)
    draw.text((sx + pad, title_y + 22), "Calculator Pro", font=font(18, True), fill=WOOD_DARK)

    f_pill = font(10, True)
    pill_text = "100% OFFLINE"
    pill_w = int(draw.textlength(pill_text, font=f_pill)) + 14
    pill_y = title_y + 52
    draw.rounded_rectangle(
        (sx + pad, pill_y, sx + pad + pill_w, pill_y + 18),
        radius=9,
        fill=(244, 231, 215),
    )
    draw.text((sx + pad + 7, pill_y + 3), pill_text, font=f_pill, fill=WOOD_DARK)

    tools = [
        ("Miter", TAN),
        ("Board Cut", GREEN),
        ("Board Feet", TAN),
        ("Spacing", GREEN),
        ("Stairs", TAN),
        ("Lumber", GREEN),
    ]
    gap = 8
    cw = (sw - pad * 2 - gap) // 2
    ch = 70
    grid_top = pill_y + 30
    f_card = font(11, True)
    for index, (name, accent) in enumerate(tools):
        col = index % 2
        row = index // 2
        cx = sx + pad + col * (cw + gap)
        cy = grid_top + row * (ch + gap)
        draw.rounded_rectangle(
            (cx, cy, cx + cw, cy + ch),
            radius=10,
            fill=WHITE,
            outline=OUTLINE,
            width=1,
        )
        # Tinted icon container in brand color (low alpha) — matches main grid.
        tint = Image.new("RGBA", (20, 20), (0, 0, 0, 0))
        ImageDraw.Draw(tint).rounded_rectangle((0, 0, 20, 20), radius=5, fill=(*accent[:3], 56))
        image.alpha_composite(tint, dest=(cx + 8, cy + 8))
        # Solid dot inside the container (universal mini glyph).
        draw.ellipse((cx + 13, cy + 13, cx + 23, cy + 23), fill=accent[:3] + (255,))
        draw.text((cx + 8, cy + 34), name, font=f_card, fill=CHARCOAL)


def create_feature_graphic():
    # Calm cream gradient background — no wood grain, no diagonal lines.
    image = gradient((1024, 500), (252, 248, 242), (238, 230, 218)).convert("RGBA")
    draw = ImageDraw.Draw(image)

    # Brand title (clean, no emboss).
    text(draw, (62, 70), "Woodworking", 58, WOOD_DARK, True)
    text(draw, (62, 130), "Calculator Pro", 58, WOOD_DARK, True)

    # Outline brand chip (M3 style).
    chip_text_value = "100% OFFLINE · NO ADS · NO IAP"
    chip_font = font(22, True)
    chip_w = int(draw.textlength(chip_text_value, font=chip_font)) + 36
    chip_h = 44
    chip_x = 62
    chip_y = 212
    draw.rounded_rectangle(
        (chip_x, chip_y, chip_x + chip_w, chip_y + chip_h),
        radius=chip_h // 2,
        fill=(255, 255, 255, 255),
        outline=(168, 124, 70, 255),
        width=2,
    )
    draw.text((chip_x + 18, chip_y + 11), chip_text_value, font=chip_font, fill=WOOD_DARK)

    # Bullets — flat dots, clear typography.
    bullets = [
        "10 practical workshop calculators",
        "Board feet, cut lists, spacing & costs",
        "Built for overseas paid Google Play users",
    ]
    for index, line in enumerate(bullets):
        y = 290 + index * 52
        draw.ellipse((68, y + 9, 86, y + 27), fill=GREEN)
        text(draw, (104, y), line, 30, CHARCOAL, False)

    # Realistic phone mockup with the inline app UI on the right.
    phone_w, phone_h = 216, 442
    phone_x = 736
    phone_y = (500 - phone_h) // 2
    sx, sy, sw, sh, _ = draw_realistic_phone_frame(image, phone_x, phone_y, phone_w, phone_h)
    draw_inline_app_ui(image, sx, sy, sw, sh)
    draw_phone_chrome_overlay(image, sx, sy, sw, sh)

    image.save(out / "feature-graphic-1024x500.png")

def create_home_screenshot():
    image, draw = screenshot_base("10 premium woodworking calculators", "Fast workshop math for cuts, lumber, spacing, stairs, flooring, paint and conversions.")
    pill(draw, (76, 308), "100% offline · no ads · local history", fill=(244, 231, 215), fg=WOOD_DARK, size=28)
    cards = [
        ("Miter Angle", "Corner cuts for frames & polygons"),
        ("Board Optimizer", "Cut layouts with saw kerf"),
        ("Board Feet & Cost", "Lumber volume, waste & price"),
        ("Equal Spacing", "Balusters, slats & shelves"),
        ("Stair Layout", "Risers, treads & stringers"),
        ("Lumber Reference", "Nominal vs actual sizes"),
    ]
    glyphs = ["miter", "cut", "board", "spacing", "stairs", "lumber"]
    for index, (title_text, description) in enumerate(cards):
        mini_card(
            image,
            76 + (index % 2) * 464,
            405 + (index // 2) * 270,
            424,
            218,
            title_text,
            description,
            accent=TAN if index % 2 == 0 else GREEN,
            glyph=glyphs[index],
        )
    image.save(out / "phone-01-home-1080x1920.png")

def create_board_optimizer_screenshot():
    image, draw = screenshot_base("Optimize every board before you cut", "Plan stock length, kerf, quantities, efficiency and waste in one offline tool.")
    shadow_card(image, (70, 340, 1010, 1660), 36, fill=WHITE, outline=OUTLINE, shadow=(70, 45, 25, 30))
    text(draw, (112, 382), "Board Optimizer", 44, WOOD_DARK, True)
    inputs = [("Stock length", "96 in"), ("Saw kerf", "0.125 in"), ("Cut A", "24 in × 3"), ("Cut B", "18 in × 4"), ("Cut C", "12 in × 5")]
    for index, (label, value) in enumerate(inputs):
        y = 478 + index * 92
        draw.rounded_rectangle((112, y, 948, y + 64), radius=20, fill=(250, 246, 241), outline=OUTLINE, width=2)
        text(draw, (140, y + 16), label, 25, MUTED, False)
        text(draw, (918, y + 16), value, 25, CHARCOAL, True, anchor="ra")
    result_row(image, 1015, "Stock pieces needed", "3", True)
    result_row(image, 1100, "Material efficiency", "91.4%", False)
    result_row(image, 1185, "Total waste", "8.25 in", False)
    text(draw, (112, 1322), "Cut layout", 32, CHARCOAL, True)
    for y in [1388, 1480]:
        draw.rounded_rectangle((112, y, 948, y + 38), radius=19, fill=(244, 231, 215))
        x = 124
        for segment, color in [(210, TAN), (160, GREEN), (240, TAN), (90, (210, 160, 110))]:
            draw.rounded_rectangle((x, y + 6, x + segment, y + 32), radius=13, fill=color)
            x += segment + 8
    image.save(out / "phone-02-board-optimizer-1080x1920.png")

def create_board_feet_screenshot():
    image, draw = screenshot_base("Estimate lumber volume and cost", "Board feet, waste allowance, and price totals for North American lumber buying.")
    shadow_card(image, (70, 340, 1010, 1588), 36, fill=WHITE, outline=OUTLINE, shadow=(70, 45, 25, 30))
    text(draw, (112, 382), "Board Feet & Cost", 44, WOOD_DARK, True)
    inputs = [("Thickness", "1.5 in"), ("Width", "5.5 in"), ("Length", "8 ft"), ("Quantity", "6"), ("Waste", "10%"), ("Price", "$4.25 / bd ft")]
    for index, (label, value) in enumerate(inputs):
        x = 112 + (index % 2) * 424
        y = 488 + (index // 2) * 120
        draw.rounded_rectangle((x, y, x + 392, y + 76), radius=22, fill=(250, 246, 241), outline=OUTLINE, width=2)
        text(draw, (x + 24, y + 14), label, 23, MUTED, False)
        text(draw, (x + 366, y + 14), value, 23, CHARCOAL, True, anchor="ra")
    result_row(image, 940, "Total board feet", "39.930 bd ft", True)
    result_row(image, 1038, "Board feet / piece", "5.500 bd ft", False)
    result_row(image, 1136, "Waste allowance", "3.630 bd ft", False)
    result_row(image, 1234, "Estimated cost", "$169.70", True)
    pill(draw, (112, 1390), "One-time $5.99 utility · no subscription", fill=(244, 231, 215), fg=WOOD_DARK, size=26)
    image.save(out / "phone-03-board-feet-1080x1920.png")

def create_spacing_screenshot():
    image, draw = screenshot_base("Equal spacing without guesswork", "Mark balusters, slats, screws, shelves and decorative strips with clean center positions.")
    shadow_card(image, (70, 340, 1010, 1630), 36, fill=WHITE, outline=OUTLINE, shadow=(70, 45, 25, 30))
    text(draw, (112, 382), "Equal Spacing", 44, WOOD_DARK, True)
    pill(draw, (112, 460), "Known count", fill=WOOD, fg=WHITE, size=25)
    pill(draw, (330, 460), "Target gap", fill=(244, 231, 215), fg=WOOD_DARK, size=25)
    for index, (label, value) in enumerate([("Opening / span", "96 in"), ("Item width", "1.5 in"), ("Item count", "7")]):
        y = 565 + index * 96
        draw.rounded_rectangle((112, y, 948, y + 66), radius=20, fill=(250, 246, 241), outline=OUTLINE, width=2)
        text(draw, (140, y + 17), label, 25, MUTED, False)
        text(draw, (918, y + 17), value, 25, CHARCOAL, True, anchor="ra")
    result_row(image, 925, "Equal gap", "10.688 in", True)
    result_row(image, 1015, "Center-to-center", "12.188 in", False)
    result_row(image, 1105, "First center mark", "11.438 in", False)
    text(draw, (112, 1266), "Marks from start", 31, CHARCOAL, True)
    draw.line((142, 1385, 918, 1385), fill=WOOD_DARK, width=8)
    for index in range(7):
        x = 170 + index * 116
        draw.line((x, 1338, x, 1432), fill=TAN, width=8)
        draw.ellipse((x - 12, 1373, x + 12, 1397), fill=GREEN)
    text(draw, (112, 1490), "11.44, 23.63, 35.81, 48.00, 60.19, 72.38, 84.56 in", 25, MUTED, False, max_width=850)
    image.save(out / "phone-04-equal-spacing-1080x1920.png")

def create_privacy_screenshot():
    image, draw = screenshot_base("Private by design", "Save calculation history locally. No tracking, no ads, no internet permission.")
    shadow_card(image, (70, 340, 1010, 1630), 36, fill=WHITE, outline=OUTLINE, shadow=(70, 45, 25, 30))
    text(draw, (112, 382), "Local History", 44, WOOD_DARK, True)
    entries = [
        ("Board Feet & Cost", "Total board feet: 39.930 bd ft · Estimated cost: $169.70"),
        ("Board Optimizer", "Stock pieces: 3 · Efficiency: 91.4%"),
        ("Equal Spacing", "Gap: 10.688 in · Center-to-center: 12.188 in"),
    ]
    for index, (title, description) in enumerate(entries):
        y = 510 + index * 220
        draw.rounded_rectangle((112, y, 948, y + 160), radius=30, fill=(250, 246, 241), outline=OUTLINE, width=2)
        text(draw, (146, y + 30), title, 31, CHARCOAL, True)
        text(draw, (146, y + 82), description, 24, MUTED, False, max_width=740)
    for index, item in enumerate(["No INTERNET permission", "No analytics SDK", "No advertising SDK", "No in-app purchases"]):
        y = 1230 + index * 76
        draw.ellipse((126, y + 8, 158, y + 40), fill=GREEN)
        text(draw, (178, y), item, 34, CHARCOAL, True)
    image.save(out / "phone-05-privacy-history-1080x1920.png")

def main():
    create_icon()
    create_feature_graphic()
    create_home_screenshot()
    create_board_optimizer_screenshot()
    create_board_feet_screenshot()
    create_spacing_screenshot()
    create_privacy_screenshot()
    for path in sorted(out.glob("*.png")):
        with Image.open(path) as image:
            print(f"{path.name}: {image.size[0]}x{image.size[1]}")

if __name__ == "__main__":
    main()
