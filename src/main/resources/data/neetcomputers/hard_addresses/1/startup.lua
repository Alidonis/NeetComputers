local fs = peripherals.locate("file system")
if fs.exists("user:script.lua") then
    file = fs.open("user:script.lua")
    local success, response = pcall(chip.createThread,file.read("a"))
    if not (success) then
        print("Failed to start user startup! err="..response)
    end
    file.close()
end