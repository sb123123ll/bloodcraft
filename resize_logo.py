import os
import sys

try:
    from PIL import Image
except ImportError:
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "Pillow"])
    from PIL import Image

def resize_psd():
    input_path = r"g:\myfirstmod\bloodcraft\.github\agents\bloodcraft_logo.psd"
    output_path = r"g:\myfirstmod\bloodcraft\.github\agents\bloodcraft_logo_512.png"

    try:
        # 打开 PSD 文件
        with Image.open(input_path) as img:
            print(f"Original image size: {img.size}, format: {img.format}, mode: {img.mode}")
            
            # 使用 NEAREST (最近邻插值) 来放大像素画，这样可以保证像素边缘绝对锐利，不会发虚
            # Image.Resampling.NEAREST is used in newer Pillow, Image.NEAREST for older versions
            resample_filter = getattr(Image, 'Resampling', Image).NEAREST
            
            resized_img = img.resize((512, 512), resample_filter)
            
            # PSD读取有时可能有特殊通道，转成 RGBA 保存为 PNG
            if resized_img.mode != "RGBA":
                resized_img = resized_img.convert("RGBA")
                
            resized_img.save(output_path, "PNG")
            print(f"Successfully resized and saved to {output_path}")
            
    except Exception as e:
        print(f"Failed to resize image: {e}")
        # 如果 PIL 无法处理这个 PSD，可能需要安装 psd-tools
        if "cannot identify image file" in str(e) or "PSD" in str(e):
            print("Attempting to use psd-tools...")
            import subprocess
            subprocess.check_call([sys.executable, "-m", "pip", "install", "psd-tools"])
            from psd_tools import PSDImage
            
            psd = PSDImage.open(input_path)
            composite = psd.composite()
            resample_filter = getattr(Image, 'Resampling', Image).NEAREST
            resized_img = composite.resize((512, 512), resample_filter)
            resized_img.save(output_path, "PNG")
            print(f"Successfully resized with psd-tools and saved to {output_path}")

if __name__ == "__main__":
    resize_psd()