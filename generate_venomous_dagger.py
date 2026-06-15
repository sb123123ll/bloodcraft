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

def generate_venomous_dagger_texture():
    width, height = 16, 16
    pixels = []
    
    # 调色板 - 根据示例图片调整
    TRANS = [0, 0, 0, 0]
    BLOOD_DARK = [100, 20, 30, 255]      # 暗血
    BLOOD_MID = [200, 40, 50, 255]      # 中等血色
    BLOOD_BRIGHT = [240, 60, 70, 255]   # 鲜血
    VENOM_GREEN = [60, 180, 80, 255]    # 腥绿色毒液
    VENOM_BRIGHT = [100, 220, 120, 255] # 亮绿色毒液
    BLADE_SILVER = [200, 200, 210, 255] # 银白色剑刃
    BLADE_GRAY = [160, 165, 175, 255]   # 剑刃灰色
    BLADE_SHINE = [240, 240, 250, 255]  # 剑刃高光
    HANDLE_GOLD = [200, 150, 80, 255]   # 金棕色手柄
    HANDLE_DARK = [140, 100, 50, 255]    # 手柄暗部
    OUTLINE = [50, 40, 40, 255]         # 轮廓

    # 手绘 16x16 的像素图 - 匕首形状（根据示例图调整）
    # 剑刃向上，握把向下
    ascii_art = [
        "      SSS       ",  # 剑尖
        "     SBBBS      ",
        "    SBGBBS      ",  # 剑刃有绿色毒液
        "   SBGBBBS      ",
        "   SBBBBS       ",
        "   SBBBS        ",
        "  SBBBS         ",
        "  SBBB          ",
        "  SBBH          ",  # 剑刃根部有血
        "  BBBH          ",
        "  BBHGG         ",  # 血与绿色毒液
        "  BGGGG         ",
        "  GGGGG         ",  # 绿色毒液
        " HHHHHH         ",  # 握把处有血
        " HHHHH          ",
        "  HHH           "   # 握把底部
    ]
    
    char_to_color = {
        ' ': TRANS,
        'S': BLADE_SHINE,
        'B': BLADE_GRAY,
        'G': VENOM_GREEN,
        'V': VENOM_BRIGHT,
        'H': BLOOD_MID,
        'h': BLOOD_BRIGHT,
        'd': BLOOD_DARK,
        'W': HANDLE_GOLD,
        'D': HANDLE_DARK,
        'O': OUTLINE
    }
    
    for y in range(height):
        row = bytearray(width * 4)
        line = ascii_art[y]
        for x in range(width):
            char = line[x] if x < len(line) else ' '
            color = list(char_to_color.get(char, TRANS))
            
            # 加入噪点，让材质更有质感
            if char in ['B', 'G', 'V', 'K', 'k', 'D', 'H', 'h', 'd']:
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
        
    output_file = os.path.join(base_dir, "venomous_stinger_dagger.png")
    
    w, h, pixels = generate_venomous_dagger_texture()
    write_png(output_file, w, h, pixels)
    print(f"Texture saved to {output_file}")
