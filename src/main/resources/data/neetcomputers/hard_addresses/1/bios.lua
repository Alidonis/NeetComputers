--this nightmare of a "operating system" is brought to you by the hacked together remains of my failed attempt to make a working one

--load the font, should only fail is the file is modified or corrupted
local success = true
local drawChar1
local didWork
print("if log suppression wasnt enabled in settings this would be in the game console!")
if files.exists("bios:/font.lua") then
	local fontFile = files.open("bios:/font.lua","r")
	local fontDat = fontFile.read("a")
	fontFile.close()
	local fontProg = load(fontDat,"font")
	if fontProg then
		didWork, drawChar1 = pcall(fontProg)
		if not didWork then
			success = false
		end
	else
		success = false
	end
else 
	success = false
end

--stop boot process if it doesnt load the font
if not success then
	chip.crash("font failed to load")
end

--draws the character formatted correctly at the X,Y in the terminal
local sizeX,sizeY = screen.getSize()
local termSizeX, termSizeY = math.floor((sizeX-1)/5)*5, math.floor((sizeY-1)/6)*6
local topX, topY = (sizeX/2)-(termSizeX/2), (sizeY/2)-(termSizeY/2)
local function drawChar(x,y,c)
	screen.fill(topX+((x-1)*5),topY+((y-1)*6),topX+((x-1)*5)+5,topY+((y-1)*6)+6,0,0,0)
	drawChar1(topX+((x-1)*5)+1,topY+((y-1)*6)+1,c,255,192,0)
end

--draws a cute little graphic to the screen
local projector = require("projector")
projector.clear()
projector.drawLine(2,2,2,3)
projector.drawLine(4,2,4,3)
projector.drawLine(2,5,4,6)
projector.draw()

--write the speech
speech = [[
Welcome to NeetComputers beta!

Bad news, if you dont have access to your world file the mod is basicly bricked for you.

You see, there is no os, i (the dev) know how to make java do my bidding but im pathetic at lua.

But if you do, its your lucky day!

Go to https://www.red-toast.net/NeetDocumentation/ to read how to make your own! (or how to just write normal software)

The website has full documentation, and a guide or two.

Happy camping and good luck, and message me (redtoastneedsbutter on discord) if you make something cool, i want to see that you people make.

Also if you make a os and it has MIT liscencing, you can send it to me and i might bundle it with the mod along with any others that i think are good.
]]
local x = 1
local y = 1
--i copyed this from google, your welcome for my excelence
for i = 1, #speech do
    -- get substring of 1 chracter
    local c = speech:sub(i,i)
    -- print char
    if c=="\n" then
		y = y + 1
		x = 1
    else
		drawChar(x, y, c)
		x = x + 1
		if x == termSizeX/5 then
			drawChar(x,y,"-")
			y = y + 1
			x = 1
		end
    end
end
x = 1
signature = "Thanks for trying my mod, Red Toast!"
for i = 1, #signature do
	drawChar(x,termSizeY/6,signature:sub(i,i))
	x = x + 1
end
screen.draw()

--loop to keep the thread alive to the computer doesnt shut down
while true do end