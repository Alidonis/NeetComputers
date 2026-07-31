-- os/keyboard.lua

local Keyboard = {}

Keyboard.MOD_SHIFT = 1
Keyboard.MOD_CTRL  = 2
Keyboard.MOD_ALT   = 4
Keyboard.MOD_SUPER = 8

function Keyboard.hasMod(modifiers, mask)
    modifiers = modifiers or 0
    return math.floor(modifiers / mask) % 2 == 1
end

local SPECIAL = {
    ENTER = "enter", RETURN = "enter", ["NUMPADENTER"] = "enter",
    BACKSPACE = "backspace",
    DELETE = "delete",
    TAB = "tab",
    ESCAPE = "escape",
    LEFT = "left", RIGHT = "right", UP = "up", DOWN = "down",
    HOME = "home", ["END"] = "end",
    PAGEUP = "pageup", PAGEDOWN = "pagedown",
}

local ARROW_CODES = {
    [128] = "left",
    [129] = "right",
    [130] = "up",
    [131] = "down",
}

local held = {}

local function identity(code, letter)
    if letter and letter ~= "" then return "L:" .. letter:upper() end
    return "C:" .. tostring(code)
end

function Keyboard.poll()
    local out = {}

    for _, e in ipairs(event.getQueue("User")) do
        local kind = e[1]
        if kind == "keyReleased" then
            held[identity(e[2], e[3])] = nil
        elseif kind == "keyPressed" then
            local code, letter, modifiers = e[2], e[3], e[4]
            modifiers = modifiers or 0

            held[identity(code, letter)] = true

            local special = letter and letter ~= "" and SPECIAL[letter:upper()]
            if special then
                out[#out + 1] = { kind = "key", key = special, modifiers = modifiers }
            elseif code == 13 or code == 10 then
                out[#out + 1] = { kind = "key", key = "enter", modifiers = modifiers }
            elseif code == 8 then
                out[#out + 1] = { kind = "key", key = "backspace", modifiers = modifiers }
            elseif code == 9 then
                out[#out + 1] = { kind = "key", key = "tab", modifiers = modifiers }
            elseif code == 27 then
                out[#out + 1] = { kind = "key", key = "escape", modifiers = modifiers }
            elseif ARROW_CODES[code] then
                out[#out + 1] = { kind = "key", key = ARROW_CODES[code], modifiers = modifiers }
            elseif type(code) == "number" and code >= 32 and code <= 126 then
                out[#out + 1] = { kind = "char", ch = string.char(code), modifiers = modifiers }
            end
        end
    end
    return out
end

NeetOS.Keyboard = Keyboard
return Keyboard
