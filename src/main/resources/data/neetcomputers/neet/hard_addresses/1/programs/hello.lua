-- programs/hello.lua

local args = { ... }

print("Hello from NeetOS!")
if #args > 0 then
    print("You passed: " .. table.concat(args, " "))
end
print("Computer: " .. chip.getMachine() .. "  (" .. chip.getUUID() .. ")")

write("What's your name? ")
local name = read()
if name ~= "" then
    print("Nice to meet you, " .. name .. "!")
end
