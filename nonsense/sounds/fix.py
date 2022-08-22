import os
import re

originaldir = "originals"
fixeddir = r"..\..\app\src\main\res\raw"

def removeundesirables(s: str) -> str:
    s = s.replace(" ", "_")
    s = s.replace("\\", "_")
    s = re.sub(r"\((0\.\d+)\)", "", s)
    return s

def extractchance(fname: str) -> tuple[str, str]:
    if (match := re.search(r"\((0\.\d+)\)", fname)):
        chance = match.group(1) + "f"
        fname = fname.replace(match.group(0), "")
    else:
        chance = "1f"
    return fname, chance

for root,dirs,files in os.walk(originaldir, topdown=False):
    rootsansoriginals = "\\".join(root.split("\\")[1:])

    if not rootsansoriginals:
        continue

    currentfolder = root.split("\\")[-1]
    s = f"        val {removeundesirables(rootsansoriginals)} = SoundBundle(\n            LinkedHashMap(\n                mapOf(\n"
    for originalfilename in files:
        # Handle resource
        newfilename, chance = extractchance(originalfilename)
        newfilename = removeundesirables(os.path.join(rootsansoriginals, newfilename)).lower()
        print(originalfilename)
        os.system(f'ffmpeg -i "{os.path.join(root, originalfilename)}"  -af silenceremove=1:0:-50dB -af loudnorm -vn -ar 44100 -ac 2 -b:a 192k LOL.mp3')
        if os.path.exists(os.path.join(fixeddir, newfilename)):
            os.remove(os.path.join(fixeddir, newfilename))
        os.rename("LOL.mp3", os.path.join(fixeddir, newfilename))

        # Handle SoundBundles
        head, ext = os.path.splitext(newfilename)
        s += f"                    R.raw.{head} to {chance},\n"

    for dir in dirs:
        dir, chance = extractchance(dir)
        s += f"                    {removeundesirables(os.path.join(rootsansoriginals, dir))} to {chance},\n"

    s += "        )))"
    print(s)