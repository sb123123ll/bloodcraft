from PIL import Image, ImageDraw
import os

# 路径配置
base_path = "src/main/resources/assets/blood/textures/"
planks_path = os.path.join(base_path, "blocks/planks_blood.png")

# 确保输入存在
if not os.path.exists(planks_path):
    print("planks_blood.png not found!")
    exit(1)

# 加载木板贴图
planks_img = Image.open(planks_path).convert("RGBA")

# --- 1. 生成血腥木活板门贴图 ---
trapdoor_img = planks_img.copy()
# 16x16 中心完美居中且大小适中，建议挖 4x4 (即 x:6~9, y:6~9) 或 6x6 (x:5~10, y:5~10)
# 这里挖 6x6 (x:5~10, y:5~10) 让洞更大一些且完全居中
for y in range(5, 11):
    for x in range(5, 11):
        trapdoor_img.putpixel((x, y), (0, 0, 0, 0))
        
trapdoor_img.save(os.path.join(base_path, "blocks/blood_trapdoor.png"))
print("blood_trapdoor.png created.")

# --- 2. 生成血腥木门方块贴图 (上下两半) ---
# 我们可以直接把木板当做基础，然后稍微画点边框和把手
door_lower = planks_img.copy()
door_upper = planks_img.copy()

# 下半部分：加点边框，去掉一些中间部分或者直接加个把手
# 为了简单，我们在上半部分右侧加一个把手，中间挖几个洞
draw_upper = ImageDraw.Draw(door_upper)
draw_lower = ImageDraw.Draw(door_lower)

# 简单画一个把手 (比如 x=12, y=8, 2x3 像素的暗红色或黑色)
for y in range(8, 11):
    for x in range(12, 14):
        door_upper.putpixel((x, y), (40, 10, 10, 255))

# 在上半部分挖几个洞 (玻璃窗效果)
# 之前是 y:2~6 (高度5)，现在减少上下宽度，改为 y:3~5 (高度3)
for y in range(3, 6):
    for x in range(3, 7):
        door_upper.putpixel((x, y), (0, 0, 0, 0))
    for x in range(9, 13):
        door_upper.putpixel((x, y), (0, 0, 0, 0))

door_lower.save(os.path.join(base_path, "blocks/blood_door_lower.png"))
door_upper.save(os.path.join(base_path, "blocks/blood_door_upper.png"))
print("blood_door_lower.png and blood_door_upper.png created.")

# --- 3. 生成血腥木门物品贴图 ---
# 物品贴图通常是16x16，把上下半部分缩放拼在一起
door_item = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
door_item_upper = door_upper.resize((8, 8), Image.NEAREST)
door_item_lower = door_lower.resize((8, 8), Image.NEAREST)
door_item.paste(door_item_upper, (4, 0))
door_item.paste(door_item_lower, (4, 8))

door_item.save(os.path.join(base_path, "items/blood_door.png"))
print("blood_door.png (item) created.")
