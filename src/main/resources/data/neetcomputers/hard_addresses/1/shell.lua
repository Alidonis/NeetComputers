-------Basic text-based shell for NEET computers------
-----Generic minimal shell for NEET's beta release----
--Based on code I wrote for a CC shell two years ago--
-------------------Made by WindClan-------------------

term.clear()
term.setPos(1,1)
term.print("* NEET Default Shell *")

local dir = "user:/" --TODO: working change dir command with coresponding path resolving exposed function
local defaults = {
	clr = function()
		term.clear()
		term.setPos(1,1)
	end,
	rm = function(path)
		if path == nil then
			term.print("No such file")
			return
		end
		if fs.exists(path) then
			fs.delete(path)
		else
			term.print("No such file")
		end
	end,
	ls = function(dir1)
		if not dir1 then
			dir1 = dir
		end
		if not fs.exists(dir1) then
			term.print("A valid directory is required!")
			return
		end
		local maxX = term.getSize()
		local lines = {}
		local current = 1
		for _,v in pairs(fs.getChildren(dir1)) do
			if not lines[current] then
				lines[current] = ""
			end
			if v:sub(1,1) ~= "." then
				if #lines[current] + #v > maxX then
					current = current + 1
					lines[current] = ""
				end
				lines[current] = lines[current] .. v .. " "
			end
		end
		for i,v in pairs(lines) do
			term.print(v)
		end
	end,
	--this exists only as a stop-gap solution for there being no proper edit command
	--please only use this if its absolutely needed
	tw = function(fileName,shouldAppend)
		if not fileName then
			printError("A file name is required!")
			return
		end
		local mode = "w"
		if shouldAppend and (shouldAppend:lower() == "append" or shouldAppend:lower() == "a") then
			mode = "a"
		end
		local a = fs.open(dir..fileName,mode)
		local edit = true
		local size = term.getSize()
		while edit do
			local keyCode, keyLetter = event.getKeyPressed()
			if keyLetter then
				local pos = term.getPos()
				if pos <= size then
					term.write(keyLetter)
					a.write(keyLetter)
				end
			else
				if b == 13 then
					a.write("\n")
					term.print("")
					a.flush()
				elseif b == keys.rightCtrl then
					term.print("")
					edit = false
				elseif b == 8 then
					local posX,posY = term.getPos()
					if posX ~= 1 then
						term.setPos(posX-1,posY)
						a.seek("cur",-1)
					end
				end
			end
		end
		a.close()
	end
}

--Internal (but exposed) shell APIs, either exposed because they are useful
--or they help get internal shell variables some programs may need (ex. dir)
_G._getCurrentShellDir = function()
	return dir
end
_G._setCurrentShellDir = function(dir1)
	dir = dir1
end
 _G._shellResolveProgName = function(name)
	if fs.exists(dir..name) then
		return dir..name
	elseif fs.exists(dir..name..".lua") then
		return dir..name..".lua"
	elseif fs.exists("bios:/prog/"..name..".lua") then
		return "bios:/prog/"..name..".lua"
	elseif fs.exists("bios:/prog/"..name) then
		return "bios:/prog/"..name
	elseif fs.exists(name) then
		return name
	end
end

--Main program loop
while true do
	term.write(">")
	screen.draw()
	
	local str = read()
	
	local split = {}
	for a in str:gmatch("([^%s]+)") do --taken from https://stackoverflow.com/a/7615129
		table.insert(split, a)
	end
	
	if split[1] then --only show the illegal command notice if something was typed
		local name = split[1]
		local prog
		if defaults[name] then
			prog = defaults[name]
		elseif _shellResolveProgName(name) then
			local path = _shellResolveProgName(name)
			local handle = fs.open(path,"r")
			local dat = handle.read("a")
			handle.close()
			prog = load(dat,path) --syntax errors can cause this to return nil
		end
		if not prog then
			term.print("Illegal command: "..name)
		elseif prog then
			table.remove(split,1)
			local worked,err = pcall(prog,table.unpack(split))
			if not worked then
				term.print(err)
			end
		end
	end
end