if fs.exists("user/script.lua") then
    file = fs.readAll("user/script.lua")
    local success, response = pcall(chip.createThread,file)
    if not (success) then
        print("Failed to start user startup! err="..response)
    end
end
while true do
end