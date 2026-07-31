-- os/config.lua
-- Central place for user-tunable NeetOS settings.
--
-- You can override any defaults by dropping a bios:/os/neetos.cfg file:
--
--   {
--       theme = { fg = "lime", bg = "black", prompt = "cyan" },
--       path = { "bios:/programs", "home:/bin" },
--       aliases = { ll = "ls", cls = "clear" },
--       historySize = 500,
--       prompt = "%cwd% $ ",
--       editor = { tabWidth = 4 },
--   }

local Colors = NeetOS.Colors

local Config = {
    prompt = "%cwd%> ",
    theme = {
        fg     = Colors.defaultFg,
        bg     = Colors.defaultBg,
        prompt = "lime",
    },
    path = { "bios:/programs" },
    aliases = {},
    historySize = 200,
    editor = {
        tabWidth = 2,
    },
}

local CONFIG_PATH = "bios:/os/neetos.cfg"

local function deepMerge(defaults, overrides)
    for k, v in pairs(overrides) do
        if k == "path" then
            defaults[k] = v
        elseif type(v) == "table" and type(defaults[k]) == "table" then
            deepMerge(defaults[k], v)
        else
            defaults[k] = v
        end
    end
end

local function parseConfigTable(source)
    local chunk, err = load("return " .. source, "=" .. CONFIG_PATH, "t", {})
    if not chunk then return nil, err end
    local ok, result = pcall(chunk)
    if not ok then return nil, result end
    if type(result) ~= "table" then return nil, "config must be a table" end
    return result
end

function Config.load(fs)
    if not fs:exists(CONFIG_PATH) then return true end

    local source, readErr = fs:readAll(CONFIG_PATH)
    if not source then return false, readErr end

    local overrides, err = parseConfigTable(source)
    if not overrides then return false, err end

    deepMerge(Config, overrides)

    if Colors.palette[Config.theme.fg] then Colors.defaultFg = Config.theme.fg end
    if Colors.palette[Config.theme.bg] then Colors.defaultBg = Config.theme.bg end

    return true
end

NeetOS.Config = Config
return Config
