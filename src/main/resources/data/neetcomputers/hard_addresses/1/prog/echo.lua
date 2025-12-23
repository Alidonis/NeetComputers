local a = {...}
local b = ""
for i,v in pairs(a) do
	if i ~= 1 then
		b = b.." "
	end
	b = b..v
end
term.print(b)