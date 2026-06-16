import os
import random
import math
from PIL import Image, ImageDraw

def blend(c1, c2, alpha):
    return (
        int(c1[0] * (1 - alpha) + c2[0] * alpha),
        int(c1[1] * (1 - alpha) + c2[1] * alpha),
        int(c1[2] * (1 - alpha) + c2[2] * alpha),
        255
    )

def apply_noise(img):
    pixels = img.load()
    w, h = img.size
    for x in range(w):
        for y in range(h):
            if pixels[x, y][3] > 0:
                if random.random() < 0.35:
                    is_fresh = random.random() < 0.4
                    if is_fresh:
                        color = (random.randint(150, 220), 0, 0, 255)
                    else:
                        color = (random.randint(40, 70), random.randint(0, 10), random.randint(0, 10), 255)
                    pixels[x, y] = blend(pixels[x, y], color, random.uniform(0.4, 0.8))

def draw_ribs(img, x_start, x_end, y_start, y_end):
    pixels = img.load()
    for y in range(y_start, y_end):
        for x in range(x_start, x_end):
            # alternating bone and meat
            if (y - y_start) % 3 == 1:
                # bone
                color = (200, 190, 180, 255)
            else:
                # rotting meat
                color = (80, 10, 10, 255)
            pixels[x, y] = blend(pixels[x, y], color, 0.95)

def draw_guts(img, x_start, x_end, y_start, y_end):
    pixels = img.load()
    for y in range(y_start, y_end):
        for x in range(x_start, x_end):
            # noisy guts pattern
            nx = math.sin(x * 1.8) + math.cos(y * 1.8)
            if nx > 0.5:
                color = (220, 70, 70, 255) # highlight
            elif nx < -0.5:
                color = (70, 10, 10, 255) # shadow
            else:
                color = (150, 30, 30, 255) # mid
            pixels[x, y] = blend(pixels[x, y], color, 0.95)

def draw_ulcer(img, x, y, size):
    pixels = img.load()
    for dy in range(size):
        for dx in range(size):
            px, py = x + dx, y + dy
            if 0 <= px < img.width and 0 <= py < img.height:
                if dx == size//2 and dy == size//2:
                    c = (170, 180, 40, 255) # pus
                else:
                    c = (120, 10, 10, 255) # blood
                pixels[px, py] = blend(pixels[px, py], c, 0.85)
            # drip
            if dy == size - 1 and dx == size//2:
                for drip in range(1, random.randint(3, 5)):
                    if py + drip < img.height:
                        pixels[px, py + drip] = blend(pixels[px, py + drip], (150, 160, 40, 255), 0.75)

def main():
    img = Image.open("base_wolf.png").convert("RGBA")
    
    # 1. 全身沾满血液
    apply_noise(img)
    
    pixels = img.load()
    
    # 2. 背部纹理（Mane top & Body top）长条撕裂伤口，露出肋骨
    # Mane top: x=28..35, y=0..6
    draw_ribs(img, 29, 34, 1, 6)
    # Body top: x=24..29, y=20..28
    draw_ribs(img, 25, 28, 21, 27)
    
    # 3. 身体侧面溃烂伤与脓液
    # Body right: x=18..23, y=20..28
    draw_ulcer(img, 20, 22, 3)
    draw_ulcer(img, 19, 25, 2)
    # Body left: x=30..35, y=20..28
    draw_ulcer(img, 32, 21, 3)
    draw_ulcer(img, 31, 26, 2)
    
    # 4. 尾巴中间部分腐烂，露出血肉和白骨
    # Tail right: x=9..10, front: 11..12, left: 13..14, back: 15..16. y=20..27
    for y in range(23, 26):
        for x in range(9, 17):
            if x % 2 == 0:
                pixels[x, y] = (220, 210, 200, 255) # bone
            else:
                pixels[x, y] = (80, 10, 10, 255) # meat
                
    # 5. 头部：耳朵全是血，左耳朵尖部白骨
    # Ear UVs: x=16..19, y=14..17
    for y in range(14, 18):
        for x in range(16, 20):
            pixels[x, y] = blend(pixels[x, y], (150, 0, 0, 255), 0.8) # blood
    # Ear tip bone
    pixels[16, 14] = (220, 210, 200, 255)
    pixels[17, 14] = (220, 210, 200, 255)
    
    # 头部眼部暗红色，向下巴流血
    # Front face: x=4..9, y=4..9
    pixels[4, 6] = (60, 0, 0, 255)
    pixels[5, 6] = (60, 0, 0, 255)
    pixels[8, 6] = (60, 0, 0, 255)
    pixels[9, 6] = (60, 0, 0, 255)
    # Tear/blood streaks
    for y in range(7, 10):
        pixels[4, y] = blend(pixels[4, y], (140, 0, 0, 255), 0.85)
        pixels[9, y] = blend(pixels[9, y], (140, 0, 0, 255), 0.85)
        
    # 嘴巴渗血，上嘴唇缺一小部分露牙齿
    # Snout front: x=4..6, y=14..16
    for y in range(14, 17):
        for x in range(4, 7):
            pixels[x, y] = blend(pixels[x, y], (160, 0, 0, 255), 0.7)
    # Missing lip / teeth
    pixels[5, 15] = (230, 230, 220, 255) # tooth
    pixels[6, 15] = (230, 230, 220, 255) # tooth
    pixels[4, 15] = (90, 0, 0, 255) # dark meat
    
    # 6. 腿部：四个腿都是下面有血，腿根正常，最下面干涸暗红，中间鲜血
    # Legs: x=0..7, y=20..27
    for y in range(20, 28):
        for x in range(0, 8):
            if 23 <= y <= 25:
                # Fresh blood
                pixels[x, y] = blend(pixels[x, y], (200, 0, 0, 255), 0.85)
            elif y > 25:
                # Dried blood
                pixels[x, y] = blend(pixels[x, y], (50, 5, 5, 255), 0.95)
                
    # 7. 腹部：开放性长条撕裂伤，脑花一样的肠子
    # Body bottom: x=36..41, y=20..28
    draw_guts(img, 37, 41, 21, 28)
    
    # Save
    out_dir = r"g:\myfirstmod\bloodcraft\src\main\resources\assets\blood\textures\entity"
    os.makedirs(out_dir, exist_ok=True)
    img.save(os.path.join(out_dir, "blood_hound.png"))
    print("Texture generated successfully at " + os.path.join(out_dir, "blood_hound.png"))

if __name__ == "__main__":
    main()
