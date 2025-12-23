------------------------Basic text-based BIOS for NEET Computers------------------------
--Provides a monochrome terminal emulator and functions useful for text-based programs--
------------------------------------Made by WindClan------------------------------------

--make it a global because its currently apart of the borked require system
if not fs then
	_G.fs = require("fs") 
end

--function to draw a crash glyph
local function drawSadComputer()
	local x,y = screen.getSize()
	local posX,posY = (x/2)-50,(y/2)-50
	
	--computer
	screen.fill(posX+14,posY+14,posX+86,posY+86,25,25,25) --base
	screen.fill(posX+20,posY+20,posX+80,posY+75,0,0,200) --screen
	screen.fill(posX+20,posY+79,posX+30,posY+83,0,100,0) --light
	
	--eyes
	screen.fill(posX+40,posY+30,posX+41,posY+48,255,255,255) --left eye
	screen.fill(posX+59,posY+30,posX+60,posY+48,255,255,255) --right eye
	
	--mouth
	screen.fill(posX+43,posY+55,posX+57,posY+56,255,255,255)
	screen.fill(posX+40,posY+57,posX+43,posY+58,255,255,255)
	screen.fill(posX+57,posY+57,posX+60,posY+58,255,255,255)
	screen.fill(posX+37,posY+59,posX+40,posY+60,255,255,255)
	screen.fill(posX+60,posY+59,posX+63,posY+60,255,255,255)
	
	screen.draw()
end

--load the font, should only fail is the file is modified or corrupted
local success = true
local drawChar1
local didWork
print("Loading font...")
if fs.exists("bios:/font.lua") then
	local fontFile = fs.open("bios:/font.lua","r")
	local fontDat = fontFile.read("a")
	fontFile.close()
	print("Loaded font file")
	local fontProg = load(fontDat,"font")
	if fontProg then
		didWork, drawChar1 = pcall(fontProg)
		if not didWork then
			print(drawChar1)
			success = false
		end
	else
		success = false
	end
	print("Font loaded!")
else 
	success = false
end

--stop boot process if it doesnt load the font
if not success then
	drawSadComputer()
	while true do end
end

--initialize the screen table
local sizeX,sizeY = screen.getSize()
local termSizeX, termSizeY = math.floor((sizeX-1)/5)*5, math.floor((sizeY-1)/6)*6
local topX, topY = (sizeX/2)-(termSizeX/2), (sizeY/2)-(termSizeY/2)
local termTable = {}
for i=1,termSizeY/6 do
	local a = {}
	for i1=1,termSizeX/5 do
		table.insert(a," ")
	end
	table.insert(termTable,a)
end


--draws the character formatted correctly at the X,Y in the terminal
local function drawChar(x,y,c)
	screen.fill(topX+((x-1)*5),topY+((y-1)*6),topX+((x-1)*5)+5,topY+((y-1)*6)+6,0,0,0)
	drawChar1(topX+((x-1)*5)+1,topY+((y-1)*6)+1,c,255,192,0)
end

--initialize the term API
local termX,termY = 1,1
local term = {}
term.getSize = function() return termSizeX/5,termSizeY/6 end
term.getPos = function() return termX,termY end
term.setPos = function(x,y) 
	if x > 0 and x <= termSizeX/5 then
		termX = x
	end
	if y > 0 and y <= termSizeY/6 then
		termY = y
	end
end
term.write = function(s) 
	for i = 1, #s do
		termTable[termY][termX] = s:sub(i,i)
		drawChar(termX,termY,s:sub(i,i))
		if termX == termSizeX then
			return
		else
			termX = termX + 1
		end
	end
end
term.scroll = function(amount)
	if not amount then
		amount = 1
	end
	for _=1,amount do
		for i,v in pairs(termTable) do
			if i ~= 1 then
				termTable[i-1] = v
				if i == termSizeY/6 then
					local a = {}
					for i1=1,termSizeX/5 do
						table.insert(a," ")
					end
					termTable[termSizeY/6] = a
				end
			end
		end
	end
	for y,tab in pairs(termTable) do
		for x,c in pairs(tab) do
			drawChar(x,y,c)
		end
	end
end
term.clear = function()
	for i,v in pairs(termTable) do
		for _,v1 in pairs(v) do
			v = " "
		end
	end
	screen.fill(1,1,sizeX,sizeY,0,0,0)
	screen.draw()
