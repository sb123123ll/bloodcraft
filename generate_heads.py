import os
from PIL import Image

def generate_steve_head_textures():
    # Parasitic Steve Texture (using steve_gore.png as it's the custom one)
    src_path = "g:/myfirstmod/bloodcraft/src/main/resources/assets/blood/textures/entity/steve_gore.png"
    if not os.path.exists(src_path):
        print(f"File not found: {src_path}")
        return
        
    img = Image.open(src_path).convert("RGBA")
    
    # Coordinates in 64x64 skin:
    # Front: (8,8) to (16,16)
    # Back: (24,8) to (32,16)
    # Right: (0,8) to (8,16)
    # Left: (16,8) to (24,16)
    # Top: (8,0) to (16,8)
    # Bottom: (16,0) to (24,8)
    
    faces = {
        "front": img.crop((8, 8, 16, 16)),
        "back": img.crop((24, 8, 32, 16)),
        "right": img.crop((0, 8, 8, 16)),
        "left": img.crop((16, 8, 24, 16)),
        "top": img.crop((8, 0, 16, 8)),
        "bottom": img.crop((16, 0, 24, 8))
    }
    
    dest_dir = "g:/myfirstmod/bloodcraft/src/main/resources/assets/blood/textures/blocks/skull"
    os.makedirs(dest_dir, exist_ok=True)
    
    for name, face_img in faces.items():
        # Resize to 16x16 to match standard block texture size for better mipmapping
        face_img = face_img.resize((16, 16), Image.NEAREST)
        face_img.save(os.path.join(dest_dir, f"parasitic_steve_head_{name}.png"))

def generate_villager_head_textures():
    # Villager Texture
    src_path = "g:/myfirstmod/bloodcraft/src/main/resources/assets/blood/textures/entity/cultist_preacher.png"
    if not os.path.exists(src_path):
        print(f"File not found: {src_path}")
        return
        
    img = Image.open(src_path).convert("RGBA")
    
    # Villager head in standard 64x64 skin:
    # Top: (0,0) to (8,8)
    # Bottom: (8,0) to (16,8)  # wait, it's usually 16,0 to 24,8 in standard skin. Villager is different.
    # Actually villager texture map: 
    # Head Top: (0,0) to (8,8) -> Wait no, usually villager is:
    # X,Y offset for head is (0,0). So Top is (0,0) to (8,8), Bottom is (8,0) to (16,8)
    # Front is (0,8) to (8,16), Back is (16,8) to (24,16), Right is (24,8) to (32,16), Left is (8,8) to (16,16)?
    # Let's check typical villager map.
    # It's better to just write the standard villager head mappings:
    # Head box: width 8, height 10, depth 8.
    # Wait, villager head is not a perfect cube! It's 8x10x8!
    # And there is a nose.
    # If we map it to a standard 8x8x8 block for simplicity:
    # Let's crop: Top(0,0 to 8,8), Bottom(8,0 to 16,8), Right(0,8 to 8,18), Front(8,8 to 16,18), Left(16,8 to 24,18), Back(24,8 to 32,18).
    
    # To make it a standard 8x8x8 skull, let's just take the top 8x8 of the face.
    faces = {
        "top": img.crop((8, 0, 16, 8)),
        "bottom": img.crop((16, 0, 24, 8)),
        "right": img.crop((0, 8, 8, 18)),
        "front": img.crop((8, 8, 16, 18)),
        "left": img.crop((16, 8, 24, 18)),
        "back": img.crop((24, 8, 32, 18))
    }
    
    dest_dir = "g:/myfirstmod/bloodcraft/src/main/resources/assets/blood/textures/blocks/skull"
    os.makedirs(dest_dir, exist_ok=True)
    
    for name, face_img in faces.items():
        if name in ["top", "bottom"]:
            face_img = face_img.resize((16, 16), Image.NEAREST)
        else:
            face_img = face_img.resize((16, 20), Image.NEAREST)
        face_img.save(os.path.join(dest_dir, f"preacher_head_{name}.png"))

