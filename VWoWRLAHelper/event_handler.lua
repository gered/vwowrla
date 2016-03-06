wipe_or_timeout_period = 60

active_encounter = {}
previous_successful_encounters = {}

function undo_swstats_fixlogstring(combat_log_line)
	return string.gsub(combat_log_line, " 's", "'s")
end

function sanitize_entity_name(entity_name)
	local fixed_name = problem_entity_name_to_fixed_name.problem_to_fixed[entity_name]
	if fixed_name then
		return fixed_name
	else
		return entity_name
	end
end

function get_original_entity_name(fixed_entity_name)
	local original_name = problem_entity_name_to_fixed_name.fixed_to_problem[fixed_entity_name]
	if original_name then
		return original_name
	else
		return fixed_entity_name
	end
end

function sanitize_entity_names(combat_log_line)
	local fixed_line = combat_log_line
	for problem_name, fixed_name in pairs(problem_entity_name_to_fixed_name.problem_to_fixed) do
		fixed_line = string.gsub(fixed_line, problem_name, fixed_name)
	end
	return fixed_line
end

function parse_combat_log_line(combat_log_line)
	local found_matcher, match
	for i, matcher in ipairs(vwowrla_combat_log_patterns) do
		for j, pattern in ipairs(matcher.pattern) do
			local _, _, m1, m2, m3, m4, m5, m6 = string.find(combat_log_line, pattern)
			if m1 then
				-- found a matching line. dont need to test anymore patterns.
				-- if fn does not exist, don't worry, just return nil (it's probably an ignored pattern)
				if matcher.fn then
					local result = matcher.fn({m1, m2, m3, m4, m5, m6})
					result.event = matcher.event
					result.line = combat_log_line
					result.timestamp = time()
					return result
				else
					return nil
				end
			end
		end
	end

	-- no match found
	print("*** UNRECOGNIZED LINE***")
	print("  >", event, combat_log_line)
	return nil
end

function process_parsed_combat_log_line(parsed_line)
	if parsed_line.source_name then
		parsed_line.source_name = get_original_entity_name(parsed_line.source_name)
	end
	if parsed_line.source then
		parsed_line.source = get_original_entity_name(parsed_line.source)
	end
	if parsed_line.target_name then
		parsed_line.target_name = get_original_entity_name(parsed_line.target_name)
	end
end

function in_active_encounter()
	return not (active_encounter.name == nil)
end

function touch_entity(entity_name, timestamp)
	if not active_encounter.entities[entity_name] then
		active_encounter.entities[entity_name] = {
			last_activity_at = timestamp,
			deaths = {},
			resurrections = {}
		}
	else
		active_encounter.entities[entity_name].last_activity_at = timestamp
	end
end

function kill_entity(entity_name, timestamp)
	active_encounter.entities[entity_name].last_activity_at = timestamp
	table.insert(active_encounter.entities[entity_name].deaths, timestamp)
end

function handle_line(parsed_line)
	local ev = parsed_line.event

	if ev == "ignored" or ev == "other-damage" then
		-- nop
	elseif ev == "death" then
		kill_entity(parsed_line.source_name, parsed_line.timestamp)
	else
		if parsed_line.source_name then touch_entity(parsed_line.source_name, parsed_line.timestamp) end
		if parsed_line.target_name then touch_entity(parsed_line.target_name, parsed_line.timestamp) end
	end
end

function count_dead(entity_name)
	local entity = active_encounter.entities[entity_name]
	if entity then
		local num_deaths = table.getn(entity.deaths)
		local num_resurrects = table.getn(entity.resurrections)
		return num_deaths - num_resurrects
	else
		return 0
	end
end

function find_defined_encounter(entity_name)
	for encounter_name, encounter in pairs(defined_encounters) do
		for encounter_entity, entity_props in pairs(encounter.entities) do
			if entity_name == encounter_entity then return encounter_name end
		end
	end
	return nil
end

