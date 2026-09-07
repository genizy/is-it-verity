"""Re-project the GD difficulty faces onto the sphere blocks.

Match the rest to a face you edited yourself:
    python gd_faces_spheres.py --match easy

That measures how much of the ball the face covers in easy.png, redraws the other
eleven at exactly that size, and never touches easy.png itself.

Or set the size by hand:
    python gd_faces_spheres.py --fill 0.72     (bigger faces)
    python gd_faces_spheres.py --fill 0.50     (smaller faces)

The first run copies the untouched art into  <project>\\.gdfaces-src  and every run
reads from there, so you can re-run as many times as you like without the faces
shrinking again each time. Delete that folder once you are happy with the result.
"""

import argparse
import colorsys
import json
import math
import pathlib
import shutil
import subprocess
import sys

MOD_ID = "is_it_verity"
DEFAULT_PROJECT = r"C:\Users\mhser\is it verity"

NAMES = [
    "unrated", "auto", "easy", "normal", "hard", "harder",
    "insane", "easy-demon", "medium-demon", "hard-demon",
    "insane-demon", "extreme-demon",
]

SUPERSAMPLE = 3
ALPHA_CUTOFF = 8
OUTLINE_CUTOFF = 120
INK_CUTOFF = 48
COLOUR_CUTOFF = 0.25
COLOUR_SHARE = 0.15
BODY_TOLERANCE = 150.0
HORN_DISTANCE = 0.72
HORN_PIXELS = 2500
REFERENCE_EDGE = 0.895
HORN_TILT = 25.0
HORN_AIM = 25.0
HORN_RADIUS = 0.46
OUTLINE_DEPTH = 0.025


def load_pillow():
    try:
        from PIL import Image
        return Image
    except ImportError:
        pass
    print("Pillow is not installed - trying to install it now...")
    subprocess.run([sys.executable, "-m", "pip", "install", "pillow"], check=False)
    try:
        from PIL import Image
        return Image
    except ImportError:
        sys.exit("Could not import Pillow. Install it with:  pip install pillow")


def find_project(explicit):
    candidates = []
    if explicit:
        candidates.append(pathlib.Path(explicit))
    here = pathlib.Path(__file__).resolve()
    candidates.extend(here.parents)
    candidates.append(pathlib.Path(DEFAULT_PROJECT))

    for candidate in candidates:
        if (candidate / "src" / "main" / "resources" / "assets" / MOD_ID).is_dir():
            return candidate
    sys.exit("Could not find the mod. Run it as:  python gd_faces_spheres.py --project \"C:\\path\\to\\is it verity\"")


def measure(Image, image):
    alpha = image.getchannel("A").point(lambda a: 255 if a > ALPHA_CUTOFF else 0)
    box = alpha.getbbox()
    if box is None:
        sys.exit("An image is fully transparent")

    left, top, right, bottom = box
    cx = (left + right) / 2.0
    cy = (top + bottom) / 2.0
    radius = max(right - left, bottom - top) / 2.0
    return cx, cy, radius


