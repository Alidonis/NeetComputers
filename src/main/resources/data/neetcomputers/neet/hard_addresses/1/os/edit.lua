-- os/edit.lua
-- A small full-screen text editor.

local Keyboard   = require("keyboard")
local Colors     = require("colors")
local Util       = require("util")
local Highlight  = require("highlight")
local Config     = require("config")

local Editor = {}

local function loadLines(fs, path)
    if fs:exists(path) then
        local data = fs:readAll(path) or ""
        local lines = Util.split(data, "\n")
        if #lines == 0 then lines = { "" } end
        return lines
    end
    return { "" }
end

function Editor.run(term, fs, path)
    local lines = loadLines(fs, path)
    local row, col = 1, 1
    local top = 1
    local dirty = false
    local status = "Ctrl+S save  Ctrl+Q quit"
    local highlightLine = Highlight.for_(path)

    local cols, rows = term:getSize()
    local viewRows = rows - 1

    term:clear()

    local function clampCursor()
        row = math.max(1, math.min(row, #lines))
        col = math.max(1, math.min(col, #lines[row] + 1))
    end

    local function ensureVisible()
        if row < top then top = row end
        if row > top + viewRows - 1 then top = row - viewRows + 1 end
        if top < 1 then top = 1 end
    end

    local function gutterWidth()
        return math.max(3, #tostring(#lines)) + 1
    end

    local function render()
        local gutter = gutterWidth()
        local textCols = math.max(1, cols - gutter)

        for i = 0, viewRows - 1 do
            local lineNo = top + i
            local text = lines[lineNo]
            term:setCursorPos(1, i + 1)

            if text then
                local numStr = string.format("%" .. (gutter - 1) .. "d", lineNo)
                term:setTextColor(Colors.lightGray)
                term:write(numStr)
                term:write(" ")
            else
                term:setTextColor(Colors.defaultFg)
                term:write(string.rep(" ", gutter))
            end
            text = text or ""

            local written = 0
            for _, span in ipairs(highlightLine(text)) do
                if written >= textCols then break end
                local remaining = textCols - written
                local chunk = span.text
                if #chunk > remaining then
                    chunk = chunk:sub(1, remaining)
                end
                term:setTextColor(span.color)
                term:write(chunk)
                written = written + #chunk
            end

            term:setTextColor(Colors.defaultFg)
            if written < textCols then
                term:write(string.rep(" ", textCols - written))
            end
        end

        term:setCursorPos(1, rows)
        term:setTextColor("black")
        term:setBackgroundColor("lightGray")
        local label = string.format(" %s%s  Ln %d, Col %d  |  %s",
            path, dirty and " *" or "", row, col, status)
        term:write(label:sub(1, cols) .. string.rep(" ", math.max(0, cols - #label)))
        term:setTextColor(Colors.defaultFg)
        term:setBackgroundColor(Colors.defaultBg)

        term:setCursorPos(gutter + col, row - top + 1)
        term:redraw()
    end

    local function save()
        fs:writeAll(path, table.concat(lines, "\n"))
        dirty = false
        status = "saved"
    end

    render()
    while true do
        Util.yield()
        local redrawNeeded = term:updateBlink()

        local inputs = Keyboard.poll()
        for _, ev in ipairs(inputs) do
            redrawNeeded = true
            if ev.kind == "char" then
                if Keyboard.hasMod(ev.modifiers, Keyboard.MOD_CTRL) and ev.ch == "s" then
                    save()
                elseif Keyboard.hasMod(ev.modifiers, Keyboard.MOD_CTRL) and ev.ch == "q" then
                    return
                else
                    local line = lines[row]
                    lines[row] = line:sub(1, col - 1) .. ev.ch .. line:sub(col)
                    col = col + 1
                    dirty = true
                    status = "Ctrl+S save  Ctrl+Q quit"
                end
            elseif ev.kind == "key" then
                if ev.key == "escape" then
                    return
                elseif ev.key == "enter" then
                    local line = lines[row]
                    local before, after = line:sub(1, col - 1), line:sub(col)
                    lines[row] = before
                    table.insert(lines, row + 1, after)
                    row = row + 1
                    col = 1
                    dirty = true
                elseif ev.key == "backspace" then
                    if col > 1 then
                        local line = lines[row]
                        lines[row] = line:sub(1, col - 2) .. line:sub(col)
                        col = col - 1
                        dirty = true
                    elseif row > 1 then
                        local prevLen = #lines[row - 1]
                        lines[row - 1] = lines[row - 1] .. lines[row]
                        table.remove(lines, row)
                        row = row - 1
                        col = prevLen + 1
                        dirty = true
                    end
                elseif ev.key == "left" then
                    if col > 1 then col = col - 1
                    elseif row > 1 then row = row - 1; col = #lines[row] + 1 end
                elseif ev.key == "right" then
                    if col <= #lines[row] then col = col + 1
                    elseif row < #lines then row = row + 1; col = 1 end
                elseif ev.key == "up" then
                    if row > 1 then row = row - 1 end
                elseif ev.key == "down" then
                    if row < #lines then row = row + 1 end
                elseif ev.key == "home" then
                    col = 1
                elseif ev.key == "end" then
                    col = #lines[row] + 1
                elseif ev.key == "tab" then
                    local width = Config.editor.tabWidth or 2
                    local pad = string.rep(" ", width)
                    local line = lines[row]
                    lines[row] = line:sub(1, col - 1) .. pad .. line:sub(col)
                    col = col + width
                    dirty = true
                end
            end
        end

        clampCursor()
        if redrawNeeded then
            ensureVisible()
            render()
        end
    end
end

NeetOS.Editor = Editor
return Editor
