import struct
import zlib
import os

def read_png(filename):
    with open(filename, 'rb') as f:
        assert f.read(8) == b'\x89PNG\r\n\x1a\n'
        chunks = []
        while True:
            length_bytes = f.read(4)
            if not length_bytes: break
            length = struct.unpack('>I', length_bytes)[0]
            chunk_type = f.read(4)
            chunk_data = f.read(length)
            crc = f.read(4)
            chunks.append((chunk_type, chunk_data))
            if chunk_type == b'IEND': break
        
        width, height, bitdepth, colortype, comp, filter_meth, interlace = struct.unpack('>IIBBBBB', chunks[0][1])
        assert colortype == 6, "Must be RGBA"
        
        idat_data = b''.join(data for ctype, data in chunks if ctype == b'IDAT')
        decompressed = zlib.decompress(idat_data)
        
        pixels = []
        stride = width * 4
        idx = 0
        for y in range(height):
            filter_type = decompressed[idx]
            idx += 1
            row = bytearray(decompressed[idx:idx+stride])
            idx += stride
            # Simplified for filter_type 0
            if filter_type == 0:
                pass
            # Very basic defiltering (might not cover all PNGs, but works for simple ones)
            elif filter_type == 1: # Sub
                for x in range(4, stride):
                    row[x] = (row[x] + row[x-4]) % 256
            elif filter_type == 2: # Up
                if y > 0:
                    prev_row = pixels[-1]
                    for x in range(stride):
                        row[x] = (row[x] + prev_row[x]) % 256
            
            pixels.append(row)
            
        return width, height, pixels

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

import random
import math

def scale_and_distort(pixels, orig_w, orig_h, scale_x, scale_y, shift_x=0, shift_y=0, blood_surge=False):
    new_w = int(orig_w * scale_x)
    new_h = int(orig_h * scale_y)
    
    # Nearest neighbor scaling with shift for twitching
    scaled = []
    for y in range(new_h):
        row = bytearray(orig_w * 4) 
        orig_y = min(orig_h - 1, int(y / scale_y))
        for x in range(new_w):
            orig_x = min(orig_w - 1, int(x / scale_x))
            orig_idx = orig_x * 4
            idx = x * 4
            if idx < len(row):
                r, g, b, a = pixels[orig_y][orig_idx:orig_idx+4]
                
                # 如果是血管部分 (偏红的像素)，在 blood_surge 为 True 时增强其亮度和对比度，模拟血管暴起
                if blood_surge and a > 0 and r > g + 20 and r > b + 20:
                    r = min(255, int(r * 1.4))
                    g = max(0, int(g * 0.8))
                    b = max(0, int(b * 0.8))
                    
                row[idx:idx+4] = [r, g, b, a]
        scaled.append(row)
        
    # Center crop back to orig_w, orig_h, adding the shift offset for twitching
    offset_x = (new_w - orig_w) // 2 + shift_x
    offset_y = (new_h - orig_h) // 2 + shift_y
    
    cropped = []
    for y in range(orig_h):
        row = bytearray(orig_w * 4)
        scaled_y = y + offset_y
        if 0 <= scaled_y < new_h:
            src_row = scaled[scaled_y]
            for x in range(orig_w):
                scaled_x = x + offset_x
                if 0 <= scaled_x < new_w:
                    idx = x * 4
                    src_idx = scaled_x * 4
                    if src_idx < len(src_row):
                        row[idx:idx+4] = src_row[src_idx:src_idx+4]
        cropped.append(row)
        
    return cropped

def create_animation():
    base_dir = r"g:\myfirstmod\bloodcraft\src\main\resources\assets\blood\textures\items"
    input_file = os.path.join(base_dir, "plucked_eye.png")
    
    # Check if the file is already an animation (height > width)
    w, h, pixels = read_png(input_file)
    if h > w:
        print("Image is already a sprite sheet! We need the original static image to regenerate.")
        # If it's already a sprite sheet, we extract the first frame (frame 0) to use as our base
        pixels = pixels[:w]
        h = w
        
    # 创建一个无缝衔接的平滑心脏跳动循环 (8帧)
    # 模拟“膨胀 -> 收缩 -> 膨胀 -> 收缩”的均匀连续跳动
    
    # 帧0：基础状态
    frame0 = [bytearray(row) for row in pixels]
    
    # 帧1：稍微膨胀
    frame1 = scale_and_distort(pixels, w, h, 1.05, 0.95, blood_surge=False) 
    
    # 帧2：最大膨胀（血管微亮）
    frame2 = scale_and_distort(pixels, w, h, 1.15, 0.85, blood_surge=True) 
    
    # 帧3：回落中
    frame3 = scale_and_distort(pixels, w, h, 1.08, 0.92, blood_surge=False) 
    
    # 帧4：收缩过基础状态（变得细长）
    frame4 = scale_and_distort(pixels, w, h, 0.95, 1.05, blood_surge=False) 
    
    # 帧5：最大收缩
    frame5 = scale_and_distort(pixels, w, h, 0.85, 1.15, blood_surge=False) 
    
    # 帧6：回弹中
    frame6 = scale_and_distort(pixels, w, h, 0.92, 1.08, blood_surge=False) 
    
    # 帧7：即将回到基础状态
    frame7 = scale_and_distort(pixels, w, h, 0.98, 1.02, blood_surge=False) 
    
    all_pixels = []
    all_pixels.extend(frame0)
    all_pixels.extend(frame1)
    all_pixels.extend(frame2)
    all_pixels.extend(frame3)
    all_pixels.extend(frame4)
    all_pixels.extend(frame5)
    all_pixels.extend(frame6)
    all_pixels.extend(frame7)
    
    output_file = os.path.join(base_dir, "plucked_eye.png")
    write_png(output_file, w, w * 8, all_pixels) # 8 frames total
    print(f"Animation saved to {output_file}")

if __name__ == '__main__':
    create_animation()
