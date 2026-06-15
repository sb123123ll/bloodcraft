import os
from PIL import Image

# 颜色调色板定义
palette = {
    'D': (36, 20, 12),    # 暗树皮 Dark Bark
    'B': (56, 35, 23),    # 中树皮 Mid Bark
    'L': (71, 45, 30),    # 亮树皮 Light Bark
    'd': (50, 5, 5),      # 暗血肉边 Deep Flesh/Blood
    'F': (110, 15, 15),   # 烂肉 Mid Flesh
    'f': (150, 30, 30),   # 粉红血肉 Light Flesh
    'O': (210, 200, 180), # 骨茬 Bone
    'R': (180, 0, 0),     # 鲜血 Bright Blood
    'P': (70, 45, 30),    # 木板亮 Plank Light
    'p': (55, 35, 22),    # 木板中 Plank Mid
    'k': (35, 20, 12),    # 木板暗 Plank Dark
    'G': (15, 5, 5),      # 木板缝隙暗红 Gap Dark
    'M': (150, 100, 70),  # 骨髓 Marrow
    'S': (80, 5, 5),      # 缝隙暗血
}

# 侧面贴图：两边是树皮，中间有一道惨烈的、带骨茬和血肉的巨大裂缝，带鲜血滴落
side_art = [
    "LBDBdFfFdBDBLBDB",
    "BDBLdfOfdBLRDBLB",
    "DBRBdFOfdDBLDBLB",
    "BLBDdFfFdBDBLBLB",
    "LBDBdfFfdBLBDBDB",
    "BDBLdFfFddDBLBLB",
    "DBLBdFOfFdBDBLDB",
    "BLBDdfOFFdBLBDBL",
    "LBDBdFfFddBLRDBB",
    "BDBLdFFfdBDBLBLB",
    "DBLBdfFFdBLBDBLB",
    "BLBDdFfFdDBLDBDB",
    "LBRBdfOfdBLBDBLB",
    "BDBLdFOFdBDBLBLB",
    "DBLBdFfFddBLDBDB",
    "BLBDdfFFdBLBDBLB"
]

# 顶面贴图：被血肉腐蚀的年轮，最中心是骨髓
top_art = [
    "DDDBLLLLLLBBBDDD",
    "DBLddddddddddLBD",
    "BLdFFffffFFFfdLB",
    "BdfFddddddddFfdB",
    "LdfdFffffFFfddfL",
    "LfdFfOOOOMOffFdL",
    "BdfdfOMMMOOMfdfB",
    "BdfdFOMMMOMOfdfB",
    "LdfdfOOMOOOOfdfL",
    "LfdFfOOOOOOffFdL",
    "BdfdFffffFFfddfB",
    "BdfFddddddddFfdB",
    "BLdFFffffFFFfdLB",
    "DBLddddddddddLBD",
    "DDBBLLBBLLBBBDDD",
    "DDDBLLLLLLBBBDDD"
]

# 木板贴图：四条横木板，缝隙被血肉和碎骨填满，木板上有飞溅的血滴
plank_art = [
    "PPPPpPPPPPPpPPPP",
    "pppppppppppppppp",
    "kkkkkkRkkkkkkkkk",
    "GGSdFfFdGSGdFFfG",
    "PPpPPPPPPpPPPRPP",
    "ppppRppppppppppp",
    "kkkkkkkkkkkkkkkk",
    "dFFfSGSGGdFfFdGS",
    "PPPPpPPPPPPpPPPP",
    "pppppppppppppppp",
    "kkkkRkkkkkkkkkkk",
    "SGSdFOfdGSGdFFfG",
    "PPpPPPPPPpPPPPPP",
    "ppppppppppppRppp",
    "kkkkkkkkkkkkkkkk",
    "dFfFdGGGSdFOfdGS"
]

def create_img(art, path):
    img = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            char = art[y][x]
            img.putpixel((x, y), palette[char] + (255,))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)

base = "g:/myfirstmod/bloodcraft/src/main/resources/assets/blood/textures/blocks/"
create_img(side_art, base + "log_blood_side.png")
create_img(top_art, base + "log_blood_top.png")
create_img(plank_art, base + "planks_blood.png")
print("Pixel art textures generated!")