end
term.print = function(s)
	s = tostring(s)
	for i = 1, #s do
		local curChar = s:sub(i,i)
		if not curChar:match '%c' and curChar ~= "\r" then
			termTable[termY][termX] = curChar
			drawChar(termX,termY,curChar)
		end
		if termX == termSizeX/5 or curChar == "\n" then
			termX = 1
			if termY == termSizeY/6 then
				term.scroll()
			else
				termY = termY + 1
			end
		else
			termX = termX + 1
		end
	end
	termX = 1
	if termY == termSizeY/6 then
		term.scroll()
	else
		termY = termY + 1
	end
	screen.draw()
end
_G.term = term

screen.fill(1,1,sizeX,sizeY,0,0,0)
screen.draw()

for y,tab in pairs(termTable) do
	for x,c in pairs(tab) do
		drawChar(x,y,c)
	end
end
screen.draw()

--needed because event handelers seem to be async, making things like "read()" impossible without a wrapper
local _KeyPressEvent,_KeyPressEvent1 = nil,nil
function event.getKeyPressed()
	_KeyPressEvent = nil
	while not _KeyPressEvent do coroutine.yield() end
	return table.unpack(_KeyPressEvent)
end
event.registerEvent("keyPressed",function(_,keyCode,keyLetter)
	_KeyPressEvent = {keyCode,keyLetter}
end)

--implement lua's "read()" command in a way that works with the terminal emulator
_G.read = function(hidden)
	local str = ""
	local doThing = true
	while doThing do
		local keyCode,letter = event.getKeyPressed()
		if letter then
			str=str..letter
			if not hidden then
				term.write(letter)
			else
				term.write(hidden)
			end
		elseif keyCode == 13 then --enter key
			doThing = false
		elseif keyCode == 8 and str ~= "" then --backspace key
			local x,y = term.getPos()
			term.setPos(x-1,y)
			term.write(" ")
			term.setPos(x-1,y)
			str = str:sub(1,#str-1)
		end
		screen.draw()
	end
	local _,y = term.getPos()
	local _,sizeY = term.getSize()
	if y == sizeY then
		term.scroll()
		term.setPos(1,sizeY)
	else
		term.setPos(1,1+y)
	end
	
	return str
end

--add a smiley face to the front of the computer
--serves no purpose besides showing the computer is on
local projector = require("projector")
local frontX, frontY = projector.getSize()
if frontY > 5 then
	local offsetX, offsetY = math.floor(frontX/2)-2.5, math.floor(frontY/2+0.5)-3
	projector.drawPixel(1+offsetX,2+offsetY)
	projector.drawPixel(2+offsetX,1+offsetY)
	projector.drawPixel(3+offsetX,1+offsetY)
	projector.drawPixel(4+offsetX,1+offsetY)
	projector.drawPixel(5+offsetX,2+offsetY)
	
	projector.drawPixel(2+offsetX,4+offsetY)
	projector.drawPixel(2+offsetX,5+offsetY)
	projector.drawPixel(2+offsetX,6+offsetY)

	projector.drawPixel(4+offsetX,4+offsetY)
	projector.drawPixel(4+offsetX,5+offsetY)
	projector.drawPixel(4+offsetX,6+offsetY)
elseif frontY > 3 then
	projector.drawPixel(2,frontY-1)
	projector.drawPixel(3,frontY-2)
	projector.drawPixel(2,frontY-3)
end
projector.draw()

--actually boot the stuff
term.print("Loading shell file...")
if fs.exists("bios:/shell.lua") then
	local osFile = fs.open("bios:/shell.lua","r")
	local osDat = osFile.read("a")
	osFile.close()
	term.print("Loading shell program...")
	local shellProg = load(osDat,"shell")
	if shellProg then
		term.print("Loaded shell program!")
		term.print("Starting shell...")
		local worked,err = pcall(shellProg)
		if not worked then
			drawSadComputer()
			term.print("***FATAL SHELL ERROR***")
			term.print(err)
		end
	else
		drawSadComputer()
		term.print("Failed to load the shell!")
		term.print("Either the file is invalid or there was a syntax error present.")
	end
else
	drawSadComputer()
	term.print("Shell not found! Is the mod correctly installed?")
end


--fallback loop so it doesnt immedietly turn off after erroring
while true do end