vwowrla = {}

vwowrla_raid_snapshots = {}   -- saved data

function get_buff_name(target, idx)
	VWoWRLA_tooltip:ClearLines()
	VWoWRLA_tooltip:SetOwner(UIParent,"ANCHOR_NONE")
	VWoWRLA_tooltip:SetUnitBuff(target, idx)
	return VWoWRLA_tooltipTextLeft1:GetText()
end

function get_debuff_name(target, idx)
	VWoWRLA_tooltip:ClearLines()
	VWoWRLA_tooltip:SetOwner(UIParent,"ANCHOR_NONE")
	VWoWRLA_tooltip:SetUnitDebuff(target, idx)
	return VWoWRLA_tooltipTextLeft1:GetText()
end

function get_all_unit_buffs(target)
	local buffs = {}
	local i = 1
	local buff = UnitBuff(target, i)
	while buff do
		table.insert(buffs, get_buff_name(target, i))
		i = i + 1
		buff = UnitBuff(target, i)
	end
	return buffs;
end

function get_all_unit_debuffs(target)
	local debuffs = {}
	local i = 1
	local debuff = UnitDebuff(target, i)
	while debuff do
		table.insert(debuffs, get_debuff_name(target, i))
		i = i + 1
		debuff = UnitDebuff(target, i)
	end
	return debuffs;
end

function get_party_member_info(unit)
	local buffs = get_all_unit_buffs(unit)
	local debuffs = get_all_unit_debuffs(unit)
	local localizedClass, englishClass = UnitClass(unit);
	local level = UnitLevel(unit);
	local online = UnitIsConnected(unit);
	return {buffs = buffs, debuffs = debuffs, class = englishClass, level = level, online = online}
end

function get_all_raid_member_status()
	local status = {}
	local unit, name, buffs, debuffs, localizedClass, englishClass, level
	for i = 1, GetNumRaidMembers() do
		unit = "raid" .. i
		name = UnitName(unit)
		if name then
			status[name] = get_party_member_info(unit)
		end
	end
	for i = 1, GetNumPartyMembers() do
		unit = "party" .. i
		name = UnitName(unit)
		if name then
			status[name] = get_party_member_info(unit)
		end

		unit = "player"
		name = UnitName(unit)
		status[name] = get_party_member_info(unit)
	end

	return status
end

function take_raid_buff_snapshot(encounter_name)
	print("Taking snapshot of all raid member buffs/debuffs.")
	local snapshot = get_all_raid_member_status()
	if not vwowrla_raid_snapshots then
		vwowrla_raid_snapshots = {}
	end
	table.insert(vwowrla_raid_snapshots, {
		date = date(),
		encounter = encounter_name,
		snapshot = snapshot
	})
end

function VWoWRLA_on_load()
	this:RegisterEvent("VARIABLES_LOADED")
	this:RegisterEvent("PLAYER_ENTERING_WORLD")
	this:RegisterEvent("ZONE_CHANGED_NEW_AREA")

	print("Combat Log Parser Helper -- Loaded")
	if not vwowrla_raid_snapshots then
		print("vwowrla_raid_snapshots is nil")
		vwowrla_raid_snapshots = {}
	else
		print("vwowrla_raid_snapshots is not nil")
	end

	if not vwowrla.Loaded then
	print("Combat Log Parser Helper -- Registering for events.")
		VWoWRLA_register_events()
		vwowrla.Loaded = true
	end
end

function VWoWRLA_on_event()
	if not arg1 then return end

	--print("event:", event, arg1, arg2, arg3, arg4, arg5, arg6)

	vwowrla_process_combat_event(arg1)
end

function VWoWRLA_register_events()
	for i, name in ipairs(vwowrla_events_to_register) do
		VWoWRLA_events:RegisterEvent(name)
	end
end