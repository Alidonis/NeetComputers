-- programs/clock.lua

local Keyboard = NeetOS.Keyboard
local cols, rows = term.getSize()
local wasBlink = NeetOS.term.blinkOn
term.setCursorBlink(false)

local function draw()
    term.clear()
    local mid = math.floor(rows / 2)
    local uptime = string.format("uptime: %.0fs", chip.getTime())
    local unix = string.format("unix:   %d", math.floor(chip.getUnixTime()))
    local hint = "(press any key to exit)"

    term.setCursorPos(math.max(1, math.floor((cols - #uptime) / 2) + 1), mid - 1)
    term.write(uptime)
    term.setCursorPos(math.max(1, math.floor((cols - #unix) / 2) + 1), mid)
    term.write(unix)
    term.setCursorPos(math.max(1, math.floor((cols - #hint) / 2) + 1), mid + 2)
    term.write(hint)
end

local running = true
while running do
    draw()
    -- Wait up to a second, but bail out early the moment a key is pressed.
    local waited = 0
    while waited < 1 do
        if #Keyboard.poll() > 0 then
            running = false
            break
        end
        sleep(0.05)
        waited = waited + 0.05
    end
end

term.setCursorBlink(wasBlink)
term.clear()
print("NeetOS")
