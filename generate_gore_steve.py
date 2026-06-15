import struct
import zlib
import os

def write_png(filename, width, height, pixels):
    def make_chunk(ctype, data):
        return struct.pack('>I', len(data)) + ctype + data + struct.pack('>I', zlib.crc32(ctype + data) & 0xffffffff)
        
    with open(filename, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n')
        ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
        f.write(make_chunk(b'IHDR', ihdr_data))
        
        raw_data = bytearray()
        for row in pixels:
            raw_data.append(0) 
            raw_data.extend(row)
        compressed = zlib.compress(raw_data)
        f.write(make_chunk(b'IDAT', compressed))
        f.write(make_chunk(b'IEND', b''))

def fill_rect(pixels, x_start, y_start, w, h, color):
    for y in range(y_start, y_start + h):
        for x in range(x_start, x_start + w):
            idx = x * 4
            pixels[y][idx:idx+4] = color

def generate_gore_steve():
    width, height = 64, 64
    pixels = [bytearray(width * 4) for _ in range(height)]
    
    SKIN = [170, 118, 86, 255]
    HAIR = [51, 31, 15, 255]
    SHIRT_CYAN = [0, 168, 168, 255]
    PANTS_BLUE = [43, 43, 137, 255]
    SHOES_GRAY = [76, 76, 76, 255]
    MOUTH = [89, 58, 41, 255]
    
    # Gore Colors
    EYE_BLACK = [10, 10, 10, 255]
    BLOOD_RED = [139, 0, 0, 255]
    FRESH_BLOOD = [200, 20, 20, 255]
    MEAT = [150, 60, 60, 255]
    BONE = [220, 220, 210, 255]
    
    # --- 头部 (Head) ---
    fill_rect(pixels, 8, 0, 8, 8, HAIR) # Top
    fill_rect(pixels, 16, 0, 8, 8, SKIN) # Bottom
    fill_rect(pixels, 0, 8, 8, 8, SKIN) # Right
    fill_rect(pixels, 0, 8, 8, 2, HAIR) 
    fill_rect(pixels, 8, 8, 8, 8, SKIN) # Front
    fill_rect(pixels, 8, 8, 8, 2, HAIR) 
    
    # Black Eyes
    fill_rect(pixels, 8+1, 8+4, 2, 1, EYE_BLACK)
    fill_rect(pixels, 8+5, 8+4, 2, 1, EYE_BLACK)
    # Mouth/Beard
    fill_rect(pixels, 8+2, 8+6, 4, 1, MOUTH)
    
    # Blood Tears (流到下巴)
    fill_rect(pixels, 8+1, 8+5, 1, 3, BLOOD_RED)
    fill_rect(pixels, 8+2, 8+5, 1, 2, FRESH_BLOOD)
    fill_rect(pixels, 8+5, 8+5, 1, 3, BLOOD_RED)
    fill_rect(pixels, 8+6, 8+5, 1, 2, FRESH_BLOOD)
    
    fill_rect(pixels, 16, 8, 8, 8, SKIN) # Left
    fill_rect(pixels, 16, 8, 8, 2, HAIR)
    fill_rect(pixels, 24, 8, 8, 8, HAIR) # Back

    # --- 身体 (Body) ---
    fill_rect(pixels, 20, 16, 8, 4, SHIRT_CYAN) # Top
    fill_rect(pixels, 28, 16, 8, 4, SHIRT_CYAN) # Bottom
    fill_rect(pixels, 16, 20, 4, 12, SHIRT_CYAN) # Right
    fill_rect(pixels, 20, 20, 8, 12, SHIRT_CYAN) # Front
    fill_rect(pixels, 20+3, 20, 2, 2, SKIN) # V-neck
    
    # Slash Wound on Chest (衣服划破露出肉色和血肉)
    # 斜着的划痕
    fill_rect(pixels, 20+1, 20+3, 2, 2, SKIN)
    fill_rect(pixels, 20+2, 20+4, 4, 2, MEAT)
    fill_rect(pixels, 20+3, 20+5, 3, 2, FRESH_BLOOD)
    fill_rect(pixels, 20+4, 20+6, 2, 2, SKIN)
    fill_rect(pixels, 20+5, 20+7, 2, 2, MEAT)
    fill_rect(pixels, 20+6, 20+8, 1, 2, BLOOD_RED)
    
    fill_rect(pixels, 28, 20, 4, 12, SHIRT_CYAN) # Left
    fill_rect(pixels, 32, 20, 8, 12, SHIRT_CYAN) # Back

    # --- 右手 (Right Arm) ---
    fill_rect(pixels, 44, 16, 4, 4, SKIN)
    fill_rect(pixels, 48, 16, 4, 4, SKIN)
    fill_rect(pixels, 40, 20, 4, 12, SKIN)
    fill_rect(pixels, 40, 20, 4, 4, SHIRT_CYAN) 
    fill_rect(pixels, 44, 20, 4, 12, SKIN)
    fill_rect(pixels, 44, 20, 4, 4, SHIRT_CYAN)
    fill_rect(pixels, 48, 20, 4, 12, SKIN)
    fill_rect(pixels, 48, 20, 4, 4, SHIRT_CYAN)
    fill_rect(pixels, 52, 20, 4, 12, SKIN)
    fill_rect(pixels, 52, 20, 4, 4, SHIRT_CYAN)

    # --- 左手 (Left Arm) ---
    fill_rect(pixels, 36, 48, 4, 4, SKIN)
    fill_rect(pixels, 40, 48, 4, 4, SKIN)
    fill_rect(pixels, 32, 52, 4, 12, SKIN)
    fill_rect(pixels, 32, 52, 4, 4, SHIRT_CYAN)
    fill_rect(pixels, 36, 52, 4, 12, SKIN)
    fill_rect(pixels, 36, 52, 4, 4, SHIRT_CYAN)
    fill_rect(pixels, 40, 52, 4, 12, SKIN)
    fill_rect(pixels, 40, 52, 4, 4, SHIRT_CYAN)
    fill_rect(pixels, 44, 52, 4, 12, SKIN)
    fill_rect(pixels, 44, 52, 4, 4, SHIRT_CYAN)

    # --- 右腿 (Right Leg) - 血肉模糊 ---
    fill_rect(pixels, 4, 16, 4, 4, MEAT)
    fill_rect(pixels, 8, 16, 4, 4, BLOOD_RED)
    
    # 碎布料和血肉混合
    fill_rect(pixels, 0, 20, 4, 12, MEAT)
    fill_rect(pixels, 0, 20+1, 2, 2, PANTS_BLUE) # 裤子碎布
    fill_rect(pixels, 0, 20+5, 4, 3, BLOOD_RED)
    
    fill_rect(pixels, 4, 20, 4, 12, MEAT)
    fill_rect(pixels, 4, 20+3, 1, 3, BONE) # 露骨
    fill_rect(pixels, 4, 20+6, 3, 2, FRESH_BLOOD)
    fill_rect(pixels, 4, 20+10, 2, 2, PANTS_BLUE) # 裤脚碎布
    
    fill_rect(pixels, 8, 20, 4, 12, BLOOD_RED)
    fill_rect(pixels, 8, 20+2, 2, 5, MEAT)
    
    fill_rect(pixels, 12, 20, 4, 12, MEAT)
    fill_rect(pixels, 12, 20+8, 4, 4, BLOOD_RED)

    # --- 左腿 (Left Leg) - 血肉模糊 ---
    fill_rect(pixels, 20, 48, 4, 4, BLOOD_RED)
    fill_rect(pixels, 24, 48, 4, 4, MEAT)
    
    fill_rect(pixels, 16, 52, 4, 12, MEAT)
    fill_rect(pixels, 16, 52+2, 3, 4, BLOOD_RED)
    
    fill_rect(pixels, 20, 52, 4, 12, MEAT)
    fill_rect(pixels, 20, 52, 3, 3, PANTS_BLUE) # 裤子碎片
    fill_rect(pixels, 20+2, 52+4, 1, 4, BONE) # 露骨
    fill_rect(pixels, 20, 52+8, 4, 4, BLOOD_RED)
    
    fill_rect(pixels, 24, 52, 4, 12, BLOOD_RED)
    fill_rect(pixels, 24, 52+4, 2, 4, MEAT)
    
    fill_rect(pixels, 28, 52, 4, 12, MEAT)
    fill_rect(pixels, 28, 52+6, 2, 2, PANTS_BLUE)

    return width, height, pixels

if __name__ == '__main__':
    base_dir = r"g:\myfirstmod\bloodcraft\src\main\resources\assets\blood\textures\entity"
    if not os.path.exists(base_dir):
        os.makedirs(base_dir)
        
    output_file = os.path.join(base_dir, "steve_gore.png")
    
    w, h, pixels = generate_gore_steve()
    write_png(output_file, w, h, pixels)
    print(f"Texture saved to {output_file}")