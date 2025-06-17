drive = peripherals.locate("file system")
file = drive.open("neetos:startup.lua")
local success, response = pcall(chip.createThread,file.read("a"))
file.close()
if not (success) then
    print("os missing somehow, seriously what did you do"..response)
end