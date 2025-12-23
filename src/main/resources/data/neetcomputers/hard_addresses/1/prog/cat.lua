local file2 = {...}
local file1 = file2[1]
local file
if fs.exists(file1) then
	file = file1
elseif fs.exists(_getCurrentShellDir()..file1) then
	file = _getCurrentShellDir()..file1
end
if file then
	local handle = fs.open(file,"r")
	local dat = handle.read("a")
	handle.close()
	term.print(dat)
end