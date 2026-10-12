#!/usr/bin/env python3
import subprocess
import os

def generate_icons():
    # 1. Generate master 512x512 square icon
    draw_cmds = [
        # Background base
        'fill "#0F2444" rectangle 0,0 512,512',
        # Outer rings
        'stroke "#D4AF37" stroke-width 4 fill none circle 256,256 256,26',
        'stroke "#FFFFFF" stroke-opacity 0.2 stroke-width 2 fill none circle 256,256 256,20',
        # Outer Gold Shield
        'stroke none fill "#D4AF37" path "M 256,90 C 324,90 384,104 384,104 C 384,226 346,338 256,410 C 166,338 128,226 128,104 C 128,104 188,90 256,90 Z"',
        # Navy Shield Interior
        'fill "#071426" path "M 256,104 C 316,104 368,116 368,116 C 368,222 334,324 256,392 C 178,324 144,222 144,116 C 144,116 196,104 256,104 Z"',
        # Shield inner gold wireframe
        'stroke "#D4AF37" stroke-opacity 0.4 stroke-width 2 fill none path "M 256,116 C 304,116 350,126 350,126 C 350,214 320,306 256,368 C 192,306 162,214 162,126 C 162,126 208,116 256,116 Z"',
        # Three Stars
        'stroke none fill "#FFFFFF" polygon 256,126 261,140 276,140 264,149 268,163 256,154 244,163 248,149 236,140 251,140',
        'fill "#D4AF37" polygon 212,134 216,146 229,146 219,154 222,166 212,158 202,166 205,154 195,146 208,146',
        'fill "#D4AF37" polygon 300,134 304,146 317,146 307,154 310,166 300,158 290,166 293,154 283,146 296,146',
        # Pediment Roof
        'fill "#FFFFFF" polygon 256,174 176,218 336,218',
        'stroke "#D4AF37" stroke-width 4 fill none polyline 174,218 256,172 338,218',
        # Entablature Bar
        'stroke none fill "#FFFFFF" rectangle 176,220 336,232',
        'fill "#D4AF37" rectangle 180,224 332,228',
        # 4 Columns
        'fill "#FFFFFF" rectangle 190,236 208,300',
        'fill "#FFFFFF" rectangle 226,236 244,300',
        'fill "#FFFFFF" rectangle 268,236 286,300',
        'fill "#FFFFFF" rectangle 304,236 322,300',
        # Column steps / Plinth
        'fill "#FFFFFF" rectangle 172,302 340,312',
        'fill "#FFFFFF" rectangle 164,314 348,324',
        'fill "#D4AF37" rectangle 160,326 352,332',
        # Civic Book at base
        'fill "#D4AF37" path "M 256,336 C 242,326 212,326 190,332 V 370 C 212,364 242,364 256,374 C 270,364 300,364 322,370 V 332 C 300,326 270,326 256,336 Z"',
        'stroke "#071426" stroke-width 3 fill none line 256,336 256,374',
        # Liberty Torch Cup & Handle
        'stroke none fill "#D4AF37" polygon 253,250 259,250 257,294 255,294',
        'fill "#D4AF37" path "M 246,250 C 246,260 266,260 266,250 L 270,242 H 242 Z"',
        # Glowing Golden Flame
        'fill "#FF9800" path "M 256,202 C 265,216 272,226 268,236 C 264,242 260,244 256,244 C 252,244 248,242 244,236 C 240,226 247,216 256,202 Z"',
        'fill "#FFF59D" path "M 256,216 C 261,224 265,229 262,236 C 260,240 258,242 256,242 C 254,242 252,240 250,236 C 247,229 251,224 256,216 Z"'
    ]

    draw_str = " ".join([f"-draw '{cmd}'" for cmd in draw_cmds])
    os.makedirs("build/tmp", exist_ok=True)
    master_square = "build/tmp/master_icon_square.png"
    master_round = "build/tmp/master_icon_round.png"

    # Create square icon
    cmd_square = f'convert -size 512x512 xc:"#071426" {draw_str} {master_square}'
    subprocess.run(cmd_square, shell=True, check=True)

    # Create round icon (circular mask)
    cmd_round = (
        f'convert {master_square} '
        f'\\( -size 512x512 xc:none -fill white -draw "circle 256,256 256,0" \\) '
        f'-alpha set -compose DstIn -composite "PNG32:{master_round}"'
    )
    subprocess.run(cmd_round, shell=True, check=True)

    # Densities: mdpi:48, hdpi:72, xhdpi:96, xxhdpi:144, xxxhdpi:192
    densities = {
        "mdpi": 48,
        "hdpi": 72,
        "xhdpi": 96,
        "xxhdpi": 144,
        "xxxhdpi": 192
    }

    base_res = "app/src/main/res"

    for density, size in densities.items():
        dir_path = os.path.join(base_res, f"mipmap-{density}")
        os.makedirs(dir_path, exist_ok=True)

        # Remove default .webp icons
        for webp_name in ["ic_launcher.webp", "ic_launcher_round.webp"]:
            webp_path = os.path.join(dir_path, webp_name)
            if os.path.exists(webp_path):
                os.remove(webp_path)

        # Generate ic_launcher.png
        png_path = os.path.join(dir_path, "ic_launcher.png")
        subprocess.run(f'convert {master_square} -resize {size}x{size}! "PNG32:{png_path}"', shell=True, check=True)

        # Generate ic_launcher_round.png
        round_png_path = os.path.join(dir_path, "ic_launcher_round.png")
        subprocess.run(f'convert {master_round} -resize {size}x{size}! "PNG32:{round_png_path}"', shell=True, check=True)

    print("Successfully generated all density icons!")

if __name__ == "__main__":
    generate_icons()
