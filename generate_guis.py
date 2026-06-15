import os
import random
from PIL import Image, ImageDraw, ImageFont

def draw_meat_background(draw, x0, y0, width, height):
    for x in range(x0, x0 + width):
        for y in range(y0, y0 + height):
            r = random.randint(120, 180)
            g = random.randint(20, 50)
            b = random.randint(20, 50)
            if y % 4 == 0: r = min(255, r + 20)
            draw.point((x, y), fill=(r, g, b, 255))
            
    for _ in range(width * height // 400):
        start_x = random.randint(x0, x0 + width)
        start_y = random.randint(y0, y0 + height)
        cx, cy = start_x, start_y
        for _ in range(random.randint(5, 15)):
            nx = cx + random.randint(-8, 8)
            ny = cy + random.randint(-8, 8)
            draw.line([(cx, cy), (nx, ny)], fill=(220, 220, 200, 255), width=random.randint(2, 4))
            cx, cy = nx, ny

def draw_slot(draw, x, y, size=18):
    draw.rectangle([x, y, x+size-1, y+size-1], fill=(100, 20, 20, 255))
    draw.line([(x, y), (x+size-1, y)], fill=(60, 10, 10, 255), width=1)
    draw.line([(x, y), (x, y+size-1)], fill=(60, 10, 10, 255), width=1)
    draw.line([(x+size-1, y), (x+size-1, y+size-1)], fill=(200, 50, 50, 255), width=1)
    draw.line([(x, y+size-1), (x+size-1, y+size-1)], fill=(200, 50, 50, 255), width=1)

def draw_bone_arrow(draw, x, y):
    draw.polygon([(x, y+6), (x+14, y+6), (x+14, y+2), (x+22, y+8), (x+14, y+14), (x+14, y+10), (x, y+10)], fill=(230, 230, 220, 255), outline=(100, 100, 90, 255))

def draw_skull_icon(draw, x, y):
    # 画一个小头骨
    draw.rectangle([x+3, y+2, x+11, y+8], fill=(220, 220, 220, 255)) # 头顶
    draw.rectangle([x+5, y+8, x+9, y+12], fill=(220, 220, 220, 255)) # 下巴
    # 眼睛
    draw.rectangle([x+4, y+5, x+6, y+7], fill=(40, 40, 40, 255))
    draw.rectangle([x+8, y+5, x+10, y+7], fill=(40, 40, 40, 255))
    # 鼻子
    draw.point((x+7, y+8), fill=(40, 40, 40, 255))
    # 牙齿缝隙
    draw.line([(x+6, y+10), (x+6, y+12)], fill=(40, 40, 40, 255), width=1)
    draw.line([(x+8, y+10), (x+8, y+12)], fill=(40, 40, 40, 255), width=1)

def generate_altar_gui():
    img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw_meat_background(draw, 0, 0, 176, 166)
    draw.rectangle([0, 0, 175, 165], outline=(50, 10, 10, 255), width=2)
    
    for row in range(3):
        for col in range(9):
            draw_slot(draw, 7 + col * 18, 83 + row * 18)
    for col in range(9):
        draw_slot(draw, 7 + col * 18, 141)
        
    draw_slot(draw, 55, 16)
    draw_slot(draw, 55, 52)
    
    draw_slot(draw, 115, 34)
    
    draw_bone_arrow(draw, 79, 34)
    draw_skull_icon(draw, 56, 36)
    
    path = "g:/myfirstmod/bloodcraft/src/main/resources/assets/blood/textures/gui/container/blood_tempering_altar.png"
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)

def generate_creative_tab_gui():
    img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw_meat_background(draw, 0, 0, 195, 136)
    draw.rectangle([0, 0, 194, 135], outline=(50, 10, 10, 255), width=2)
    
    # We shouldn't draw slots for creative tab because the game draws them itself.
    # Actually wait, creative tab `tab_items.png` draws the slots on the background.
    # We should draw them.
    for row in range(5):
        for col in range(9):
            draw_slot(draw, 8 + col * 18, 17 + row * 18)
            
    # 绘制玩家的快捷物品栏
    for col in range(9):
        draw_slot(draw, 8 + col * 18, 111)
            
    draw.rectangle([174, 17, 174+13, 17+111], fill=(80, 15, 15, 255), outline=(40, 5, 5, 255))
    
    path = "g:/myfirstmod/bloodcraft/src/main/resources/assets/minecraft/textures/gui/container/creative_inventory/tab_bloodcraft.png"
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)

if __name__ == "__main__":
    generate_altar_gui()
    generate_creative_tab_gui()
