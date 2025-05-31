print("a")
print("b")
print("c")
print("17")
print("17")
print("17")
print("17")
for k, v in pairs(fs) do
    print(k, v)
  end
print(fs)
print("17")
print("file",fs.exists("user/script.lua"))
print("d")
if fs.exists("user/script.lua") then
    print(2)
    file = fs.readAll("user/script.lua")
    local success, response = pcall(chip.createThread,file)
    if not (success) then
        print("Failed to start user startup! err="..response)
    end
end
print(3)
while true do
    --sleep(5)
end