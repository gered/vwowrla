function obj_copy(obj, seen)
	if type(obj) ~= 'table' then return obj end
	if seen and seen[obj] then return seen[obj] end
	local s = seen or {}
	local res = setmetatable({}, getmetatable(obj))
	s[obj] = res
	for k, v in pairs(obj) do res[obj_copy(k, s)] = obj_copy(v, s) end
	return res
end

function dump_var(var, indent)
	print("<", type(var), ">")
	if not indent then indent = 0 end
	if type(var) == "table" then
		for k, v in pairs(var) do
			formatting = string.rep("  ", indent) .. k .. ": "
			if type(v) == "table" then
				print(formatting)
				dump_var(v, indent+1)
			elseif type(v) == 'boolean' then
				print(formatting .. tostring(v))      
			else
				print(formatting .. v)
			end
		end
	elseif type(var) == "boolean" then
		formatting = string.rep("  ", indent)
		print(formatting .. tostring(var))
	else
		formatting = string.rep("  ", indent)
		print(formatting .. tostring(var))
	end
end

--function explode(sep, str, limit)
--    if not sep or sep == "" then return false end
--    if not str then return false end
--    limit = limit or mhuge
--    if limit == 0 or limit == 1 then return {str},1 end
--
--    local r = {}
--    local n, init = 0, 1
--
--    while true do
--        local s,e = strfind(str, sep, init, true)
--        if not s then break end
--        r[#r+1] = strsub(str, init, s - 1)
--        init = e + 1
--        n = n + 1
--        if n == limit - 1 then break end
--    end
--
--    if init <= strlen(str) then
--        r[#r+1] = strsub(str, init)
--    else
--        r[#r+1] = ""
--    end
--    n = n + 1
--
--    if limit < 0 then
--        for i=n, n + limit + 1, -1 do r[i] = nil end
--        n = n + limit
--    end
--
--    return r, n
--end