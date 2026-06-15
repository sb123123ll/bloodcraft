import sys
import os
from PIL import Image

def generate_animated_eye(input_path, output_path):
    # 打开原始图片
    original_img = Image.open(input_path).convert("RGBA")
    width, height = original_img.size
    
    # 我们打算制作 4 帧的动画
    # 帧0：原始状态
    # 帧1：眼球微微鼓起（向外膨胀），神经轻微扭动
    # 帧2：眼球最大程度鼓起，瞳孔变大，神经血管变粗/变红
    # 帧3：回落到微鼓状态（平滑过渡回原始状态）
    frames = []
    
    # 帧0: 原图
    frames.append(original_img.copy())
    
    # 简单实现：通过缩放和裁剪来模拟"鼓起"的效果
    # 我们把图片分为两部分：眼球主体（左下）和神经血管（右上）
    
    # 帧1: 微鼓 (放大 5%)
    frame1 = Image.new("RGBA", (width, height), (0,0,0,0))
    # 稍微放大图片
    scaled1 = original_img.resize((int(width * 1.05), int(height * 1.05)), Image.NEAREST)
    # 计算居中裁剪的坐标
    offset_x1 = (scaled1.width - width) // 2
    offset_y1 = (scaled1.height - height) // 2
    # 将放大后的图片贴回 16x16 画布（居中）
    frame1.paste(scaled1.crop((offset_x1, offset_y1, offset_x1 + width, offset_y1 + height)), (0,0))
    frames.append(frame1)
    
    # 帧2: 极度鼓胀 (放大 10%)，并且让红色的像素变得更鲜艳
    frame2 = Image.new("RGBA", (width, height), (0,0,0,0))
    scaled2 = original_img.resize((int(width * 1.10), int(height * 1.10)), Image.NEAREST)
    offset_x2 = (scaled2.width - width) // 2
    offset_y2 = (scaled2.height - height) // 2
    cropped2 = scaled2.crop((offset_x2, offset_y2, offset_x2 + width, offset_y2 + height))
    
    # 增强红色通道，让它看起来充血
    pixels = cropped2.load()
    for y in range(cropped2.height):
        for x in range(cropped2.width):
            r, g, b, a = pixels[x, y]
            if a > 0:
                # 增强红色，稍微降低绿蓝，使血液感更强
                nr = min(255, int(r * 1.2))
                ng = int(g * 0.9)
                nb = int(b * 0.9)
                pixels[x, y] = (nr, ng, nb, a)
                
    frame2.paste(cropped2, (0,0))
    frames.append(frame2)
    
    # 帧3: 回落 (使用帧1)
    frames.append(frame1.copy())
    
    # 创建长条图 (sprite sheet)
    sprite_sheet = Image.new("RGBA", (width, height * len(frames)), (0,0,0,0))
    for i, frame in enumerate(frames):
        sprite_sheet.paste(frame, (0, i * height))
        
    sprite_sheet.save(output_path)
    print(f"Successfully created animated sprite sheet: {output_path}")

if __name__ == "__main__":
    base_dir = r"g:\myfirstmod\bloodcraft\src\main\resources\assets\blood\textures\items"
    input_file = os.path.join(base_dir, "plucked_eye.png")
    output_file = os.path.join(base_dir, "plucked_eye.png") # 覆盖原文件
    
    try:
        generate_animated_eye(input_file, output_file)
    except Exception as e:
        print(f"Error: {e}")
