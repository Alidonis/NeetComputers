-- os/shell.lua
-- The NeetOS shell

local Parse    = NeetOS.Parse
local Colors   = NeetOS.Colors
local Util     = NeetOS.Util
local Editor   = NeetOS.Editor
local Config   = NeetOS.Config

local Shell = {}
Shell.__index = Shell

local builtins = {}

function Shell.new(term, fs)
    local self = setmetatable({
        term = term,
        fs = fs,
        history = {},
        historyIndex = nil,
        running = true,
    }, Shell)
    return self
end

function Shell:printPrompt()
    local term = self.term
    local before, after = Config.prompt:match("^(.-)%%cwd%%(.*)$")

    term:setTextColor(Colors.defaultFg)
    if before then
        term:write(before)
        term:setTextColor(Config.theme.prompt or "lime")
        term:write(self.fs:pwd())
        term:setTextColor(Colors.defaultFg)
        term:write(after)
    else
        -- No %cwd% token in a custom prompt; just print it as-is.
        term:write(Config.prompt)
    end

    self.promptCol, self.promptRow = term:getCursorPos()
end

function Shell:readLine()
    return Util.readLine(self.term, { history = self.history })
end

function Shell:tryRunProgram(name, args)
    local candidates = {}
    if name:match("[:/]") then
        candidates[#candidates + 1] = name
    else
        candidates[#candidates + 1] = name
        candidates[#candidates + 1] = name .. ".lua"
        for _, dir in ipairs(Config.path) do
            local prefix = dir:sub(-1) == "/" and dir or (dir .. "/")
            candidates[#candidates + 1] = prefix .. name .. ".lua"
        end
    end

    for _, candidate in ipairs(candidates) do
        if self.fs:exists(candidate) and self.fs:isFile(candidate) then
            local src, err = self.fs:readAll(candidate)
            if not src then
                self.term:writeLine("neetos: " .. tostring(err))
                return true
            end
            local chunk, cerr = load(src, "=" .. candidate)
            if not chunk then
                self.term:writeLine("neetos: syntax error: " .. tostring(cerr))
                return true
            end
            local ok, runErr = pcall(chunk, table.unpack(args))
            if not ok then
                self.term:writeLine("neetos: " .. tostring(runErr))
            end
            return true
        end
    end
    return false
end

function Shell:execute(line)
    local tokens = Parse.tokenize(line)
    if #tokens == 0 then return end

    local name = table.remove(tokens, 1)

    local seen = {}
    while Config.aliases[name] and not seen[name] do
        seen[name] = true
        local expansion = Parse.tokenize(Config.aliases[name])
        if #expansion == 0 then break end
        name = table.remove(expansion, 1)
        for i = #expansion, 1, -1 do
            table.insert(tokens, 1, expansion[i])
        end
    end

    local handler = builtins[name]
    if handler then
        local ok, err = pcall(handler, self, tokens)
        if not ok then
            self.term:writeLine("neetos: " .. tostring(err))
        end
        return
    end

    if not self:tryRunProgram(name, tokens) then
        self.term:writeLine(name .. ": command not found")
    end
end

function Shell:run()
    local term = self.term
    term:writeLine("NeetOS")
    term:writeLine("Type 'help' for a list of commands.")
    term:redraw()

    while self.running do
        self:printPrompt()
        local line = self:readLine()
        term:newline()

        line = Util.trim(line)
        if line ~= "" then
            if self.history[#self.history] ~= line then
                self.history[#self.history + 1] = line
                if #self.history > Config.historySize then
                    table.remove(self.history, 1)
                end
            end
            self:execute(line)
        end
        term:redraw()
    end
end

builtins["help"] = function(shell, args)
    local term = shell.term
    local names = {}
    for name in pairs(builtins) do names[#names + 1] = name end
    table.sort(names)
    term:writeLine("Builtin commands:")
    local cols = term:getSize()
    local line = "  "
    for _, name in ipairs(names) do
        if #line + #name + 2 > cols then
            term:writeLine(line)
            line = "  "
        end
        line = line .. name .. "  "
    end
    if line ~= "  " then term:writeLine(line) end
    term:writeLine("Run a .lua file from disk with: run <path>")
end

builtins["clear"] = function(shell)
    shell.term:clear()
end

builtins["pwd"] = function(shell)
    shell.term:writeLine(shell.fs:pwd())
end

builtins["cd"] = function(shell, args)
    local ok, err = shell.fs:cd(args[1] or "/")
    if not ok then shell.term:writeLine("cd: " .. tostring(err)) end
end

builtins["ls"] = function(shell, args)
    local list, err = shell.fs:list(args[1])
    if not list then
        shell.term:writeLine("ls: " .. tostring(err))
        return
    end
    table.sort(list)
    if #list == 0 then
        shell.term:writeLine("(empty)")
        return
    end
    for _, entry in ipairs(list) do
        local full = (args[1] and (args[1] .. "/") or "") .. entry
        local suffix = shell.fs:isDir(full) and "/" or ""
        shell.term:writeLine("  " .. entry .. suffix)
    end
end

builtins["mkdir"] = function(shell, args)
    if not args[1] then shell.term:writeLine("usage: mkdir <path>"); return end
    if not shell.fs:mkdir(args[1]) then
        shell.term:writeLine("mkdir: failed to create " .. args[1])
    end
end

builtins["rm"] = function(shell, args)
    if not args[1] then shell.term:writeLine("usage: rm <path>"); return end
    if not shell.fs:delete(args[1]) then
        shell.term:writeLine("rm: failed to delete " .. args[1])
    end
end

builtins["cp"] = function(shell, args)
    if not args[1] or not args[2] then shell.term:writeLine("usage: cp <src> <dst>"); return end
    local ok, err = shell.fs:copy(args[1], args[2])
    if not ok then shell.term:writeLine("cp: " .. tostring(err)) end
end

builtins["mv"] = function(shell, args)
    if not args[1] or not args[2] then shell.term:writeLine("usage: mv <src> <dst>"); return end
    local ok, err = shell.fs:move(args[1], args[2])
    if not ok then shell.term:writeLine("mv: " .. tostring(err)) end
end

builtins["cat"] = function(shell, args)
    if not args[1] then shell.term:writeLine("usage: cat <path>"); return end
    local data, err = shell.fs:readAll(args[1])
    if not data then
        shell.term:writeLine("cat: " .. tostring(err))
        return
    end
    shell.term:write(data)
    if data:sub(-1) ~= "\n" then shell.term:newline() end
end

builtins["edit"] = function(shell, args)
    if not args[1] then shell.term:writeLine("usage: edit <path>"); return end
    local target = shell.fs:resolve(args[1])
    Editor.run(shell.term, shell.fs, target)
    shell.term:clear()
    shell.term:writeLine("NeetOS")
end

builtins["run"] = function(shell, args)
    local path = table.remove(args, 1)
    if not path then shell.term:writeLine("usage: run <path> [args...]"); return end
    if not shell:tryRunProgram(path, args) then
        shell.term:writeLine("run: no such program: " .. path)
    end
end

builtins["echo"] = function(shell, args)
    shell.term:writeLine(table.concat(args, " "))
end

builtins["history"] = function(shell)
    for i, cmd in ipairs(shell.history) do
        shell.term:writeLine(string.format("%3d  %s", i, cmd))
    end
end

builtins["time"] = function(shell)
    shell.term:writeLine(string.format("uptime:    %.1fs", chip.getTime()))
    shell.term:writeLine(string.format("unix time: %d", math.floor(chip.getUnixTime())))
    shell.term:writeLine(string.format("lunar time: %d", chip.getLunarTime()))
end

builtins["disks"] = function(shell)
    local n = files.getNumberOfDisks() or 0
    if n == 0 then
        shell.term:writeLine("no disks attached")
        return
    end
    for d = 0, n - 1 do
        local ok, id = pcall(files.getDiskID, d)
        shell.term:writeLine(string.format("disk %d  id=%s", d, ok and tostring(id) or "?"))
    end
end

builtins["disk"] = function(shell, args)
    if not args[1] then
        shell.term:writeLine("current disk: " .. shell.fs.disk)
        return
    end
    local n = tonumber(args[1])
    if not n then shell.term:writeLine("usage: disk <number>"); return end
    shell.fs:setDisk(math.floor(n))
    shell.term:writeLine("switched to disk " .. n .. ", cwd is now " .. shell.fs:pwd())
end

builtins["eject"] = function(shell, args)
    local n = tonumber(args[1])
    if not n then shell.term:writeLine("usage: eject <disk number>"); return end
    if not files.removeDisk(math.floor(n)) then
        shell.term:writeLine("eject: failed (home disk can't be ejected)")
    end
end

builtins["partitions"] = function(shell)
    local raw = files.getPartitions(shell.fs.disk) or {}
    local parts = raw[1] or raw
    for _, entry in ipairs(parts) do
        local info, name
        if type(entry) == "table" then
            info, name = entry, entry.name
        else
            name = entry
            info = files.getPartition(name, shell.fs.disk) or {}
        end
        local flags = {}
        if info.readonly then flags[#flags + 1] = "readonly" end
        if info.hidden then flags[#flags + 1] = "hidden" end
        shell.term:writeLine("  " .. tostring(name) .. (#flags > 0 and (" (" .. table.concat(flags, ", ") .. ")") or ""))
    end
end

builtins["mkpart"] = function(shell, args)
    if not args[1] then shell.term:writeLine("usage: mkpart <name>"); return end
    if not files.createPartition(args[1], shell.fs.disk) then
        shell.term:writeLine("mkpart: could not create partition " .. args[1])
    end
end

builtins["rmpart"] = function(shell, args)
    if not args[1] then shell.term:writeLine("usage: rmpart <name>"); return end
    if not files.deletePartition(args[1], shell.fs.disk) then
        shell.term:writeLine("rmpart: could not delete partition " .. args[1])
    end
end

builtins["bootinfo"] = function(shell)
    local path = files.getBootPath(shell.fs.disk)
    shell.term:writeLine(path and ("boot entrypoint: " .. path) or "disk is not bootable")
end

builtins["setboot"] = function(shell, args)
    if not args[1] then shell.term:writeLine("usage: setboot <entrypoint path>"); return end
    if not files.setBoot(args[1], shell.fs.disk) then
        shell.term:writeLine("setboot: failed (path must exist on the home disk)")
    end
end

builtins["peripherals"] = function(shell)
    local ids = io.getPeripherals() or {}
    if #ids == 0 then
        shell.term:writeLine("no peripherals attached")
        return
    end
    for _, id in ipairs(ids) do
        local tag = io.getTag(id)
        shell.term:writeLine(string.format("  %s  [%s]%s", id, io.getType(id),
            (tag and tag ~= "") and (" tag=" .. tag) or ""))
    end
end

builtins["lua"] = function(shell, args)
    local expr = table.concat(args, " ")
    if expr == "" then
        shell.term:writeLine("usage: lua <expression>")
        return
    end
    local chunk = load("return " .. expr) or load(expr)
    if not chunk then
        shell.term:writeLine("lua: syntax error")
        return
    end
    local ok, result = pcall(chunk)
    if not ok then
        shell.term:writeLine("lua: " .. tostring(result))
    elseif type(result) == "table" then
        shell.term:writeLine(Util.serialize(result))
    elseif result ~= nil then
        shell.term:writeLine(tostring(result))
    end
end

builtins["dump"] = function(shell, args)
    local expr = table.concat(args, " ")
    if expr == "" then
        shell.term:writeLine("usage: dump <expression>  (like lua, but always serializes the result)")
        return
    end
    local chunk, cerr = load("return " .. expr)
    if not chunk then
        shell.term:writeLine("dump: syntax error: " .. tostring(cerr))
        return
    end
    local ok, result = pcall(chunk)
    if not ok then
        shell.term:writeLine("dump: " .. tostring(result))
        return
    end
    shell.term:writeLine(Util.serialize(result))
end

builtins["shutdown"] = function(shell)
    shell.term:writeLine("Shutting down...")
    shell.term:redraw()
    chip.shutdown()
end

builtins["reboot"] = function(shell)
    chip.reboot()
end

builtins["exit"] = function(shell)
    shell.running = false
end

builtins["alias"] = function(shell, args)
    if #args == 0 then
        local names = {}
        for name in pairs(Config.aliases) do names[#names + 1] = name end
        table.sort(names)
        if #names == 0 then
            shell.term:writeLine("no aliases set")
            return
        end
        for _, name in ipairs(names) do
            shell.term:writeLine(name .. " = " .. Config.aliases[name])
        end
        return
    end

    local spec = table.concat(args, " ")
    local name, value = spec:match("^(%S+)%s*=%s*(.+)$")
    if not name then
        shell.term:writeLine("usage: alias name=command  (or `alias` alone to list)")
        return
    end
    Config.aliases[name] = value
end

builtins["unalias"] = function(shell, args)
    if not args[1] then shell.term:writeLine("usage: unalias <name>"); return end
    if not Config.aliases[args[1]] then
        shell.term:writeLine("unalias: no such alias: " .. args[1])
        return
    end
    Config.aliases[args[1]] = nil
end

NeetOS.Shell = Shell
return Shell
