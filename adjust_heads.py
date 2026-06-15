import json
import os

item_models_dir = "g:/myfirstmod/bloodcraft/src/main/resources/assets/blood/models/item"
heads = ["parasitic_steve_head.json", "preacher_head.json"]

for head in heads:
    path = os.path.join(item_models_dir, head)
    if not os.path.exists(path):
        continue
        
    with open(path, "r") as f:
        data = json.load(f)
    
    # 调整 display 属性，使其在物品栏和手中的大小与原版头颅几乎一致
    data["display"] = {
        "gui": {
            "rotation": [30, 225, 0],
            "translation": [0, -3, 0],
            "scale": [1.5, 1.5, 1.5]
        },
        "ground": {
            "translation": [0, 2, 0],
            "scale": [0.5, 0.5, 0.5]
        },
        "fixed": {
            "translation": [0, -1, 0],
            "scale": [0.75, 0.75, 0.75]
        },
        "thirdperson_righthand": {
            "rotation": [75, 45, 0],
            "translation": [0, 2.5, 0],
            "scale": [0.5, 0.5, 0.5]
        },
        "firstperson_righthand": {
            "rotation": [0, 45, 0],
            "translation": [0, 0, 0],
            "scale": [0.5, 0.5, 0.5]
        }
    }
    
    with open(path, "w") as f:
        json.dump(data, f, indent=4)

print("Head models updated successfully.")
