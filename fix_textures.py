import os
from PIL import Image

src_path = 'src/main/resources/assets/blood/textures/entity/cultist_preacher.png'
out_dir = 'src/main/resources/assets/blood/textures/blocks/skull/'

img = Image.open(src_path).convert('RGBA')

def extract(box, name):
    part = img.crop(box)
    # 创建标准的 16x16 透明画布
    canvas = Image.new('RGBA', (16, 16), (0,0,0,0))
    # 将截取的部分贴在左上角
    canvas.paste(part, (0, 0))
    canvas.save(os.path.join(out_dir, name))
    print(f"Saved {name}")

# 根据村民模型标准的头部 UV 进行精确截取 (宽8, 高10, 深8)
extract((8, 8, 16, 18), 'preacher_head_front.png')
extract((24, 8, 32, 18), 'preacher_head_back.png')
extract((16, 8, 24, 18), 'preacher_head_left.png')
extract((0, 8, 8, 18), 'preacher_head_right.png')
extract((8, 0, 16, 8), 'preacher_head_top.png')
extract((16, 0, 24, 8), 'preacher_head_bottom.png')
