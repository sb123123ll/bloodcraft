import struct
import zlib
import os

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

def fill_rect(pixels, x_start, y_start, w, h, color):
    for y in range(y_start, y_start + h):
        for x in range(x_start, x_start + w):
            idx = x * 4
            pixels[y][idx:idx+4] = color

def generate_steve():
    width, height = 64, 64
    # 初始化为全透明
    pixels = [bytearray(width * 4) for _ in range(height)]
    
    # 颜色定义
    SKIN = [170, 118, 86, 255]       # 肤色
    HAIR = [51, 31, 15, 255]         # 头发深棕色
    SHIRT_CYAN = [0, 168, 168, 255]  # 青色上衣
    PANTS_BLUE = [43, 43, 137, 255]  # 深蓝色裤子
    SHOES_GRAY = [76, 76, 76, 255]   # 灰色鞋子
    EYE_WHITE = [255, 255, 255, 255] # 眼白
    EYE_PUPIL = [64, 64, 180, 255]   # 瞳孔蓝色
    MOUTH = [89, 58, 41, 255]        # 嘴巴/胡渣
    
    # --- 头部 (Head) ---
    # Top (8x8): x=8, y=0
    fill_rect(pixels, 8, 0, 8, 8, HAIR)
    # Bottom (8x8): x=16, y=0
    fill_rect(pixels, 16, 0, 8, 8, SKIN)
    # Right (8x8): x=0, y=8
    fill_rect(pixels, 0, 8, 8, 8, SKIN)
    fill_rect(pixels, 0, 8, 8, 2, HAIR) # Side hair
    # Front (8x8): x=8, y=8
    fill_rect(pixels, 8, 8, 8, 8, SKIN)
    fill_rect(pixels, 8, 8, 8, 2, HAIR) # Bangs
    # Eyes
    fill_rect(pixels, 8+1, 8+4, 2, 1, EYE_WHITE)
    fill_rect(pixels, 8+2, 8+4, 1, 1, EYE_PUPIL)
    fill_rect(pixels, 8+5, 8+4, 2, 1, EYE_WHITE)
    fill_rect(pixels, 8+5, 8+4, 1, 1, EYE_PUPIL)
    # Mouth/Beard
    fill_rect(pixels, 8+2, 8+6, 4, 1, MOUTH)
    # Left (8x8): x=16, y=8
    fill_rect(pixels, 16, 8, 8, 8, SKIN)
    fill_rect(pixels, 16, 8, 8, 2, HAIR)
    # Back (8x8): x=24, y=8
    fill_rect(pixels, 24, 8, 8, 8, HAIR)

    # --- 身体 (Body) ---
    # Top (8x4): x=20, y=16
    fill_rect(pixels, 20, 16, 8, 4, SHIRT_CYAN)
    # Bottom (8x4): x=28, y=16
    fill_rect(pixels, 28, 16, 8, 4, SHIRT_CYAN)
    # Right (4x12): x=16, y=20
    fill_rect(pixels, 16, 20, 4, 12, SHIRT_CYAN)
    # Front (8x12): x=20, y=20
    fill_rect(pixels, 20, 20, 8, 12, SHIRT_CYAN)
    # 领口 (V-neck)
    fill_rect(pixels, 20+3, 20, 2, 2, SKIN)
    # Left (4x12): x=28, y=20
    fill_rect(pixels, 28, 20, 4, 12, SHIRT_CYAN)
    # Back (8x12): x=32, y=20
    fill_rect(pixels, 32, 20, 8, 12, SHIRT_CYAN)

    # --- 右手 (Right Arm) ---
    # Top (4x4): x=44, y=16
    fill_rect(pixels, 44, 16, 4, 4, SKIN)
    # Bottom (4x4): x=48, y=16
    fill_rect(pixels, 48, 16, 4, 4, SKIN)
    # Right (4x12): x=40, y=20
    fill_rect(pixels, 40, 20, 4, 12, SKIN)
    fill_rect(pixels, 40, 20, 4, 4, SHIRT_CYAN) # Sleeve
    # Front (4x12): x=44, y=20
    fill_rect(pixels, 44, 20, 4, 12, SKIN)
    fill_rect(pixels, 44, 20, 4, 4, SHIRT_CYAN)
    # Left (4x12): x=48, y=20
    fill_rect(pixels, 48, 20, 4, 12, SKIN)
    fill_rect(pixels, 48, 20, 4, 4, SHIRT_CYAN)
    # Back (4x12): x=52, y=20
    fill_rect(pixels, 52, 20, 4, 12, SKIN)
    fill_rect(pixels, 52, 20, 4, 4, SHIRT_CYAN)

    # --- 左手 (Left Arm) (新版64x64皮肤左手在下面) ---
    # Top (4x4): x=36, y=48
    fill_rect(pixels, 36, 48, 4, 4, SKIN)
    # Bottom (4x4): x=40, y=48
    fill_rect(pixels, 40, 48, 4, 4, SKIN)
    # Right (4x12): x=32, y=52
    fill_rect(pixels, 32, 52, 4, 12, SKIN)
    fill_rect(pixels, 32, 52, 4, 4, SHIRT_CYAN)
    # Front (4x12): x=36, y=52
    fill_rect(pixels, 36, 52, 4, 12, SKIN)
    fill_rect(pixels, 36, 52, 4, 4, SHIRT_CYAN)
    # Left (4x12): x=40, y=52
    fill_rect(pixels, 40, 52, 4, 12, SKIN)
    fill_rect(pixels, 40, 52, 4, 4, SHIRT_CYAN)
    # Back (4x12): x=44, y=52
    fill_rect(pixels, 44, 52, 4, 12, SKIN)
    fill_rect(pixels, 44, 52, 4, 4, SHIRT_CYAN)

    # --- 右腿 (Right Leg) ---
    # Top (4x4): x=4, y=16
    fill_rect(pixels, 4, 16, 4, 4, PANTS_BLUE)
    # Bottom (4x4): x=8, y=16
    fill_rect(pixels, 8, 16, 4, 4, SHOES_GRAY)
    # Right (4x12): x=0, y=20
    fill_rect(pixels, 0, 20, 4, 12, PANTS_BLUE)
    fill_rect(pixels, 0, 20+8, 4, 4, SHOES_GRAY) # Shoe
    # Front (4x12): x=4, y=20
    fill_rect(pixels, 4, 20, 4, 12, PANTS_BLUE)
    fill_rect(pixels, 4, 20+8, 4, 4, SHOES_GRAY)
    # Left (4x12): x=8, y=20
    fill_rect(pixels, 8, 20, 4, 12, PANTS_BLUE)
    fill_rect(pixels, 8, 20+8, 4, 4, SHOES_GRAY)
    # Back (4x12): x=12, y=20
    fill_rect(pixels, 12, 20, 4, 12, PANTS_BLUE)
    fill_rect(pixels, 12, 20+8, 4, 4, SHOES_GRAY)

    # --- 左腿 (Left Leg) ---
    # Top (4x4): x=20, y=48
    fill_rect(pixels, 20, 48, 4, 4, PANTS_BLUE)
    # Bottom (4x4): x=24, y=48
    fill_rect(pixels, 24, 48, 4, 4, SHOES_GRAY)
    # Right (4x12): x=16, y=52
    fill_rect(pixels, 16, 52, 4, 12, PANTS_BLUE)
    fill_rect(pixels, 16, 52+8, 4, 4, SHOES_GRAY)
    # Front (4x12): x=20, y=52
    fill_rect(pixels, 20, 52, 4, 12, PANTS_BLUE)
    fill_rect(pixels, 20, 52+8, 4, 4, SHOES_GRAY)
    # Left (4x12): x=24, y=52
    fill_rect(pixels, 24, 52, 4, 12, PANTS_BLUE)
    fill_rect(pixels, 24, 52+8, 4, 4, SHOES_GRAY)
    # Back (4x12): x=28, y=52
    fill_rect(pixels, 28, 52, 4, 12, PANTS_BLUE)
    fill_rect(pixels, 28, 52+8, 4, 4, SHOES_GRAY)

    return width, height, pixels

if __name__ == '__main__':
    base_dir = r"g:\myfirstmod\bloodcraft\src\main\resources\assets\blood\textures\entity"
    if not os.path.exists(base_dir):
        os.makedirs(base_dir)
        
    output_file = os.path.join(base_dir, "steve.png")
    
    w, h, pixels = generate_steve()
    write_png(output_file, w, h, pixels)
    print(f"Texture saved to {output_file}")