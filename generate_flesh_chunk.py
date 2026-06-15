#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成肉块(Flesh Chunk)方块贴图 - 更血腥版本
与渝血者肉块风格一致但更血腥糜烂
"""

from PIL import Image, ImageDraw, ImageFilter
import random
import math

def generate_flesh_chunk_texture():
    """生成16x16的肉块贴图 - 简化版本"""
    size = 16
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    
    # 基础血肉色调 - 深红色系
    base_colors = [
        (139, 0, 0),      # 深红
        (128, 0, 0),      # 暗红
        (120, 20, 20),    # 深褐红
        (110, 30, 30),    # 暗血红
        (100, 25, 25),    # 更深的红
    ]
    
    # 填充基础背景
    for y in range(size):
        for x in range(size):
            color = random.choice(base_colors)
            
            # 添加轻微的颜色变化
            variation = random.randint(-15, 15)
            r = max(0, min(255, color[0] + variation))
            g = max(0, min(255, color[1] + variation))
            b = max(0, min(255, color[2] + variation))
            
            img.putpixel((x, y), (r, g, b, 255))
    
    # 添加血管/纤维纹理
    for _ in range(8):
        start_x = random.randint(0, size-1)
        start_y = random.randint(0, size-1)
        length = random.randint(3, 8)
        
        # 深红色血管
        vein_color = (80, 0, 0, 255)
        
        x, y = start_x, start_y
        for i in range(length):
            if 0 <= x < size and 0 <= y < size:
                img.putpixel((x, y), vein_color)
                # 稍微加粗
                for dx in [-1, 0, 1]:
                    for dy in [-1, 0, 1]:
                        nx, ny = x + dx, y + dy
                        if 0 <= nx < size and 0 <= ny < size and random.random() < 0.5:
                            img.putpixel((nx, ny), vein_color)
            
            x += random.choice([-1, 0, 1])
            y += random.choice([-1, 0, 1])
    
    # 添加一些深色斑点
    for _ in range(5):
        px = random.randint(0, size-1)
        py = random.randint(0, size-1)
        hole_color = (50, 20, 20, 255)
        img.putpixel((px, py), hole_color)
        for dx in [-1, 0, 1]:
            for dy in [-1, 0, 1]:
                nx, ny = px + dx, py + dy
                if 0 <= nx < size and 0 <= ny < size and random.random() < 0.4:
                    img.putpixel((nx, ny), hole_color)
    
    # 轻微模糊使纹理更自然
    img = img.filter(ImageFilter.GaussianBlur(radius=0.3))
    
    return img

if __name__ == "__main__":
    import os
    texture = generate_flesh_chunk_texture()
    output_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), 
                              "src", "main", "resources", "assets", "blood", "textures", "blocks")
    os.makedirs(output_dir, exist_ok=True)
    output_path = os.path.join(output_dir, "flesh_chunk.png")
    texture.save(output_path)
    print(f"肉块贴图已生成: {output_path}")
