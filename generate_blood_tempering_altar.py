import os
from PIL import Image, ImageDraw
import random
import math

def draw_base(size):
    img = Image.new('RGBA', (size, size), (0, 0, 0, 255))
    draw = ImageDraw.Draw(img)
    # 1. 基础极度风化石头背景
    for x in range(size):
        for y in range(size):
            base_val = random.randint(50, 90)
            img.putpixel((x, y), (base_val, base_val, base_val, 255))
    
    # 2. 石头裂痕
    for _ in range(15):
        start_x, start_y = random.randint(0, size), random.randint(0, size)
        cur_x, cur_y = start_x, start_y
        for _ in range(random.randint(2, 5)):
            next_x = cur_x + random.randint(-4, 4)
            next_y = cur_y + random.randint(-4, 4)
            draw.line([(cur_x, cur_y), (next_x, next_y)], fill=(20, 20, 20, 255), width=1)
            cur_x, cur_y = next_x, next_y
            
    # 3. 边缘的纯血肉 (用于模型四个角落的支柱)
    edge_width = size // 8
    for x in range(edge_width + 1):
        for y in range(size):
            r = random.randint(120, 220)
            g = random.randint(10, 40)
            b = random.randint(10, 40)
            if y % 4 == 0:
                r = min(255, r + 30)
            img.putpixel((x, y), (r, g, b, 255))
            
    return img, draw

def generate_textures(size=32):
    save_dir = 'g:/myfirstmod/bloodcraft/src/main/resources/assets/blood/textures/blocks/'
    os.makedirs(save_dir, exist_ok=True)

    # ==========================================
    # 第一张贴图：Side (侧面/底面/柱子使用，无眼珠)
    # ==========================================
    img_side, draw_side = draw_base(size)
    
    # 画一些普通的血管在石头上
    for _ in range(25):
        start_x, start_y = random.randint(0, size), random.randint(0, size)
        cur_x, cur_y = start_x, start_y
        for _ in range(random.randint(3, 8)):
            next_x = cur_x + random.randint(-5, 5)
            next_y = cur_y + random.randint(-5, 5)
            draw_side.line([(cur_x, cur_y), (next_x, next_y)], fill=(130, 20, 20, 255), width=random.randint(1, 2))
            cur_x, cur_y = next_x, next_y
            
    img_side.save(os.path.join(save_dir, 'blood_tempering_altar_side.png'))

    # ==========================================
    # 第二张贴图：Top (仅顶部台面使用，有眼珠)
    # ==========================================
    img_top, draw_top = draw_base(size)
    cx, cy = size // 2, size // 2
    eye_radius = size // 4
    
    # 眼白
    draw_top.ellipse([(cx - eye_radius, cy - eye_radius), (cx + eye_radius, cy + eye_radius)], 
                 fill=(220, 210, 210, 255), outline=(120, 20, 20, 255))
    # 虹膜 (血红色)
    draw_top.ellipse([(cx - eye_radius//2, cy - eye_radius//2), (cx + eye_radius//2, cy + eye_radius//2)], 
                 fill=(160, 0, 0, 255))
    # 瞳孔 (黑色睁大)
    draw_top.ellipse([(cx - eye_radius//4, cy - eye_radius//4), (cx + eye_radius//4, cy + eye_radius//4)], 
                 fill=(10, 0, 0, 255))
                 
    # 眼白上的红血丝
    for _ in range(16):
        angle = random.uniform(0, 2 * math.pi)
        r1 = eye_radius // 2 + 1
        r2 = eye_radius - 1
        draw_top.line([(cx + math.cos(angle)*r1, cy + math.sin(angle)*r1), 
                   (cx + math.cos(angle)*r2, cy + math.sin(angle)*r2)], 
                  fill=(200, 30, 30, 255), width=1)

    # 血管包裹石头：从眼珠向外蔓延
    for _ in range(40):
        angle = random.uniform(0, 2 * math.pi)
        r1 = eye_radius
        cur_x, cur_y = cx + math.cos(angle)*r1, cy + math.sin(angle)*r1
        steps = random.randint(4, 12)
        thickness = random.randint(2, 3)
        for step in range(steps):
            next_x = cur_x + math.cos(angle) * random.uniform(2, 5) + random.uniform(-1.5, 1.5)
            next_y = cur_y + math.sin(angle) * random.uniform(2, 5) + random.uniform(-1.5, 1.5)
            color = (max(50, 180 - step*15), 10, 10, 255)
            draw_top.line([(cur_x, cur_y), (next_x, next_y)], fill=color, width=thickness)
            cur_x, cur_y = next_x, next_y
            if random.random() > 0.6 and thickness > 1:
                thickness -= 1
                
    img_top.save(os.path.join(save_dir, 'blood_tempering_altar_top.png'))
    print("Top and Side textures generated successfully.")

if __name__ == "__main__":
    generate_textures(32)
