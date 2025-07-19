local fs = peripherals.locate("file system")

function parse_bdf(filename)
    local glyphs = {}
    local file = assert(fs.open(filename, "r"))
    local line = file.read("l")
    local current = nil
    local in_bitmap = false

    while line do
        if line:match("^STARTCHAR") then
            current = {
                name = line:match("^STARTCHAR%s+(.+)")
            }
        elseif current and line:match("^ENCODING") then
            current.encoding = tonumber(line:match("^ENCODING%s+(%d+)"))
        elseif current and line:match("^DWIDTH") then
            current.width = tonumber(line:match("^DWIDTH%s+(%d+)"))
        elseif current and line:match("^BBX") then
            local w, h = line:match("^BBX%s+(%d+)%s+(%d+)")
            current.height = tonumber(h)
            -- width from BBX is ignored, DWIDTH is used
        elseif current and line:match("^BITMAP") then
            current.bitmap = {}
            in_bitmap = true
        elseif in_bitmap then
            if line:match("^ENDCHAR") then
                -- Store by ASCII character if printable, else by name
                local charname = string.char(current.encoding)
                if current.encoding >= 32 and current.encoding <= 126 then
                    glyphs[charname] = {
                        encoding = current.encoding,
                        width = current.width,
                        height = current.height,
                        bitmap = current.bitmap
                    }
                else
                    glyphs[current.name] = {
                        encoding = current.encoding,
                        width = current.width,
                        height = current.height,
                        bitmap = current.bitmap
                    }
                end
                current = nil
                in_bitmap = false
            else
                table.insert(current.bitmap, tonumber(line, 16))
            end
        end
        line = file.read("l")
    end

    file.close()
    return glyphs
end


local glyphs = parse_bdf("bios:ibm.bdf")
for k, v in pairs(glyphs) do
    print(k, v.encoding, v.width, v.height)
    for _, row in ipairs(v.bitmap) do
        print(string.format("%02X", row))
    end
end
