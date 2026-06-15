import struct
import zlib
import os

def read_png(filename):
    with open(filename, 'rb') as f:
        # 跳过PNG签名
        f.read(8)
        
        pixels = None
        width = None
        height = None
        
        while True:
            # 读取chunk长度
            length = struct.unpack('>I', f.read(4))[0]
            # 读取chunk类型
            chunk_type = f.read(4)
            # 读取chunk数据
            data = f.read(length)
            # 读取CRC
            f.read(4)
            
            if chunk_type == b'IHDR':
                width = struct.unpack('>I', data[0:4])[0]
                height = struct.unpack('>I', data[4:8])[0]
            elif chunk_type == b'IDAT':
                compressed = data
            elif chunk_type == b'IEND':
                break
        
        # 解压IDAT数据
        raw_data = zlib.decompress(compressed)
        
        # 解析像素数据
        pixels = []
        pos = 0
        for y in range(height):
            # 跳过filter type
            pos += 1
            row = []
            for x in range(width):
                r = raw_data[pos]
                g = raw_data[pos + 1]
                b = raw_data[pos + 2]
                a = raw_data[pos + 3]
                row.append([r, g, b, a])
                pos += 4
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
            for pixel in row:
                raw_data.extend(pixel)
        compressed = zlib.compress(raw_data)
        f.write(make_chunk(b'IDAT', compressed))
        
        # IEND
        f.write(make_chunk(b'IEND', b''))

def modify_colors(r, g, b, a):
    if a == 0:
        return [r, g, b, a]
    
    # 根据原始颜色判断应该改成什么颜色
    # 银白色剑刃 (原版灰色/白色) -> 保持银白色
    if r > 180 and g > 180 and b > 180:
        return [200, 200, 210, a]
    # 剑刃灰色
    elif r > 140 and g > 140 and b > 140:
        return [160, 165, 175, a]
    # 金棕色手柄 (原版棕色) -> 金棕色
    elif r > 100 and g > 70 and b < 100:
        return [200, 150, 80, a]
    # 手柄暗部
    elif r > 80 and g > 60 and b < 80:
        return [140, 100, 50, a]
    # 绿色毒液 (原版绿色) -> 腥绿色
    elif g > r + 30 and g > b + 30:
        return [60, 180, 80, a]
    # 血色 (原版红色) -> 鲜血
    elif r > g + 50 and r > b + 50:
        return [200, 40, 50, a]
    else:
        return [r, g, b, a]

if __name__ == '__main__':
    input_file = r"g:\myfirstmod\bloodcraft\src\main\resources\assets\blood\textures\items\venomous_stinger_dagger.png"
    output_file = r"g:\myfirstmod\bloodcraft\src\main\resources\assets\blood\textures\items\venomous_stinger_dagger.png"
    
    w, h, pixels = read_png(input_file)
    
    # 修改颜色，保持形状不变
    new_pixels = []
    for y in range(h):
        row = []
        for x in range(w):
            r, g, b, a = pixels[y][x]
            new_pixel = modify_colors(r, g, b, a)
            row.append(new_pixel)
        new_pixels.append(row)
    
    write_png(output_file, w, h, new_pixels)
    print(f"Texture modified and saved to {output_file}")
