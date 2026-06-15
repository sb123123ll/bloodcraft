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

def generate_gore_texture():
    width, height = 16, 16
    pixels = []
    
    # 调色板微调
    TRANS = [0, 0, 0, 0]
    BLOOD_DARK = [55, 5, 10, 255]      # 凝固的紫黑血块 (稍微加深，带点污浊感)
    BLOOD_MID = [150, 15, 20, 255]     # 烂肉主体颜色 (稍微降低一点点纯度，让它更像真实的肉而不是涂料)
    BLOOD_BRIGHT = [210, 20, 25, 255]  # 鲜血/肌肉高光 (增加层次，不那么扎眼)
    FAT_YELLOW = [185, 140, 90, 255]   # 脂肪的黄偏一点点粉肉色 (更柔和一点的腐黄)
    BONE_SHADOW = [140, 130, 120, 255] # 骨头的暗部
    BONE_BASE = [210, 200, 190, 255]   # 骨头的基本色
    BONE_HIGH = [245, 240, 230, 255]   # 骨头的高光
    OUTLINE = [20, 0, 0, 255]          # 肉块的暗色边缘轮廓

    # 手绘 16x16 的像素图 (以左上角为 0,0)
    # 微调形状：骨头上端大幅缩短，不再像一根长长的吸管，而是像一截断裂的短骨茬，下端完全被血肉包裹
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
        'd': BLOOD_DARK,
        'm': BLOOD_MID,
        'b': BLOOD_BRIGHT,
        'f': FAT_YELLOW,
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
            
            # 加入一点噪点随机性，让烂肉看起来更有质感 (除了透明和高光外)
            if char in ['d', 'm', 'b', 'f']:
                noise = random.randint(-15, 15)
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
        
    output_file = os.path.join(base_dir, "gory_flesh_bone.png")
    
    w, h, pixels = generate_gore_texture()
    write_png(output_file, w, h, pixels)
    print(f"Texture saved to {output_file}")
