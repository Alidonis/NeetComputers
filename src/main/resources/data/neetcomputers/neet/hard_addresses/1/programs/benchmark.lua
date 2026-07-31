--[[
  SpLuabench.lua
  A single file benchmark for Lua 5.5.
]]

local SCALE = tonumber(({...})[1] or (arg and arg[1])) or 1.0
if SCALE <= 0 then SCALE = 1.0 end

local function scaled(n)
  local v = math.floor(n * SCALE)
  if v < 1 then v = 1 end
  return v
end

local results = {}
local grand_total = 0
local current_section = ""

local function section(title)
  current_section = title
  print("\n   " .. title)
end

local REPEATS = tonumber(({...})[2] or (arg and arg[2])) or 5

local function bench(name, fn, ...)
  local ok, a, b = pcall(fn, ...)
  if not ok then
    print(string.format("  [FAIL] %-38s %9s    %s\n", name, "--", tostring(a)))
    results[#results + 1] = { name = name, time = -1, ok = false, info = tostring(a), section = current_section, ops = nil }
    return
  end

  local best = math.huge
  for _ = 1, REPEATS do
    local t0 = chip.getUnixTime()
    local ok2, a2, b2 = pcall(fn, ...)
    local elapsed = chip.getUnixTime() - t0
    if not ok2 then
      print(string.format("  [FAIL] %-38s %9s    %s\n", name, "--", tostring(a2)))
      results[#results + 1] = { name = name, time = -1, ok = false, info = tostring(a2), section = current_section, ops = nil }
      return
    end
    if elapsed < best then
      best = elapsed
      a, b = a2, b2
    end
  end

  grand_total = grand_total + best
  local info = (a ~= nil) and tostring(a) or ""
  print(string.format("  [ OK ] %-38s %9.4f s   %s\n", name, best, info))
  results[#results + 1] = { name = name, time = best, ok = true, info = info, section = current_section, ops = b }
end

local REFERENCE_OPS_PER_SEC = {
  ["integer loop sum"]                                           = 98522167,
  ["float loop (sin/cos/sqrt)"]                                  = 4023605,
  ["integer/float division & modulo"]                            = 16465423,
  ["exponentiation"]                                             = 16764459,
  ["bitwise ops & integer wraparound"]                           = 26737968,
  ["string concat via table.concat"]                             = 1690617,
  ["string.format heavy"]                                        = 759878,
  ["pattern matching (gmatch)"]                                  = 470219,
  ["gsub substitution"]                                          = 1244813,
  ["string.find plain vs pattern"]                               = 9009009,
  ["string.pack / string.unpack"]                                = 2729754,
  ["string.rep / byte / char roundtrip"]                         = 7137759,
  ["utf8 iteration & offset (5.5: final position)"]              = 569801,
  ["array insert (append) growth"]                               = 29673591,
  ["table.create pre-sized array (5.5)"]                         = 49504950,
  ["table.sort (random data)"]                                   = 27665387,
  ["table.sort with comparator"]                                 = 10056905,
  ["hash table string keys"]                                     = 581125,
  ["nested table construction (JSON-like)"]                      = 934579,
  ["table.remove (shift left)"]                                  = 145553787,
  ["table.move"]                                                 = 24366472,
  ["table.pack / table.unpack"]                                  = 2421308,
  ["weak table GC interaction"]                                  = 6954103,
  ["naive recursive fibonacci"]                                  = 28568250,
  ["memoized fibonacci via closure"]                             = 83045,
  ["tail-call accumulation loop"]                                = 28368794,
  ["closures capturing upvalues (counters)"]                     = 16420361,
  ["varargs sum"]                                                = 2516779,
  ["OOP inheritance + method dispatch"]                          = 7153076,
  ["operator overloading via metamethods (vectors)"]             = 3223727,
  ["__index function metamethod (proxy/lazy table)"]             = 18382353,
  ["producer/consumer coroutine pipeline"]                       = 6535948,
  ["nested coroutines"]                                          = 598802,
  ["goto-based loop restructuring"]                              = 33990483,
  ["pcall/error handling overhead"]                              = 2592913,
  ["to-be-closed <close> variables"]                             = 6038647,
  ["named vararg table (function f(... name))"]                  = 4163197,
  ["explicit global declarations (global / global<const>)"]      = 37453184,
  ["read-only for-loop control variable check"]                  = 416667,
  ["GC churn: incremental mode"]                                 = 1215067,
  ["GC churn: generational mode"]                                = 1038781,
  ["full collection cycle timing"]                               = 5277045,
  ["quicksort (custom, recursive)"]                              = 16878458,
  ["sieve of Eratosthenes"]                                      = 31982849,
  ["matrix multiplication (NxN)"]                                = 23535109,
}

local function read_file(path)
  local ok, f = pcall(io.open, path, "r")
  if not ok or not f then return nil end
  local content = f:read("*a")
  f:close()
  return content
end

local function popen_line(cmd)
  if not io.popen then return nil end
  local ok, p = pcall(io.popen, cmd)
  if not ok or not p then return nil end
  local out = p:read("*l")
  p:close()
  return out
end

local function get_system_info()
  local info = {}

  info.uname = popen_line("uname -srmo") or popen_line("uname -a")

  local cpuinfo = read_file("/proc/cpuinfo")
  if cpuinfo then
    info.cpu_model = cpuinfo:match("model name%s*:%s*([^\n]+)")
      or cpuinfo:match("Hardware%s*:%s*([^\n]+)")
      or cpuinfo:match("Processor%s*:%s*([^\n]+)")
    local cores = 0
    for _ in cpuinfo:gmatch("\nprocessor%s*:") do cores = cores + 1 end
    if cores > 0 then info.cpu_cores = cores end
  end
  if not info.cpu_cores then
    local n = popen_line("nproc")
    info.cpu_cores = n and tonumber(n)
  end

  local meminfo = read_file("/proc/meminfo")
  if meminfo then
    local kb = meminfo:match("MemTotal:%s*(%d+)")
    if kb then info.mem_gb = string.format("%.1f", tonumber(kb) / 1048576) end
  end

  return info
end

local sysinfo = get_system_info()

local function print_system_info()
  print("System info:")
  print("  Lua version : " .. (_VERSION or "unknown"))
  if sysinfo.uname then print("  OS/kernel   : " .. sysinfo.uname) end
  if sysinfo.cpu_model then print("  CPU model   : " .. sysinfo.cpu_model:gsub("^%s+", "")) end
  if sysinfo.cpu_cores then print("  CPU cores   : " .. sysinfo.cpu_cores) end
  if sysinfo.mem_gb then print("  RAM total   : " .. sysinfo.mem_gb .. " GB") end
  if sysinfo.uname == nil and sysinfo.cpu_model == nil then
    print("  (uname/proc unavailable in this sandbox -- no further detail)")
  end
end

print("SpLuabench using Lua " .. (_VERSION or "?"))
print("scale = " .. SCALE .. "   repeats = " .. REPEATS .. "   date/time = " .. chip.getUnixTime())
print_system_info()

section("Arithmetic")

bench("integer loop sum", function()
  local N = scaled(20000000)
  local s = 0
  for i = 1, N do
    s = s + i
  end
  return s, N
end)

bench("float loop (sin/cos/sqrt)", function()
  local N = scaled(1500000)
  local s = 0.0
  for i = 1, N do
    local x = i * 0.0001
    s = s + math.sin(x) * math.cos(x) + math.sqrt(x + 1.0)
  end
  return string.format("%.4f", s), N
end)

bench("integer/float division & modulo", function()
  local N = scaled(3000000)
  local isum, fsum = 0, 0.0
  for i = 1, N do
    isum = isum + (i // 7) + (i % 7)
    fsum = fsum + (i / 7.0)
  end
  return isum, N
end)

bench("exponentiation", function()
  local N = scaled(2000000)
  local s = 0.0
  for i = 1, N do
    s = s + (1.0001) ^ (i % 100)
  end
  return string.format("%.2f", s), N
end)

bench("bitwise ops & integer wraparound", function()
  local N = scaled(3000000)
  local x = 0xFFFFFFFF
  for i = 1, N do
    x = (x ~ i) & 0xFFFFFFFF
    x = (x << 1 | x >> 63)
  end

  local ok = (math.maxinteger + 1 == math.mininteger)
  return "wrap_ok=" .. tostring(ok), N
end)

section("Strings")

bench("string concat via table.concat", function()
  local N = scaled(200000)
  local parts = {}
  for i = 1, N do
    parts[i] = tostring(i)
  end
  local s = table.concat(parts, ",")
  return #s, N
end)

bench("string.format heavy", function()
  local N = scaled(150000)
  local buf = {}
  for i = 1, N do
    buf[i] = string.format("%05d:%8.3f:%s", i, i * 0.5, (i % 2 == 0) and "even" or "odd")
  end
  return #buf, N
end)

bench("pattern matching (gmatch)", function()
  local N = scaled(30000)
  local sample = "The quick brown fox jumps over the lazy dog, 12345 times!"
  local words = 0
  for i = 1, N do
    for w in sample:gmatch("%a+") do
      words = words + 1
    end
  end
  return words, N
end)

bench("gsub substitution", function()
  local N = scaled(30000)
  local sample = "hello world, hello lua, hello benchmark"
  local total = 0
  for i = 1, N do
    local r, n = sample:gsub("hello", "goodbye")
    total = total + n
  end
  return total, N
end)

bench("string.find plain vs pattern", function()
  local N = scaled(200000)
  local haystack = string.rep("abcdefghij", 50) .. "NEEDLE"
  local hits = 0
  for i = 1, N do
    if haystack:find("NEEDLE", 1, true) then hits = hits + 1 end
  end
  return hits, N
end)

bench("string.pack / string.unpack", function()
  local N = scaled(300000)
  local total = 0
  for i = 1, N do
    local packed = string.pack("i4d", i, i * 1.5)
    local a, b = string.unpack("i4d", packed)
    total = total + a
  end
  return total, N
end)

bench("string.rep / byte / char roundtrip", function()
  local N = scaled(1000000)
  local s = string.rep("x", 8)
  local total = 0
  for i = 1, N do
    local b = string.byte(s, 1)
    local c = string.char(b)
    total = total + #c
  end
  return total, N
end)

section("Tables")

bench("array insert (append) growth", function()
  local N = scaled(1000000)
  local t = {}
  for i = 1, N do
    t[#t + 1] = i
  end
  return #t, N
end)

bench("table.create pre-sized array (5.5)", function()
  local N = scaled(1000000)
  local t = (table.create and table.create(N)) or {}
  for i = 1, N do
    t[i] = i * 2
  end
  return #t, N
end)

bench("table.sort (random data)", function()
  local N = scaled(300000)
  local t = {}
  math.randomseed(42)
  for i = 1, N do
    t[i] = math.random(1, 1000000)
  end
  table.sort(t)
  return t[1] .. ".." .. t[#t], N * math.log(N, 2)
end)

bench("table.sort with comparator", function()
  local N = scaled(200000)
  local t = {}
  for i = 1, N do
    t[i] = { key = (N - i), val = i }
  end
  table.sort(t, function(a, b) return a.key < b.key end)
  return t[1].key .. ".." .. t[#t].key, N * math.log(N, 2)
end)

bench("hash table string keys", function()
  local N = scaled(500000)
  local t = {}
  for i = 1, N do
    t["key_" .. i] = i * i
  end
  local sum = 0
  for i = 1, N do
    sum = sum + t["key_" .. i]
  end
  return sum, N
end)

bench("nested table construction (JSON-like)", function()
  local N = scaled(50000)
  local root = { items = {} }
  for i = 1, N do
    root.items[i] = { id = i, name = "item" .. i, tags = { "a", "b", "c" }, meta = { active = (i % 2 == 0) } }
  end
  return #root.items, N
end)

bench("table.remove (shift left)", function()
  local N = scaled(8000)
  local t = {}
  for i = 1, N do t[i] = i end
  local removed = 0
  while #t > 0 do
    table.remove(t, 1)
    removed = removed + 1
  end
  return removed, N * N
end)

bench("table.move", function()
  local N = scaled(5000000)
  local src = {}
  for i = 1, N do src[i] = i end
  local dst = {}
  table.move(src, 1, N, 1, dst)
  return dst[N], N
end)

bench("table.pack / table.unpack", function()
  local N = scaled(200000)
  local total = 0
  for i = 1, N do
    local packed = table.pack(i, i + 1, i + 2)
    local a, b, c = table.unpack(packed, 1, packed.n)
    total = total + a + b + c
  end
  return total, N
end)

bench("weak table GC interaction", function()
  local N = scaled(1000000)
  local weak = setmetatable({}, { __mode = "k" })
  for i = 1, N do
    weak[{}] = i
  end
  local count = 0
  for _ in pairs(weak) do count = count + 1 end
  return "surviving=" .. count, N
end)

section("Functions & Closures")

local fib_call_count = 0
local function fib_naive(n)
  fib_call_count = fib_call_count + 1
  if n < 2 then return n end
  return fib_naive(n - 1) + fib_naive(n - 2)
end

bench("naive recursive fibonacci", function()
  local n = scaled(28)
  if n > 32 then n = 32 end
  fib_call_count = 0
  local r = fib_naive(n)
  return r, fib_call_count
end)

bench("memoized fibonacci via closure", function()
  local N = scaled(12000)
  local last
  for rep = 1, N do
    local cache = {}
    local fib
    fib = function(n)
      if n < 2 then return n end
      local c = cache[n]
      if c then return c end
      local r = fib(n - 1) + fib(n - 2)
      cache[n] = r
      return r
    end
    last = fib(90)
  end
  return last, N
end)

bench("tail-call accumulation loop", function()
  local N = scaled(2000000)
  local function loop(i, acc)
    if i > N then return acc end
    return loop(i + 1, acc + i)
  end
  return loop(1, 0), N
end)

bench("closures capturing upvalues (counters)", function()
  local N = scaled(2000000)
  local function makeCounter()
    local n = 0
    return function() n = n + 1; return n end
  end
  local counters = {}
  for i = 1, 100 do counters[i] = makeCounter() end
  local total = 0
  for i = 1, N do
    total = total + counters[(i % 100) + 1]()
  end
  return total, N
end)

bench("varargs sum", function()
  local function vsum(...)
    local s = 0
    local n = select("#", ...)
    for i = 1, n do
      s = s + select(i, ...)
    end
    return s
  end
  local N = scaled(300000)
  local total = 0
  for i = 1, N do
    total = total + vsum(1, 2, 3, 4, 5)
  end
  return total, N
end)

section("Metatables & OOP")

local Animal = {}
Animal.__index = Animal
function Animal.new(name, sound)
  return setmetatable({ name = name, sound = sound }, Animal)
end
function Animal:speak()
  return self.name .. " says " .. self.sound
end

local Dog = setmetatable({}, { __index = Animal })
Dog.__index = Dog
function Dog.new(name)
  local self = Animal.new(name, "Woof")
  return setmetatable(self, Dog)
end
function Dog:fetch()
  return self.name .. " fetches the ball"
end

bench("OOP inheritance + method dispatch", function()
  local N = scaled(500000)
  local d = Dog.new("Rex")
  local total = 0
  for i = 1, N do
    local s = d:speak()
    total = total + #s
  end
  return total, N
end)

bench("operator overloading via metamethods (vectors)", function()
  local Vec = {}
  Vec.__index = Vec
  Vec.__add = function(a, b) return setmetatable({ x = a.x + b.x, y = a.y + b.y }, Vec) end
  Vec.__tostring = function(v) return string.format("(%g,%g)", v.x, v.y) end
  local N = scaled(500000)
  local acc = setmetatable({ x = 0, y = 0 }, Vec)
  local step = setmetatable({ x = 1, y = 2 }, Vec)
  for i = 1, N do
    acc = acc + step
  end
  return tostring(acc), N
end)

bench("__index function metamethod (proxy/lazy table)", function()
  local N = scaled(1500000)
  local proxy = setmetatable({}, {
    __index = function(t, k) return k * k end,
  })
  local total = 0
  for i = 1, N do
    total = total + proxy[i % 1000]
  end
  return total, N
end)

section("Coroutines")

bench("producer/consumer coroutine pipeline", function()
  local N = scaled(200000)
  local producer = coroutine.wrap(function()
    for i = 1, N do
      coroutine.yield(i)
    end
  end)
  local total = 0
  for i = 1, N do
    total = total + producer()
  end
  return total, N
end)

bench("nested coroutines", function()
  local function inner()
    for i = 1, 5 do coroutine.yield(i * i) end
  end
  local N = scaled(30000)
  local total = 0
  for r = 1, N do
    local co = coroutine.create(inner)
    while true do
      local ok, v = coroutine.resume(co)
      if coroutine.status(co) == "dead" then break end
      total = total + v
    end
  end
  return total, N
end)

section("Control Flow & Errors")

bench("goto-based loop restructuring", function()
  local N = scaled(5000000)
  local i = 0
  local sum = 0
  ::top::
  i = i + 1
  if i > N then goto done end
  if i % 2 == 0 then goto continue1 end
  sum = sum + i
  ::continue1::
  goto top
  ::done::
  return sum, N
end)

bench("pcall/error handling overhead", function()
  local N = scaled(300000)
  local caught = 0
  for i = 1, N do
    local ok, err = pcall(function()
      if i % 7 == 0 then error("boom " .. i) end
      return i
    end)
    if not ok then caught = caught + 1 end
  end
  return "caught=" .. caught, N
end)

bench("to-be-closed <close> variables", function()
  local N = scaled(500000)
  local closed_count = 0
  local Resource = {}
  Resource.__close = function(self) closed_count = closed_count + 1 end
  Resource.__index = Resource
  for i = 1, N do
    do
      local r <close> = setmetatable({}, Resource)
    end
  end
  return "closed=" .. closed_count, N
end)

section("Lua 5.5 New Language Features")

bench("named vararg table (function f(... name))", function()
  local function statsNamed(... args)
    local sum, count = 0, args.n
    for i = 1, count do sum = sum + args[i] end
    return sum, count
  end
  local N = scaled(1500000)
  local total, cnt = 0, 0
  for i = 1, N do
    local s, n = statsNamed(1, 2, 3, 4, 5, 6, 7)
    total = total + s
    cnt = cnt + n
  end
  return "total=" .. total .. " lastcount=" .. cnt, N
end)

bench("explicit global declarations (global / global<const>)", function()
  local N = scaled(2000000)
  local code = string.format([[
    global X
    global <const> Y = 42
    X = 0
    for i = 1, %d do
      X = X + i
    end
    return X, Y
  ]], N)
  local f, loaderr = load(code, "global_decls_test", "t", setmetatable({}, { __index = _G }))
  if not f then error(loaderr) end
  local x, y = f()
  return "X=" .. tostring(x) .. " Y=" .. tostring(y), N
end)

bench("read-only for-loop control variable check", function()
  local N = scaled(60000)
  local code = "for i = 1, 10 do i = i + 1 end"
  local rejected = true
  for i = 1, N do
    local f = load(code, "readonly_forvar_test")
    if f ~= nil then rejected = false end
  end
  return "correctly_rejected=" .. tostring(rejected), N
end)

section("Classic Algorithms")

local function quicksort(t, lo, hi)
  lo = lo or 1
  hi = hi or #t
  if lo < hi then
    local pivot = t[hi]
    local i = lo - 1
    for j = lo, hi - 1 do
      if t[j] <= pivot then
        i = i + 1
        t[i], t[j] = t[j], t[i]
      end
    end
    t[i + 1], t[hi] = t[hi], t[i + 1]
    quicksort(t, lo, i)
    quicksort(t, i + 2, hi)
  end
  return t
end

bench("quicksort (custom, recursive)", function()
  local N = scaled(80000)
  math.randomseed(7)
  local t = {}
  for i = 1, N do t[i] = math.random(1, 1000000) end
  quicksort(t)
  return t[1] .. ".." .. t[#t], N * math.log(N, 2)
end)

bench("sieve of Eratosthenes", function()
  local N = scaled(1000000)
  local sieve = {}
  local count = 0
  for i = 2, N do
    if not sieve[i] then
      count = count + 1
      for j = i * i, N, i do
        sieve[j] = true
      end
    end
  end
  return "primes<=" .. N .. " = " .. count, N * math.log(math.log(N))
end)

bench("matrix multiplication (NxN)", function()
  local N = scaled(180)
  if N < 2 then N = 2 end
  local function newMat(n, fill)
    local m = {}
    for i = 1, n do
      m[i] = {}
      for j = 1, n do
        m[i][j] = fill and (i + j) or 0
      end
    end
    return m
  end
  local A, B = newMat(N, true), newMat(N, true)
  local C = newMat(N, false)
  for i = 1, N do
    for j = 1, N do
      local s = 0
      for k = 1, N do
        s = s + A[i][k] * B[k][j]
      end
      C[i][j] = s
    end
  end
  return C[N][N], N * N * N
end)

section("Summary")

local ok_count, fail_count = 0, 0
for _, r in ipairs(results) do
  if r.ok then ok_count = ok_count + 1 else fail_count = fail_count + 1 end
end

print(string.format("Tests run:    %d", #results))
print(string.format("Passed:       %d", ok_count))
print(string.format("Failed:       %d", fail_count))
print(string.format("Total time:   %.4f s (sum of passing test wall/CPU times)", grand_total))
print(string.format("Lua version:  %s", _VERSION or "unknown"))

local MIN_CLOCK = 0.0005

local function geomean(values)
  if #values == 0 then return nil end
  local logsum = 0
  for _, v in ipairs(values) do
    logsum = logsum + math.log(v)
  end
  return math.exp(logsum / #values)
end

local section_scores = {}
local section_order = {}
local all_scores = {}
local missing_ref, failed_names = {}, {}

for _, r in ipairs(results) do
  if r.ok then
    local norm = REFERENCE_OPS_PER_SEC[r.name]
    if norm and r.ops then
      local t = math.max(r.time, MIN_CLOCK)
      local throughput = r.ops / t
      local score = 1000 * throughput / norm
      all_scores[#all_scores + 1] = score
      if not section_scores[r.section] then
        section_scores[r.section] = {}
        section_order[#section_order + 1] = r.section
      end
      table.insert(section_scores[r.section], score)
    else
      missing_ref[#missing_ref + 1] = r.name
    end
  else
    failed_names[#failed_names + 1] = r.name
  end
end

print("SpLB Scores (higher = faster)")

for _, sec in ipairs(section_order) do
  if sec ~= "Summary" then
    local sub = geomean(section_scores[sec])
    print(string.format("  %-45s %8.1f", sec, sub))
  end
end

local overall = geomean(all_scores)

if overall then
  print(string.format("SpLB Score:   %.1f", overall))
else
  print("SpLB Score:   n/a (no reference throughput matched)")
end
if #missing_ref > 0 then
  print("NOTE: no reference throughput for: " .. table.concat(missing_ref, ", "))
end
if #failed_names > 0 then
  print("NOTE: excluded from score (failed): " .. table.concat(failed_names, ", "))
end
