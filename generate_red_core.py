import os
from PIL import Image

def generate_red_sting_core():
    src_path = "g:/myfirstmod/bloodcraft/src/main/resources/assets/blood/textures/items/sting_core.png"
    # 我们暂时将新贴图命名为 blood_sting_core.png (血腥毒刺核心)
    dest_path = "g:/myfirstmod/bloodcraft/src/main/resources/assets/blood/textures/items/blood_sting_core.png"
    
    if not os.path.exists(src_path):
        print(f"Source file not found: {src_path}")
        return
        
    img = Image.open(src_path).convert("RGBA")
    pixels = img.load()
    
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pixels[x, y]
            if a > 0:
                # 1. 计算原像素的灰度/亮度 (Luminance) 以保留深浅细节
                l = (0.299 * r + 0.587 * g + 0.114 * b)
                
                # 2. 将亮度映射到红色调上
                # 亮度高的地方呈现亮红甚至偏粉，亮度低的地方呈现暗红，保留原来的质感
                new_r = min(255, int(l * 1.6))
                new_g = min(255, int(l * 0.2))
                new_b = min(255, int(l * 0.25))
                
                pixels[x, y] = (new_r, new_g, new_b, a)
                
    os.makedirs(os.path.dirname(dest_path), exist_ok=True)
    img.save(dest_path)
    print(f"Successfully generated new red texture at: {dest_path}")

if __name__ == '__main__':
    generate_red_sting_core()
