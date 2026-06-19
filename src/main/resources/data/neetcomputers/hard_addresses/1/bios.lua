--load the import library, should only fail if the file is modified or corrupted
local success = true
local import
local didWork
print("if log suppression wasnt enabled in settings this would be in the game console!")
if files.exists("bios:/sys/import.lua") then
    local impFile = files.open("bios:/sys/import.lua","r")
    local dat = impFile.read("a")
    impFile.close()
    local implib = load(dat,"importlib")
    if implib then
        didWork, import = pcall(implib)
        if not didWork then
            success = false
        end
    else
        success = false
    end
else
    success = false
end

if not success then
    chip.crash("Failed to load import library! Was the system modified?")
end

_G.import = import

--draws a cute little graphic to the screen
local projector = require("projector")
projector.clear()
projector.drawLine(2,2,2,3)
projector.drawLine(4,2,4,3)
projector.drawLine(2,5,4,6)
projector.draw()

local vterm = import("bios:/sys/vterm.lua")
_G.term = vterm

term.print("Init finished! Loading shell...")

import("bios:/sys/cmd.lua")()

--loop to keep the thread alive if the computer doesnt shut down
while true do end