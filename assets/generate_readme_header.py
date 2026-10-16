# -*- coding: utf-8 -*-
"""
Generate the README header banner(s) and the 16x16 pixel icon for RebarIllumination.

Outputs (relative to the project root, i.e. the parent of this script's folder):
  - readme-header.png        Chinese banner  (default language)
  - readme-header-en.png     English banner
  - assets/icon-16x16.png    the 16x16 pixel lamp icon
  - assets/icon-preview.png  256x256 upscaled preview of the icon

Sources (kept next to this script so the repo is self-contained):
  - assets/src/banner-background.png   (AI-generated dark blurred lamp scene)
  - assets/src/icon.png                (the 16x16 pixel lamp icon, RGBA)

Run:  python assets/generate_readme_header.py
Deps: Pillow  (pip install pillow)
Fonts: Windows system fonts under C:/Windows/Fonts (Segoe UI / Microsoft YaHei).
"""
import os
from PIL import Image, ImageDraw, ImageFilter, ImageFont, ImageEnhance

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)                      # project root
FONTS = r"C:\Windows\Fonts"

SRC_BG = os.path.join(HERE, "src", "banner-background.png")
SRC_ICON = os.path.join(HERE, "src", "icon.png")

OUT_BANNER_ZH = os.path.join(ROOT, "readme-header.png")
OUT_BANNER_EN = os.path.join(ROOT, "readme-header-en.png")
OUT_ICON = os.path.join(HERE, "icon-16x16.png")
OUT_ICON_PREVIEW = os.path.join(HERE, "icon-preview.png")


def load_font(names, size):
    for n in names:
        try:
            return ImageFont.truetype(os.path.join(FONTS, n), size)
        except OSError:
            continue
    raise RuntimeError("Missing font, tried: %s" % names)


def build_icon():
    """Use assets/src/icon.png (16x16 RGBA) as-is, preserving transparency.

    The source is already the final 16x16 icon, so no crop/upscaling is needed;
    resize with NEAREST keeps pixel-art edges crisp, and RGBA is preserved so
    the transparent background does not turn black.
    """
    icon = Image.open(SRC_ICON).convert("RGBA")
    p16 = icon.resize((16, 16), Image.NEAREST)
    p16.save(OUT_ICON, optimize=True)
    p16.resize((256, 256), Image.NEAREST).save(OUT_ICON_PREVIEW)
    return p16


def build_banner(p16, subtitle, sub_size, badges):
    """Compose one banner: remove watermark, add title + subtitle + badges + framed icon."""
    banner = Image.open(SRC_BG).convert("RGB")
    # remove the AI watermark in the bottom-right corner
    patch_src = banner.crop((940, 340, 1090, 400))
    patch = patch_src.filter(ImageFilter.GaussianBlur(8)).resize((150, 60))
    banner.paste(patch, (1130, 340))
    seam = banner.crop((1120, 335, 1280, 400)).filter(ImageFilter.GaussianBlur(4))
    mask = Image.new("L", (160, 65), 0)
    ImageDraw.Draw(mask).rounded_rectangle((6, 6, 154, 59), radius=10, fill=255)
    banner.paste(seam, (1120, 335), mask.filter(ImageFilter.GaussianBlur(3)))

    # dark gradient on the left for text readability
    grad = Image.new("L", (1280, 1), 0)
    for x in range(1280):
        grad.putpixel((x, 0), int(max(0, 150 * (1 - x / 760.0))))
    dark = Image.new("RGB", (1280, 400), (8, 8, 10))
    banner = Image.composite(dark, banner, grad.resize((1280, 400)))

    overlay = Image.new("RGBA", (1280, 400), (0, 0, 0, 0))
    d = ImageDraw.Draw(overlay)

    LX = 76
    d.text((LX, 76), "Rebar Illumination",
           font=load_font(["SegoeUIBlack.ttf", "arialbd.ttf", "segoeuib.ttf"], 74),
           fill=(255, 255, 255, 255))
    d.text((LX + 2, 178), subtitle,
           font=load_font(["msyhbd.ttc", "msyh.ttc"], sub_size),
           fill=(224, 158, 62, 255))

    badge_font = load_font(["msyh.ttc"], 19)
    bx, by, bh, gap, pad = LX, 246, 40, 14, 24
    for t in badges:
        w = d.textlength(t, font=badge_font)
        d.rounded_rectangle((bx, by, bx + w + pad * 2, by + bh), radius=9,
                            fill=(44, 44, 46, 210))
        d.text((bx + pad, by + 8), t, font=badge_font, fill=(226, 226, 228, 255))
        bx += w + pad * 2 + gap

    banner = Image.alpha_composite(banner.convert("RGBA"), overlay)

    # framed pixel icon on the right
    PLATE = 208
    px, py = 1280 - PLATE - 72, (400 - PLATE) // 2
    plate = Image.new("RGBA", (PLATE, PLATE), (0, 0, 0, 0))
    pd = ImageDraw.Draw(plate)
    pd.rounded_rectangle((0, 0, PLATE - 1, PLATE - 1), radius=18, fill=(16, 16, 18, 200))
    pd.rounded_rectangle((0, 0, PLATE - 1, PLATE - 1), radius=18,
                         outline=(255, 255, 255, 36), width=2)
    banner.alpha_composite(plate, (px, py))
    big = p16.resize((176, 176), Image.NEAREST)
    banner.alpha_composite(big.convert("RGBA"),
                           (px + (PLATE - 176) // 2, py + (PLATE - 176) // 2))
    return banner.convert("RGB")


def main():
    p16 = build_icon()
    zh = build_banner(p16,
                      "丰富的彩色照明系统 · 5 种灯具", 30,
                      ["Rebar 附属", "MC 26.2"])
    zh.save(OUT_BANNER_ZH, optimize=True)
    en = build_banner(p16,
                      "Colorful lights · 5 lamp types", 26,
                      ["Rebar Addon", "MC 26.2"])
    en.save(OUT_BANNER_EN, optimize=True)
    print("Generated:")
    for f in (OUT_BANNER_ZH, OUT_BANNER_EN, OUT_ICON, OUT_ICON_PREVIEW):
        print("  ", os.path.relpath(f, ROOT))


if __name__ == "__main__":
    main()