def generate_json_models():
    # Block models
    models_dir = "g:/myfirstmod/bloodcraft/src/main/resources/assets/blood/models/block/skull"
    os.makedirs(models_dir, exist_ok=True)
    
    for head_type in ["parasitic_steve_head", "preacher_head"]:
        height = 10 if head_type == "preacher_head" else 8
        model_json = f"""{{
    "parent": "block/block",
    "textures": {{
        "particle": "blood:blocks/skull/{head_type}_front",
        "up": "blood:blocks/skull/{head_type}_top",
        "down": "blood:blocks/skull/{head_type}_bottom",
        "north": "blood:blocks/skull/{head_type}_front",
        "south": "blood:blocks/skull/{head_type}_back",
        "east": "blood:blocks/skull/{head_type}_left",
        "west": "blood:blocks/skull/{head_type}_right"
    }},
    "elements": [
        {{
            "from": [4, 0, 4],
            "to": [12, {height}, 12],
            "faces": {{
                "down":  {{ "uv": [0, 0, 16, 16], "texture": "#down" }},
                "up":    {{ "uv": [0, 0, 16, 16], "texture": "#up" }},
                "north": {{ "uv": [0, 0, 16, 16], "texture": "#north" }},
                "south": {{ "uv": [0, 0, 16, 16], "texture": "#south" }},
                "west":  {{ "uv": [0, 0, 16, 16], "texture": "#west" }},
                "east":  {{ "uv": [0, 0, 16, 16], "texture": "#east" }}
            }}
        }}
    ]
}}"""
        with open(os.path.join(models_dir, f"{head_type}.json"), "w") as f:
            f.write(model_json)
            
    # Blockstates
    blockstates_dir = "g:/myfirstmod/bloodcraft/src/main/resources/assets/blood/blockstates"
    for head_type in ["parasitic_steve_head", "preacher_head"]:
        bs_json = f"""{{
    "variants": {{
        "facing=north": {{ "model": "blood:skull/{head_type}" }},
        "facing=east":  {{ "model": "blood:skull/{head_type}", "y": 90 }},
        "facing=south": {{ "model": "blood:skull/{head_type}", "y": 180 }},
        "facing=west":  {{ "model": "blood:skull/{head_type}", "y": 270 }},
        "facing=up":    {{ "model": "blood:skull/{head_type}" }}
    }}
}}"""
        with open(os.path.join(blockstates_dir, f"{head_type}.json"), "w") as f:
            f.write(bs_json)
            
    # Item models
    item_models_dir = "g:/myfirstmod/bloodcraft/src/main/resources/assets/blood/models/item"
    for head_type in ["parasitic_steve_head", "preacher_head"]:
        item_json = f"""{{
    "parent": "blood:block/skull/{head_type}",
    "display": {{
        "thirdperson_righthand": {{
            "rotation": [75, 45, 0],
            "translation": [0, 2.5, 0],
            "scale": [0.375, 0.375, 0.375]
        }},
        "thirdperson_lefthand": {{
            "rotation": [75, 45, 0],
            "translation": [0, 2.5, 0],
            "scale": [0.375, 0.375, 0.375]
        }},
        "firstperson_righthand": {{
            "rotation": [0, 45, 0],
            "translation": [0, 0, 0],
            "scale": [0.40, 0.40, 0.40]
        }},
        "firstperson_lefthand": {{
            "rotation": [0, 225, 0],
            "translation": [0, 0, 0],
            "scale": [0.40, 0.40, 0.40]
        }},
        "ground": {{
            "translation": [0, 3, 0],
            "scale": [0.25, 0.25, 0.25]
        }},
        "gui": {{
            "rotation": [30, 225, 0],
            "translation": [0, -1, 0],
            "scale": [1.2, 1.2, 1.2]
        }},
        "fixed": {{
            "translation": [0, 0, 0],
            "scale": [0.5, 0.5, 0.5]
        }}
    }}
}}"""
        with open(os.path.join(item_models_dir, f"{head_type}.json"), "w") as f:
            f.write(item_json)

if __name__ == "__main__":
    generate_steve_head_textures()
    generate_villager_head_textures()
    generate_json_models()
