import os
import random
try:
    from PIL import Image, ImageDraw
except ImportError:
    import subprocess
    import sys
    subprocess.check_call([sys.executable, "-m", "pip", "install", "Pillow"])
    from PIL import Image, ImageDraw

def generate_logo():
    # 初始化一个 64x64 的全透明图像
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    # 定义 Minecraft 原版像素风格的字体（1代表有像素，0代表空白）
    font_data = {
        'B': ["11110", "10001", "10001", "11110", "10001", "10001", "11110"],
        'l': ["1", "1", "1", "1", "1", "1", "1"],
        'o': ["00000", "00000", "01110", "10001", "10001", "10001", "01110"],
        'd': ["00001", "00001", "01111", "10001", "10001", "10001", "01111"],
        'c': ["00000", "00000", "01110", "10000", "10000", "10000", "01110"],
        'r': ["0000", "0000", "1011", "1100", "1000", "1000", "1000"],
        'a': ["00000", "00000", "01110", "00001", "01111", "10001", "01111"],
        'f': ["0011", "0100", "1110", "0100", "0100", "0100", "0100"],
        't': ["010", "010", "111", "010", "010", "010", "001"]
    }

    word = "Bloodcraft"
    
    # 计算文本总宽度
    total_width = 0
    for char in word:
        total_width += len(font_data[char][0]) + 1 # 加上字间距
    total_width -= 1 # 移除最后一个多余的空格

    # 居中计算
    start_x = (64 - total_width) // 2
    start_y = (64 - 7) // 2

    # 创建一个 64x64 的文字遮罩网格
    mask = [[0]*64 for _ in range(64)]

    curr_x = start_x
    for char in word:
        char_grid = font_data[char]
        for y, row in enumerate(char_grid):
            for x, val in enumerate(row):
                if val == '1':
                    mask[start_y + y][curr_x + x] = 1
        curr_x += len(char_grid[0]) + 1

    pixels = img.load()
    
    # 第一遍：绘制细黑描边（8方向检测）
    for y in range(64):
        for x in range(64):
            if mask[y][x] == 0:
                is_outline = False
                for dy in [-1, 0, 1]:
                    for dx in [-1, 0, 1]:
                        if 0 <= y+dy < 64 and 0 <= x+dx < 64:
                            if mask[y+dy][x+dx] == 1:
                                is_outline = True
                                break
                    if is_outline: break
                
                if is_outline:
                    # 改为极深的偏绿黑色作为描边
                    pixels[x, y] = (10, 15, 10, 255) 

    # 第二遍：绘制带有深浅混合与渐变的绿色字体
    for y in range(64):
        for x in range(64):
            if mask[y][x] == 1:
                # 根据 Y 轴高度计算渐变
                rel_y = y - start_y
                ratio = rel_y / 6.0
                
                # 顶部是鲜绿色，底部是暗绿色
                # 将原本红色的数值分配给绿色通道，红色和蓝色通道保持低值
                r_base = int(40 * (1 - ratio) + 0 * ratio)
                g_base = int(220 * (1 - ratio) + 100 * ratio)
                b_base = int(40 * (1 - ratio) + 0 * ratio)
                
                # 添加随机噪点（深浅混合效果）
                noise = random.randint(-25, 25)
                r = max(0, min(255, r_base + noise))
                g = max(0, min(255, g_base + noise))
                b = max(0, min(255, b_base + noise))
                
                # 10% 概率生成非常亮的亮绿高光
                if random.random() < 0.1:
                    r = min(255, r + 20)
                    g = min(255, g + 40)
                    b = min(255, b + 20)
                    
                pixels[x, y] = (r, g, b, 255)

    output_path = "bloodcraft_logo_green.png"
    img.save(output_path)
    print(f"Successfully generated {output_path}")

if __name__ == "__main__":
    generate_logo()