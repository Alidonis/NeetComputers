if fs.exists("user/script.lua") then
    file = fs.readAll("user/script.lua")
    bois.createThread(file)
end
while true do
    --sleep(5)
end