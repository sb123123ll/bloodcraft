from PIL import Image, ImageDraw

def create_cooked_heart_texture():
    # 颜色定义：煮熟后的肉类颜色
    # 煮熟后血液凝固，肌肉变性，颜色从鲜红转为灰褐、深棕
    dark_brown = (50, 30, 20, 255)      # 极暗的阴影/焦褐部分
    mid_brown = (75, 45, 30, 255)       # 中等深度的棕色，增加层次感
    base_brown = (100, 60, 40, 255)     # 基础棕色，煮熟的肌肉
    light_brown = (130, 80, 50, 255)    # 亮棕色，边缘或较薄的部分
    fat_cooked = (180, 160, 110, 255)   # 煮熟的脂肪，发黄变暗
    vein_black = (30, 20, 20, 255)      # 静脉凝固后的黑褐色
    artery_dark = (70, 30, 30, 255)     # 主动脉凝固后的暗红偏褐
    highlight_dry = (150, 100, 70, 255) # 失去水润光泽后的干瘪高光

    color_dict = {
        0: (0, 0, 0, 0),
        1: dark_brown,
        2: mid_brown,     # 使用 mid_brown 替换生肉的 dark_blood，丰富暗部层次
        3: base_brown,    
        4: light_brown,   
        5: highlight_dry, 
        6: vein_black,    
        7: artery_dark,   
        8: fat_cooked     
    }

    # 修改 pixel_map 下半部分（y=9 到 y=14），打乱原本连续的相同数字，增加噪点和纤维感
    pixel_map = [
        [0,0,0,0,0,7,7,0,6,6,0,0,0,0,0,0], # 0
        [0,0,0,0,7,7,7,0,6,6,0,0,0,0,0,0], # 1
        [0,0,0,6,6,7,5,7,7,2,0,0,0,0,0,0], # 2
        [0,0,0,6,1,7,4,7,7,2,0,0,0,0,0,0], # 3
        [0,0,0,1,2,2,3,4,2,2,2,0,0,0,0,0], # 4
        [0,0,2,2,3,4,5,4,3,2,2,2,0,0,0,0], # 5
        [0,2,3,4,4,3,6,6,3,4,3,2,2,0,0,0], # 6
        [0,2,4,5,4,3,8,8,6,3,5,4,2,0,0,0], # 7
        [0,1,3,4,3,2,6,6,8,3,4,3,2,0,0,0], # 8
        [0,1,2,3,1,3,2,6,8,2,4,3,2,0,0,0], # 9  (丰富层次)
        [0,0,2,1,2,1,2,2,6,8,3,2,1,0,0,0], # 10 (打乱连续的1和2，加入3)
        [0,0,0,1,3,2,1,2,1,6,3,2,2,0,0,0], # 11 (增加高光和暗部的交错)
        [0,0,0,0,2,1,2,1,2,1,3,2,1,0,0,0], # 12 (肌肉纤维纹理感)
        [0,0,0,0,0,1,3,2,1,2,1,2,0,0,0,0], # 13
        [0,0,0,0,0,0,2,1,3,2,1,0,0,0,0,0], # 14
        [0,0,0,0,0,0,0,1,2,0,0,0,0,0,0,0]  # 15
    ]

    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    for y in range(16):
        for x in range(16):
            color_code = pixel_map[y][x]
            draw.point((x, y), fill=color_dict[color_code])
            
    # 添加一些特有的煮熟细节（表面焦痕或血沫凝结）
    draw.point((10, 6), fill=highlight_dry)
    draw.point((11, 8), fill=highlight_dry)
    
    # 凝固的暗色血块
    draw.point((5, 10), fill=vein_black)
    draw.point((6, 12), fill=dark_brown)
    draw.point((8, 12), fill=dark_brown)

    save_path = 'g:\\myfirstmod\\bloodcraft\\src\\main\\resources\\assets\\blood\\textures\\items\\cooked_human_heart.png'
    img.save(save_path)
    print(f"Cooked heart texture saved to: {save_path}")

if __name__ == "__main__":
    create_cooked_heart_texture()
