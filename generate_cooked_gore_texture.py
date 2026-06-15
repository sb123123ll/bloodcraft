import struct
import zlib
import os
import random

def write_png(filename, width, height, pixels):
    def make_chunk(ctype, data):
        return struct.pack('>I', len(data)) + ctype + data + struct.pack('>I', zlib.crc32(ctype + data) & 0xffffffff)
        
    with open(filename, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n')
        # IHDR
        ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
        f.write(make_chunk(b'IHDR', ihdr_data))
        
        # IDAT
        raw_data = bytearray()
        for row in pixels:
            raw_data.append(0) # filter type 0
            raw_data.extend(row)
        compressed = zlib.compress(raw_data)
        f.write(make_chunk(b'IDAT', compressed))
        
        # IEND
        f.write(make_chunk(b'IEND', b''))

def generate_cooked_gore_texture():
    width, height = 16, 16
    pixels = []
    
    # 烤熟肉的调色板
    TRANS = [0, 0, 0, 0]
    MEAT_DARK = [45, 25, 15, 255]      # 烤焦的深褐色边缘
    MEAT_MID = [100, 55, 35, 255]      # 烤熟的主体熟肉色
    MEAT_BRIGHT = [140, 85, 55, 255]   # 熟肉的浅色纹理
    FAT_COOKED = [200, 160, 100, 255]  # 烤熟后的脂肪，颜色更金黄
    BONE_SHADOW = [120, 110, 100, 255] # 骨头的暗部稍微变深变黄一点点
    BONE_BASE = [190, 180, 170, 255]   # 骨头的基本色，被熏烤过稍微暗淡
    BONE_HIGH = [220, 215, 205, 255]   # 骨头的高光
    OUTLINE = [15, 10, 5, 255]         # 烤熟后的暗色边缘轮廓更偏黑褐

    # 手绘 16x16 的像素图 (和生烂肉保持一致，但应用不同的颜色)
    ascii_art = [
        "                ",
        "                ",
        "          O     ",
        "         OS O   ",
        "  OdO   OHS  O  ",
        " OmdmO OBS OdO  ",
        "OmmbbdOdBS  OmddO",
        "OddbmmOdbOdOdmdO",
        " OdmbbmdSmbddmbO",
        "OdfmdmbbmmfbbmmdO",
        "OdfdfmddmmfmbdOO",
        "OdmfmmmmbdmbdfbO",
        " ObmddfmbmddmbO ",
        " OOmbdsmfbbmdO  ",
        "  OmmdmbdffmO   ",
        "   OOddmmdOO    "
    ]
    
    char_to_color = {
        ' ': TRANS,
        'O': OUTLINE,
        'd': MEAT_DARK,
        'm': MEAT_MID,
        'b': MEAT_BRIGHT,
        'f': FAT_COOKED,
        'S': BONE_SHADOW,
        'B': BONE_BASE,
        'H': BONE_HIGH,
        's': BONE_BASE # 碎骨头
    }
    
    for y in range(height):
        row = bytearray(width * 4)
        line = ascii_art[y]
        for x in range(width):
            char = line[x]
            color = list(char_to_color[char])
            
            # 加入一点噪点随机性，让烤肉看起来更有质感
            if char in ['d', 'm', 'b', 'f']:
                noise = random.randint(-10, 10)
                color[0] = max(0, min(255, color[0] + noise))
                color[1] = max(0, min(255, color[1] + noise))
                color[2] = max(0, min(255, color[2] + noise))
                
            idx = x * 4
            row[idx:idx+4] = color
        pixels.append(row)
        
    return width, height, pixels

if __name__ == '__main__':
    base_dir = r"g:\myfirstmod\bloodcraft\src\main\resources\assets\blood\textures\items"
    if not os.path.exists(base_dir):
        os.makedirs(base_dir)
        
    output_file = os.path.join(base_dir, "cooked_gory_flesh.png")
    
    w, h, pixels = generate_cooked_gore_texture()
    write_png(output_file, w, h, pixels)
    print(f"Texture saved to {output_file}")