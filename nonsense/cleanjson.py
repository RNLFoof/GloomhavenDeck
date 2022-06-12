import json
import os
dir = r"..\app\src\main\res\json"

for file in os.listdir(dir):
    fullpath = os.path.join(dir, file)
    with open(fullpath) as f:
        data = json.load(f)
    with open(fullpath, 'w') as f:
        json.dump(data, f, indent=2)
