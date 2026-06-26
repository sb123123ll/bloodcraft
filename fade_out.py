import numpy as np
from scipy.io import wavfile

# 由于我们没有 pydub 或 ffmpeg，我们可以用 numpy + scipy 自己写一个淡出逻辑
# 由于 scipy 只能读取 wav，我们需要用户保证输入是 wav 或者我们使用其他手段
# 但是因为你指定的是 mp3 文件，我们需要将音频转换
# 幸运的是，Python 的 soundfile 也不能直接写 mp3，但是它能处理很多音频格式。
