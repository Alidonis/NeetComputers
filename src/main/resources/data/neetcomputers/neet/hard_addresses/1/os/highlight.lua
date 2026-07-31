-- os/highlight.lua
-- Extensible per-line syntax highlighting for the editor.

local Colors = NeetOS.Colors

local Highlight = {}

local tokenizers = {}

local function plainTokenizer(line)
    return { { text = line, color = Colors.defaultFg } }
end

function Highlight.register(ext, tokenizer)
    tokenizers[ext:lower()] = tokenizer
end

function Highlight.for_(path)
    local ext = path and path:match("%.([%w_]+)$")
    if ext then
        local tokenizer = tokenizers[ext:lower()]
        if tokenizer then return tokenizer end
    end
    return plainTokenizer
end

local LUA_KEYWORDS = {
    ["and"] = true, ["break"] = true, ["do"] = true, ["else"] = true,
    ["elseif"] = true, ["end"] = true, ["false"] = true, ["for"] = true,
    ["function"] = true, ["goto"] = true, ["if"] = true, ["in"] = true,
    ["local"] = true, ["nil"] = true, ["not"] = true, ["or"] = true,
    ["repeat"] = true, ["return"] = true, ["then"] = true, ["true"] = true,
    ["until"] = true, ["while"] = true,
}

local function luaTokenizer(line)
    local spans = {}
    local i, n = 1, #line

    local function push(text, color)
        if text ~= "" then
            spans[#spans + 1] = { text = text, color = color }
        end
    end

    while i <= n do
        local c = line:sub(i, i)

        if c == "-" and line:sub(i, i + 1) == "--" then
            push(line:sub(i), "gray")
            break
        elseif c == '"' or c == "'" then
            local quote = c
            local start = i
            i = i + 1
            while i <= n and line:sub(i, i) ~= quote do
                if line:sub(i, i) == "\\" then i = i + 1 end
                i = i + 1
            end
            if i <= n then
                push(line:sub(start, i), "lime")
                i = i + 1
            else
                push(line:sub(start, n), "lime")
            end
        elseif c:match("%d") then
            local start = i
            while i <= n and line:sub(i, i):match("[%w%.]") do i = i + 1 end
            push(line:sub(start, i - 1), "yellow")
        elseif c:match("[%a_]") then
            local start = i
            while i <= n and line:sub(i, i):match("[%w_]") do i = i + 1 end
            local word = line:sub(start, i - 1)
            push(word, LUA_KEYWORDS[word] and "lightBlue" or Colors.defaultFg)
        else
            push(c, Colors.defaultFg)
            i = i + 1
        end
    end

    return spans
end

Highlight.register("lua", luaTokenizer)

NeetOS.Highlight = Highlight
return Highlight
