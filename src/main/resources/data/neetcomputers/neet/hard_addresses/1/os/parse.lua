-- os/parse.lua
-- Splits a shell command line into arguments

local Parse = {}

function Parse.tokenize(line)
    local tokens = {}
    local i, n = 1, #line

    local function skipSpaces()
        while i <= n and line:sub(i, i):match("%s") do i = i + 1 end
    end

    skipSpaces()
    while i <= n do
        local quote = line:sub(i, i)
        local tok
        if quote == '"' or quote == "'" then
            i = i + 1
            local start = i
            while i <= n and line:sub(i, i) ~= quote do i = i + 1 end
            tok = line:sub(start, i - 1)
            i = i + 1
        else
            local start = i
            while i <= n and not line:sub(i, i):match("%s") do i = i + 1 end
            tok = line:sub(start, i - 1)
        end
        tokens[#tokens + 1] = tok
        skipSpaces()
    end

    return tokens
end

NeetOS.Parse = Parse
return Parse