function determine_encounter_from_combat_event(parsed_line)
	local encounter_name
	encounter_name = find_defined_encounter(parsed_line.source_name)
	if not encounter_name then encounter_name = find_defined_encounter(parsed_line.target_name) end
	return encounter_name
end

function has_previous_successful_encounter(encounter_name)
	if not encounter_name then return false end
	for i, name in ipairs(previous_successful_encounters) do
		if name == encounter_name then return true end
	end
	return false
end

function aura_is_non_combat_starting(aura_name)
	if not aura_name then return false end
	for i, name in ipairs(non_combat_starting_auras) do
		if name == aura_name then return true end
	end
	return false
end

function detect_encounter_triggered(parsed_line)
	local encounter_name = determine_encounter_from_combat_event(parsed_line)
	if not encounter_name then return nil end

	if (not has_previous_successful_encounter(encounter_name)) and (not aura_is_non_combat_starting(parsed_line.aura_name)) then
		local encounter = defined_encounters[encounter_name]
		if encounter.trigger_on_attack then
			if parsed_line.attack then return encounter_name end
		else
			return encounter_name
		end
	end

	return nil
end

function are_all_encounter_mobs_dead()
	local encounter = defined_encounters[active_encounter.name]
	for entity_name, props in pairs(encounter.entities) do
		local num_dead = count_dead(entity_name)
		if props.must_kill_count then
			if num_dead < props.must_kill_count then return false end
		else
			if num_dead < props.count then return false end
		end
	end
	return true
end

function has_active_encounter_wiped_or_timed_out(parsed_line)
	local timestamp = parsed_line.timestamp
	local encounter = defined_encounters[active_encounter.name]
	for entity_name, props in pairs(encounter.entities) do
		local entity = active_encounter.entities[entity_name]
		if entity then
			if (timestamp - entity.last_activity_at) < wipe_or_timeout_period then return false end
		end
	end
	return true
end

function detect_encounter_end(parsed_line)
	if are_all_encounter_mobs_dead() then
		return "killed"
	elseif has_active_encounter_wiped_or_timed_out(parsed_line) then
		return "wipe-or-timeout"
	else
		return nil
	end
end

function begin_encounter(encounter_name, parsed_line)
	local datetime = date()
	active_encounter = {
		name = encounter_name,
		entities = {},
		started_at = datetime,
		started_at_timestamp = parsed_line.timestamp
	}
	print("Beginning encounter \"" .. encounter_name .. "\" detected on line: " .. parsed_line.line)
	take_raid_buff_snapshot(encounter_name)
end

function end_encounter(parsed_line, end_reason)
	local wipe_or_timeout = (end_reason == "wipe-or-timeout")
	print("Ending encounter \"" .. active_encounter.name .. "\" detected on line: " .. parsed_line.line)
	if wipe_or_timeout then
		print("Encounter ending due to wipe or trigger entity activity timeout (unsuccessful encounter kill attempt).")
	end

	if not wipe_or_timeout then
		table.insert(previous_successful_encounters, active_encounter.name);
	end

	active_encounter = {}
end

function active_encounter_processing(parsed_line)
	handle_line(parsed_line)
	local end_reason = detect_encounter_end(parsed_line)
	if end_reason then
		end_encounter(parsed_line, end_reason)
	end
end

function out_of_encounter_processing(parsed_line)
	local encounter_name = detect_encounter_triggered(parsed_line)
	if encounter_name then
		begin_encounter(encounter_name, parsed_line)
		handle_line(parsed_line)
	end
end

function vwowrla_process_combat_event(combat_log_line)
	if not combat_log_line then return end

	combat_log_line = undo_swstats_fixlogstring(sanitize_entity_names(combat_log_line))

	local parsed = parse_combat_log_line(combat_log_line)
	if parsed then
		process_parsed_combat_log_line(parsed)
		if in_active_encounter() then
			active_encounter_processing(parsed)
		else
			out_of_encounter_processing(parsed)
		end
	end
end