def outline_width(image, cx, cy, radius):
    pixels = image.load()
    width, height = image.size
    runs = []

    for k in range(0, 360, 3):
        angle = math.tau * k / 360.0
        dx = math.cos(angle)
        dy = math.sin(angle)
        rim = None

        for walk in range(0, 200):
            reach = radius * (1.0 - walk * 0.005)
            column = int(cx + reach * dx)
            row = int(cy + reach * dy)
            if 0 <= column < width and 0 <= row < height and pixels[column, row][3] > 200:
                rim = reach
                break

        if rim is None:
            continue

        run = 0

        while run < 200:
            column = int(cx + (rim - run) * dx)
            row = int(cy + (rim - run) * dy)
            if not (0 <= column < width and 0 <= row < height):
                break
            near = pixels[column, row]
            if near[3] < 200 or max(near[0], near[1], near[2]) >= INK_CUTOFF:
                break
            run += 1

        if run:
            runs.append(run)

    if not runs:
        return 0

    runs.sort()
    return runs[len(runs) // 2]


def strip_outline(Image, image, thickness):
    from PIL import ImageChops, ImageFilter

    if thickness < 1:
        return image

    mask = image.getchannel("A").point(lambda a: 255 if a > ALPHA_CUTOFF else 0)

    for _ in range(thickness):
        mask = mask.filter(ImageFilter.MinFilter(3))

    stripped = image.copy()
    stripped.putalpha(ImageChops.multiply(image.getchannel("A"), mask))
    return stripped


def ball_colour(Image, image):
    step = max(1, max(image.width, image.height) // 256)
    if step > 1:
        image = image.resize((image.width // step, image.height // step), Image.NEAREST)

    buckets = {}
    for count, (r, g, b, a) in image.getcolors(image.width * image.height) or []:
        if a < 200:
            continue
        key = (r >> 3, g >> 3, b >> 3)
        total = buckets.get(key)
        if total is None:
            buckets[key] = [count, r * count, g * count, b * count]
        else:
            total[0] += count
            total[1] += r * count
            total[2] += g * count
            total[3] += b * count

    if not buckets:
        return (255, 255, 255)

    total = sum(bucket[0] for bucket in buckets.values())
    colourful = {}

    for key, bucket in buckets.items():
        count = bucket[0]
        saturation, value = colorsys.rgb_to_hsv(bucket[1] / count / 255.0,
                                                bucket[2] / count / 255.0,
                                                bucket[3] / count / 255.0)[1:]
        if saturation >= COLOUR_CUTOFF and value >= COLOUR_CUTOFF:
            colourful[key] = bucket

    if sum(bucket[0] for bucket in colourful.values()) >= total * COLOUR_SHARE:
        pool = colourful
    else:
        pool = {k: v for k, v in buckets.items()
                if v[1] + v[2] + v[3] > OUTLINE_CUTOFF * v[0]} or buckets

    count, r, g, b = max(pool.values(), key=lambda v: v[0])
    return (round(r / count), round(g / count), round(b / count))


def horn_axis(around, tilt):
    around = math.radians(around)
    tilt = math.radians(tilt)
    return (-math.sin(tilt), math.cos(tilt) * math.sin(around), -math.cos(tilt) * math.cos(around))


def block_limit(around, tilt, aim, shell):
    seat = horn_axis(around, tilt)
    point = horn_axis(around, aim)
    room = 9.9

    for part, step in zip(seat, point):
        start = part * shell
        if step > 0.0001:
            room = min(room, (0.49 - start) / step)
        elif step < -0.0001:
            room = min(room, (-0.49 - start) / step)

    return max(0.02, room)


def find_horns(Image, image, cx, cy, radius, thickness):
    from collections import deque
    from PIL import ImageChops, ImageFilter

    red, green, blue, alpha = image.split()
    brightest = ImageChops.lighter(ImageChops.lighter(red, green), blue)
    darkest = ImageChops.darker(ImageChops.darker(red, green), blue)
    pale = ImageChops.multiply(darkest.point(lambda v: 255 if v > 170 else 0),
                               alpha.point(lambda a: 255 if a > 200 else 0))
    pale = ImageChops.multiply(pale, ImageChops.subtract(brightest, darkest).point(lambda v: 0 if v > 40 else 255))

    width, height = image.size
    white = pale.tobytes()
    seen = bytearray(width * height)
    horns = []
    erase = bytearray(width * height)

    for start in range(width * height):
        if not white[start] or seen[start]:
            continue

        seen[start] = 1
        queue = deque([start])
        blob = []

        while queue:
            index = queue.popleft()
            blob.append(index)
            x = index % width

            for step, guard in ((-1, x > 0), (1, x < width - 1), (-width, True), (width, True)):
                near = index + step
                if guard and 0 <= near < width * height and white[near] and not seen[near]:
                    seen[near] = 1
                    queue.append(near)

        if len(blob) < HORN_PIXELS:
            continue

        spots = [(index % width - cx, index // width - cy) for index in blob]
        mid_x = sum(dx for dx, dy in spots) / len(spots)
        mid_y = sum(dy for dx, dy in spots) / len(spots)

        if math.hypot(mid_x, mid_y) / radius < HORN_DISTANCE:
            continue

        near = min(math.hypot(dx, dy) for dx, dy in spots) / radius
        far = max(math.hypot(dx, dy) for dx, dy in spots) / radius
        middle = math.atan2(mid_y, mid_x)
        spread = max(abs((math.atan2(dy, dx) - middle + math.pi) % math.tau - math.pi) for dx, dy in spots)

        around = -math.degrees(middle)
        overhang = (far - near) / 0.95
        shell = 1.0 - OUTLINE_DEPTH / HORN_RADIUS
        length = ((1.0 + overhang) - shell * math.cos(math.radians(HORN_TILT))) \
            / math.cos(math.radians(HORN_AIM)) * HORN_RADIUS

        horns.append({
            "around": round(around, 1),
            "tilt": HORN_TILT,
            "length": round(min(length, block_limit(around, HORN_TILT, HORN_AIM, shell * HORN_RADIUS)), 3),
            "width": round(math.sin(spread) * 0.95 * HORN_RADIUS, 3),
        })

        for index in blob:
            erase[index] = 255

    if not horns:
        return image, []

    grown = Image.frombytes("L", (width, height), bytes(erase))

    for _ in range(max(1, thickness)):
        grown = grown.filter(ImageFilter.MaxFilter(3))

    trimmed = image.copy()
    trimmed.putalpha(ImageChops.subtract(alpha, grown))
    horns.sort(key=lambda horn: horn["around"])
    return trimmed, horns


def body_edge(source, cx, cy, radius):
    pixels = source.load()
    width, height = source.size
    edges = []

    for k in range(0, 360, 2):
        angle = math.tau * k / 360.0
        dx = math.cos(angle)
        dy = math.sin(angle)

        for walk in range(0, 200):
            reach = radius * (1.0 - walk * 0.005)
            column = int(cx + reach * dx)
            row = int(cy + reach * dy)
            if 0 <= column < width and 0 <= row < height and pixels[column, row][3] > 200:
                edges.append(reach / radius)
                break

    if not edges:
        return 1.0

    edges.sort()
    return edges[len(edges) // 2]


def ring_colours(source, cx, cy, radius, colour):
    pixels = source.load()
    width, height = source.size
    steps = 360
    raw = []

    for k in range(steps):
        angle = math.tau * k / steps
        dx = math.cos(angle)
        dy = math.sin(angle)
        found = None

        for walk in range(0, 110):
            reach = radius * (1.0 - walk * 0.005)
            column = int(cx + reach * dx)
            row = int(cy + reach * dy)
            if not (0 <= column < width and 0 <= row < height):
                continue

            near = pixels[column, row]
            if near[3] < 200:
                continue

            gap = ((near[0] - colour[0]) ** 2 + (near[1] - colour[1]) ** 2 + (near[2] - colour[2]) ** 2) ** 0.5
            if gap < BODY_TOLERANCE:
                found = near
                break

        raw.append(found)

    if not any(raw):
        return [colour + (255,)] * steps

    for k in range(steps):
        if raw[k] is None:
            for offset in range(1, steps):
                near = raw[(k - offset) % steps] or raw[(k + offset) % steps]
                if near is not None:
                    raw[k] = near
                    break

    window = 20
    smoothed = []

    for k in range(steps):
        band = [raw[(k + offset) % steps] for offset in range(-window, window + 1)]
        smoothed.append(tuple(sorted(pixel[channel] for pixel in band)[len(band) // 2]
                              for channel in range(3)) + (255,))

    return smoothed


def project(Image, source, cx, cy, radius, colour, size, fill, samples=SUPERSAMPLE):
    side = size * samples
    scale = min(1.0, fill * side / math.pi / radius)

    if scale < 1.0:
        source = source.resize(
            (max(1, round(source.width * scale)), max(1, round(source.height * scale))),
            Image.LANCZOS,
        )
        cx *= scale
        cy *= scale
        radius *= scale

    reach = radius / fill
    ring = ring_colours(source, cx, cy, radius, colour)
    width, height = source.size
    pixels = source.load()
    fallback = colour + (255,)

    face = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    target = face.load()

    cos_phi = []
    sin_phi = []
    for i in range(side):
        phi = 2.0 * math.pi * (1.0 - (i + 0.5) / side)
        cos_phi.append(math.cos(phi))
        sin_phi.append(math.sin(phi))

    for j in range(side):
        theta = math.pi * (j + 0.5) / side
        sin_theta = math.sin(theta)
        dy = -reach * math.cos(theta)
        row = int(cy + dy)
        inside = 0 <= row < height

        for i in range(side):
            dx = -reach * sin_theta * sin_phi[i]
            sample = None

            if inside and sin_theta * cos_phi[i] < 0.0:
                column = int(cx + dx)
                if 0 <= column < width:
                    found = pixels[column, row]
                    if found[3] > ALPHA_CUTOFF:
                        sample = found

            if sample is None and (dx or dy):
                sample = ring[int(math.atan2(dy, dx) / math.tau * 360.0) % 360]

            target[i, j] = sample or fallback

    ball = Image.new("RGBA", (side, side), fallback)
    merged = Image.alpha_composite(ball, face).resize((size, size), Image.LANCZOS)
    return merged.convert("RGB").convert("RGBA")


def face_spread(Image, texture, size=128):
    flat = texture.convert("RGB").resize((size, size), Image.LANCZOS)
    pixels = flat.load()
    seen = []

    for j in range(size):
        theta = math.pi * (j + 0.5) / size
        sin_theta = math.sin(theta)

        for i in range(size):
            phi = math.tau * (1.0 - (i + 0.5) / size)
            x = sin_theta * math.cos(phi)
            if x >= 0.0:
                continue

            r, g, b = pixels[i, j]
            seen.append((0.299 * r + 0.587 * g + 0.114 * b, math.sqrt(max(0.0, 1.0 - x * x))))

    if not seen:
        return 0.0

    seen.sort(key=lambda pair: pair[0])
    ink = sorted(pair[1] for pair in seen[:max(50, len(seen) // 25)])
    return ink[int(len(ink) * 0.92)]


def match_fill(Image, target, source, cx, cy, radius, colour, start):
    wanted = face_spread(Image, target)
    fill = start

    for _ in range(3):
        made = project(Image, source, cx, cy, radius, colour, 128, fill, 2)
        spread = face_spread(Image, made)
        if spread < 0.01:
            break
        fill = min(0.98, max(0.15, fill * wanted / spread))

    return round(fill, 3), wanted


def hull(points):
    points = sorted(set(points))
    if len(points) < 3:
        return points

    def half(order):
        stack = []
        for point in order:
            while len(stack) > 1:
                (ax, ay), (bx, by) = stack[-2], stack[-1]
                if (bx - ax) * (point[1] - ay) - (by - ay) * (point[0] - ax) > 0:
                    break
                stack.pop()
            stack.append(point)
        return stack

    return half(points)[:-1] + half(points[::-1])[:-1]


def render(Image, texture, size, rim, horns):
    from PIL import ImageDraw

    pixels = texture.load()
    width, height = texture.size
    thumb = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    target = thumb.load()
    inner = 1.0 - rim

    for j in range(size):
        sy = 1.0 - 2.0 * (j + 0.5) / size
        for i in range(size):
            sx = 2.0 * (i + 0.5) / size - 1.0
            flat = sx * sx + sy * sy
            if flat > 1.0:
                continue
            if flat > inner * inner:
                target[i, j] = (0, 0, 0, 255)
                continue

            y = sy / inner
            z = -sx / inner
            flat = y * y + z * z
            x = -math.sqrt(max(0.0, 1.0 - flat))

            theta = math.acos(max(-1.0, min(1.0, y)))
            phi = math.atan2(z, x) % (2.0 * math.pi)

            column = min(width - 1, int((1.0 - phi / (2.0 * math.pi)) * width))
            row = min(height - 1, int(theta / math.pi * height))
            shade = 0.45 + 0.55 * math.sqrt(max(0.0, 1.0 - flat))
            r, g, b, a = pixels[column, row]
            target[i, j] = (round(r * shade), round(g * shade), round(b * shade), 255)

    draw = ImageDraw.Draw(thumb)

    for horn in horns:
        seat = horn_axis(horn["around"], horn["tilt"])
        axis = horn_axis(horn["around"], horn.get("aim", horn["tilt"]))
        side = (0.0, axis[2], -axis[1])
        span = math.hypot(side[1], side[2]) or 1.0
        side = (0.0, side[1] / span, side[2] / span)
        up = (side[1] * axis[2] - side[2] * axis[1],
              side[2] * axis[0] - side[0] * axis[2],
              side[0] * axis[1] - side[1] * axis[0])

        girth = horn["width"] / HORN_RADIUS
        root = inner * 0.85
        stretch = horn["length"] / HORN_RADIUS

        def flatten(point):
            return ((-point[2] * 0.5 + 0.5) * size, (0.5 - point[1] * 0.5) * size)

        shape = [flatten([seat[k] * inner + axis[k] * stretch for k in range(3)])]

        for step in range(12):
            angle = math.tau * step / 12
            shape.append(flatten([seat[k] * root + (side[k] * math.cos(angle) + up[k] * math.sin(angle)) * girth
                                  for k in range(3)]))

        draw.polygon(hull(shape), fill=(238, 238, 238, 255), outline=(0, 0, 0, 255),
                     width=max(1, round(rim * size * 0.5)))

    return thumb


def main():
    parser = argparse.ArgumentParser(description="Wrap the GD difficulty faces around the sphere blocks.")
    parser.add_argument("--project", help="path to the mod folder")
    parser.add_argument("--fill", type=float, default=0.62,
                        help="how much of the ball the face covers, 0.62 matches the veritys")
    parser.add_argument("--size", type=int, default=256, help="texture size in pixels (default 256)")
    parser.add_argument("--rim", type=float, default=0.054,
                        help="outline thickness the sphere model draws, only used for the preview")
    parser.add_argument("--keep-outline", action="store_true",
                        help="leave the painted black outline in the texture")
    parser.add_argument("--match", metavar="NAME",
                        help="read the face size out of NAME's current texture, use it for the others "
                             "and leave NAME alone")
    args = parser.parse_args()

    if not 0.1 <= args.fill <= 1.6:
        sys.exit("--fill has to be between 0.1 and 1.6")
    if args.size % 16 or args.size < 16:
        sys.exit("--size has to be a multiple of 16 so mipmaps work")

    Image = load_pillow()
    project_root = find_project(args.project)
    textures = project_root / "src" / "main" / "resources" / "assets" / MOD_ID / "textures" / "block" / "gd-faces"
    originals = project_root / ".gdfaces-src"
    originals.mkdir(parents=True, exist_ok=True)

    print(f"mod       {project_root}")

    fill = args.fill

    if args.match:
        if args.match not in NAMES:
            sys.exit(f"--match has to be one of: {', '.join(NAMES)}")

        guide = textures / f"{args.match}.png"
        if not guide.exists():
            sys.exit(f"Missing texture: {guide}")

        kept = originals / f"{args.match}.png"
        if not kept.exists():
            shutil.copy2(guide, kept)

        art = Image.open(kept).convert("RGBA")
        cx, cy, radius = measure(Image, art)
        colour = ball_colour(Image, art)
        thickness = outline_width(art, cx, cy, radius)
        art = strip_outline(Image, art, thickness)
        art = find_horns(Image, art, cx, cy, radius, thickness)[0]

        fill, spread = match_fill(Image, Image.open(guide).convert("RGBA"), art, cx, cy, radius, colour,
                                  args.fill)
        print(f"matched   {args.match}.png covers {spread:.2f} of its ball -> face fill {fill:.3f}")

    print(f"face fill {fill:.2f} of the ball, face edge at {min(0.95, fill * REFERENCE_EDGE):.2f}\n")

    thumbs = []
    horns = {}
    redrawn = 0

    for name in NAMES:
        live = textures / f"{name}.png"
        kept = originals / f"{name}.png"

        if not kept.exists():
            if not live.exists():
                sys.exit(f"Missing texture: {live}")
            shutil.copy2(live, kept)

        source = Image.open(kept).convert("RGBA")
        cx, cy, radius = measure(Image, source)
        colour = ball_colour(Image, source)
        thickness = outline_width(source, cx, cy, radius)

        if not args.keep_outline:
            source = strip_outline(Image, source, thickness)

        source, spikes = find_horns(Image, source, cx, cy, radius, thickness)

        if spikes:
            horns[name] = spikes

        wrap = fill

        if spikes:
            wrap = min(1.6, fill * REFERENCE_EDGE / max(0.2, body_edge(source, cx, cy, radius)))

        if name == args.match:
            texture = Image.open(live).convert("RGBA")
            print(f"  {name:14s} left alone, everything else matches it")
        else:
            texture = project(Image, source, cx, cy, radius, colour, args.size, wrap)
            texture.save(live)
            redrawn += 1
            print(f"  {name:14s} {source.width}x{source.height}  fill {wrap:.2f}  "
                  f"ball #{colour[0]:02x}{colour[1]:02x}{colour[2]:02x}  horns {len(spikes)}")

        thumbs.append(render(Image, texture, 160, args.rim, spikes))

    columns = 4
    rows = (len(thumbs) + columns - 1) // columns
    sheet = Image.new("RGBA", (columns * 160, rows * 160), (32, 32, 36, 255))
    for index, thumb in enumerate(thumbs):
        sheet.alpha_composite(thumb, ((index % columns) * 160, (index // columns) * 160))

    preview = project_root / "gd_faces_preview.png"
    sheet.save(preview)

    spec = project_root / "gd_horns.json"
    spec.write_text(json.dumps(horns, indent=2) + "\n", encoding="utf-8")

    print(f"\n{redrawn} faces re-wrapped. Preview of the finished balls: {preview}")
    print("Originals kept in .gdfaces-src - re-run with --fill to resize, delete it when happy.")


if __name__ == "__main__":
    main()
