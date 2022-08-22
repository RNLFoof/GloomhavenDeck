import os
import re
from enum import Enum

originaldir = "originals"
fixeddir = r"..\..\app\src\main\res\raw"

class InsertType(Enum):
    SINGLE = 0
    LIST = 1
    DICT = 2

insertstarts = {
    InsertType.SINGLE: "\n        val {} = SoundBundle(",
    InsertType.LIST: "\n        val {} = SoundBundle(listOf(\n",
    InsertType.DICT: "\n        val {} = SoundBundle(\n            LinkedHashMap(\n                mapOf(\n",
}

insertmids = {
    InsertType.SINGLE: "R.raw.{}",
    InsertType.LIST: "            R.raw.{},\n",
    InsertType.DICT: "                    R.raw.{} to {},\n",
}

insertends = {
    InsertType.SINGLE: ")",
    InsertType.LIST: "        ))",
    InsertType.DICT: "        )))",
}

# Removes things that can't appear in resource names.
def removeundesirables(s: str) -> str:
    s = s.replace(" ", "_")
    s = s.replace("\\", "_")
    s = s.replace("+", "PLUS")
    s = s.replace("-", "MINUS")
    s = re.sub(r"\((0\.\d+)\)", "", s)
    return s

# Extracts the chance/weight from a file/dir name, expressed as (0.n)
def extractchance(fname: str) -> tuple[str, str]:
    if (match := re.search(r"\((0\.\d+)\)", fname)):
        chance = match.group(1) + "f"
        fname = fname.replace(match.group(0), "")
    else:
        chance = "1f"
    return fname, chance

s = ""
for root,dirs,files in os.walk(originaldir, topdown=False):
    if len(dirs) + len(files) == 1:
        it = InsertType.SINGLE
    else:
        if len(set(extractchance(x)[1] for x in dirs+files)) == 1:  # Are all the chances the same?
            it = InsertType.LIST
        else:
            it = InsertType.DICT
    rootsansoriginals = "\\".join(root.split("\\")[1:])

    # Skip the root folder, everything there is unused
    if not rootsansoriginals:
        continue

    currentfolder = root.split("\\")[-1]
    s += insertstarts[it].format(removeundesirables(rootsansoriginals))
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
        s += insertmids[it].format(head, chance)

    for dir in dirs:
        dir, chance = extractchance(dir)
        s += insertmids[it].format(removeundesirables(os.path.join(rootsansoriginals, dir)), chance)

    s += insertends[it]

print(s)