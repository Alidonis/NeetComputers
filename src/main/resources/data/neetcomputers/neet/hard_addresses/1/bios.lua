-- bios.lua

local DISK = 0

_G.NeetOS = _G.NeetOS or { version = "0.3.0" }

local function loadFile(path)
    local header, err = files.open(path, "r", DISK)
    if not header then
        error("cannot open " .. path .. ": " .. tostring(err))
    end
    local source = header.read("a")
    header.close()

    local chunk, cerr = load(source, "=" .. path)
    if not chunk then
        error("syntax error in " .. path .. ": " .. tostring(cerr))
    end
    return chunk()
end

local searchPaths = {
    "bios:/os/?.lua",
    "bios:/?.lua",
    "bios:/programs/?.lua",
}

local moduleCache = {}
local LOADING = {}

local function resolveModulePath(name)
    if type(name) ~= "string" or name == "" then
        return nil
    end
    if name:match("^%a[%w_%-]*:/") then
        return name
    end

    local rel = name:gsub("%.", "/")
    for _, template in ipairs(searchPaths) do
        local candidate = template:gsub("%?", rel)
        if files.exists(candidate, DISK) then
            return candidate
        end
    end
    return nil
end

function _G.require(name)
    local path = resolveModulePath(name)
    if not path then
        error(string.format(
            "module '%s' not found (searched %s)",
            tostring(name), table.concat(searchPaths, ", ")), 2)
    end

    local cached = moduleCache[path]
    if cached ~= nil then
        if cached == LOADING then
            error("circular require detected for '" .. tostring(name) ..
                "' (" .. path .. ")", 2)
        end
        return cached
    end

    local header, openErr = files.open(path, "r", DISK)
    if not header then
        error("cannot open module '" .. tostring(name) .. "' (" .. path ..
            "): " .. tostring(openErr), 2)
    end
    local source = header.read("a")
    header.close()

    local chunk, cerr = load(source, "=" .. path)
    if not chunk then
        error("syntax error in module '" .. tostring(name) .. "' (" .. path ..
            "): " .. tostring(cerr), 2)
    end

    moduleCache[path] = LOADING
    local ok, result = pcall(chunk, name, path)
    if not ok then
        moduleCache[path] = nil
        error("error loading module '" .. tostring(name) .. "': " .. tostring(result), 2)
    end

    if result == nil then result = true end
    moduleCache[path] = result
    return result
end

local function tryRunStartup(term)
    local STARTUP_PATH = "bios:/startup.lua"
    if not files.exists(STARTUP_PATH, DISK) then return end

    local ok, err = pcall(loadFile, STARTUP_PATH)
    if not ok then
        term:writeLine("startup.lua: " .. tostring(err))
        term:redraw()
    end
end

local ok, err = pcall(function()
    require("util")
    require("colors")
    require("config")
    require("keyboard")
    require("parse")
    require("fs")
    require("term")
    require("highlight")
    require("edit")
    require("api")
    require("shell")

    local fs = NeetOS.Fs.new()
    local cfgOk, cfgErr = NeetOS.Config.load(fs)

    local psf = require("psf")
    local font, fontErr = psf.open("bios:/assets/default8x9.psf")
    if not font then
        error("could not load font: " .. tostring(fontErr))
    end
    font:setSpacing(1)

    local term = NeetOS.Term.new(font)
    term:setCursorBlink(true)

    local shell = NeetOS.Shell.new(term, fs)
    NeetOS.term, NeetOS.fs, NeetOS.shell, NeetOS.font = term, fs, shell, font

    event.clear("User")

    if not cfgOk then
        term:writeLine("neetos.cfg: " .. tostring(cfgErr))
        term:redraw()
    end

    tryRunStartup(term)

    shell:run()
end)

if not ok then
    chip.crash("NeetOS boot failed: " .. tostring(err))
end
