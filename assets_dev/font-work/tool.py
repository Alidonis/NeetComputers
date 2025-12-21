from PIL import Image
import os

img = Image.open(os.getcwd()+"\\Downloads\\font-work\\pico_8_codex.png").convert("RGBA")

width, height = img.size
print(width, height)
pointerX = 0
pointerY = 0
place = 0
print(img.mode)

#return codes
#0 - empty bit
#1 - full bit
#2 - push horizontal buffer
#3 - push horizontal then vertical buffer
#4 - push horizontal then vertical buffers and stop reading
def readPlace():
    global pointerX
    global pointerY
    global place
    global img
    if (pointerX>=width):
        R, G, B, A = 0, 0, 0, 0
    else:
        R, G, B, A = img.getpixel((pointerX, pointerY))
    if (A<100):
        if (pointerY >= height - 1):
            pointerY = 0
            pointerX += 1
            if (pointerX>=width): return 4
            place = pointerX
            return 3
        else:
            pointerX = place
            pointerY += 1
            return 2
    else:
        pointerX += 1
        if sum((R,G,B)) > 150: 
            return 0 
        else: 
            return 1

symbols = list()
verticalBuffer = list()
horizontalBuffer = list()
while (True):
    code = readPlace()
    if (code<=1):
        horizontalBuffer.append(code)
    if (code>=2):
        verticalBuffer.append(horizontalBuffer.copy())
        horizontalBuffer.clear()
    if (code>=3):
        symbols.append(verticalBuffer.copy())
        verticalBuffer.clear()
    if (code==4):
        break

newline = True
output = "{"
for symbol in symbols:
    output += "\n{"
    for line in symbol:
        output += "\n{"
        for bit in line:
            output += ["flase", "true"][bit] + ","
        output = output[0:len(output)-1]+"},"
    output = output[0:len(output)-1]+"},"
output = output[0:len(output)-1]+"}"
print(output)