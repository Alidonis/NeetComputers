-- os/api.lua
-- Program-facing convenience layer.

local Util = NeetOS.Util

local function currentTerm() return NeetOS.term end
local function currentFs()   return NeetOS.fs end

function _G.print(...)
    local term = currentTerm()
    if not term then return end
    local n = select("#", ...)
    local parts = {}
    for i = 1, n do
        parts[i] = tostring((select(i, ...)))
    end
    term:writeLine(table.concat(parts, "\t"))
end

function _G.write(text)
    local term = currentTerm()
    if not term then return end
    term:write(tostring(text))
    term:redraw()
    local x = term:getCursorPos()
    return x
end

function _G.sleep(seconds)
    seconds = tonumber(seconds) or 0
    local target = chip.getTime() + seconds
    repeat
        Util.yield()
    until chip.getTime() >= target
end

function _G.read(opts)
    local term = currentTerm()
    if not term then return "" end
    local line = Util.readLine(term, opts)
    term:newline()
    term:redraw()
    return line
end

local term = {}

function term.write(text)      currentTerm():write(tostring(text)); currentTerm():redraw() end
function term.writeLine(text)  currentTerm():writeLine(tostring(text)) end
function term.clear()          currentTerm():clear(); currentTerm():redraw() end
function term.clearLine(y)     currentTerm():clearLine(y); currentTerm():redraw() end
function term.getSize()        return currentTerm():getSize() end
function term.getCursorPos()   return currentTerm():getCursorPos() end
function term.setCursorPos(x, y)  currentTerm():setCursorPos(x, y); currentTerm():redraw() end
function term.setCursorBlink(on)  currentTerm():setCursorBlink(on) end
function term.setTextColor(name)       currentTerm():setTextColor(name) end
function term.setBackgroundColor(name) currentTerm():setBackgroundColor(name) end
function term.getTextColor()       return currentTerm():getTextColor() end
function term.getBackgroundColor() return currentTerm():getBackgroundColor() end
function term.scroll(n)  currentTerm():scroll(n); currentTerm():redraw() end
function term.redraw()   currentTerm():redraw() end

_G.term = term

local EVENT_CATEGORIES = { "User", "System", "Network", "Peripheral", "Compatibility", "Unlabeled" }

function event.pull(category, filter)
    while true do
        local first = event.getFirst(category, filter)
        if first then
            return table.unpack(first)
        end
        Util.yield()
    end
end

function event.pullAny(filter)
    while true do
        for _, category in ipairs(EVENT_CATEGORIES) do
            local first = event.getFirst(category, filter)
            if first then
                return table.unpack(first)
            end
        end
        Util.yield()
    end
end

local textutils = {}

function textutils.serialize(value)
    return Util.serialize(value)
end

function textutils.unserialize(text)
    local chunk, err = load("return " .. text, "=unserialize", "t", {})
    if not chunk then return nil, err end
    local ok, result = pcall(chunk)
    if not ok then return nil, result end
    return result
end

function textutils.tabulate(rows)
    local term = currentTerm()
    if not term or not rows or #rows == 0 then return end

    local widths = {}
    for _, row in ipairs(rows) do
        for i, cell in ipairs(row) do
            widths[i] = math.max(widths[i] or 0, #tostring(cell))
        end
    end

    for _, row in ipairs(rows) do
        local parts = {}
        for i, cell in ipairs(row) do
            local s = tostring(cell)
            parts[i] = s .. string.rep(" ", widths[i] - #s)
        end
        term:writeLine(table.concat(parts, "  "))
    end
end

_G.textutils = textutils

local fs = {}

function fs.exists(path)         return currentFs():exists(path) end
function fs.isFile(path)         return currentFs():isFile(path) end
function fs.isDir(path)          return currentFs():isDir(path) end
function fs.list(path)           return currentFs():list(path) end
function fs.makeDir(path)        return currentFs():mkdir(path) end
function fs.delete(path)         return currentFs():delete(path) end
function fs.copy(src, dst)       return currentFs():copy(src, dst) end
function fs.move(src, dst)       return currentFs():move(src, dst) end
function fs.read(path)           return currentFs():readAll(path) end
function fs.write(path, data)    return currentFs():writeAll(path, data) end
function fs.pwd()                return currentFs():pwd() end
function fs.resolve(path)        return currentFs():resolve(path) end
function fs.cd(path)             return currentFs():cd(path) end

function fs.getName(path)
    return path:match("([^/]+)/?$") or path
end

function fs.open(path, mode)
    local target = currentFs():resolve(path)
    return files.open(target, mode or "r", currentFs().disk)
end

_G.fs = fs
_G.os = _G.os or {}

function os.version() return "NeetOS " .. tostring(NeetOS.version) end
os.sleep = sleep

NeetOS.Api = { term = term, textutils = textutils, fs = fs }
return NeetOS.Api
