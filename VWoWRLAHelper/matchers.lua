-- these patterns copied from the full set of combat log regex patterns located in 
-- the server app code under vwowrla.core.events.matchers/regex-matchers and 
-- "dumbed-down" / converted to lua patterns. lua patterns are not as complex as regular 
-- expressions, so there's a bit more repetition to get around things like missing support
-- for optional substring matching (e.g. "(hits|crits)", "(?:missed|misses)", etc ...).

-- event types have been "compacted" somewhat. we only care about types of events in a
-- fairly broad sense and don't need to be really, really specific.

-- additionally, for the purposes of this addon, there are some bits of info in certain
-- more complicated combat log strings that we don't care about and so support for matching
-- them has just been dropped out of the appropriate patterns. things such as glancing/crushing
-- blows and partial absorbs/resists... all we are using these matchers for is to correctly
-- parse out 1) the type of event, 2) the names of the entities involved, and finally 3) the
-- name of any skill being used by the source on a target.

vwowrla_combat_log_patterns = {
	------------------------------------------------------------------------------------------------
	-- IGNORED EVENTS
	-- these are only here to prevent "unrecognized" errors for combat log events that we
	-- do not care about. we list them first so they are matched first (and not perhaps mistaken
	-- for another type of event) and then ignored by the event handler.
	{
		event = "ignored",
		pattern = {
			"^You fail to cast (.+): (.+)%.$",
			"^You fail to perform (.+): (.+)%.$",
			"^You have slain (.+)!$",
			"^(.+) is slain by (.+)!$",
			"^(.+) create(%a?) (.+)%.$",
			"^Your pet begins eating a (.+)%.$",
			"^(.-)'s pet begins eating a (.+)%.$",
			"^Your (.+) is reflected back by (.+)%.$",
			"^(.-)'s (.+) is reflected back by (.+)%.$",
			"^(.+) is destroyed%.$",
			"^Your (.+) reputation has increased by (%d+)%.$",
			"^Your equipped items suffer a (.+) durability loss%.$",
			"^(.+) dies, honorable kill (.+)$",
			"^(.+) is killed by (.+)%.$"
		}
	},

	------------------------------------------------------------------------------------------------
	-- SKILL/SPELL DAMAGE
	{
		event = "skill-damage-to-target",
		pattern = {
			"^Your (.+) hits (.+) for (%d+) (.+) damage%.(.*)$",
			"^Your (.+) crits (.+) for (%d+) (.+) damage%.(.*)$",
			"^Your (.+) hits (.+) for (%d+) damage%.(.*)$",
			"^Your (.+) crits (.+) for (%d+) damage%.(.*)$",
			"^Your (.+) hits (.+) for (%d+) (.+)%.(.*)$",
			"^Your (.+) crits (.+) for (%d+) (.+)%.(.*)$"
			},
		fn = function(matches)
			return {
				skill = matches[1],
				target_name = matches[2],
				source_name = "you",
				attack = true
			}
		 end
	},
	{
		event = "skill-damage-to-target",
		pattern = {
			"^(.-)'s (.+) hits (.+) for (%d+) (.+) damage%.(.*)$",
			"^(.-)'s (.+) crits (.+) for (%d+) (.+) damage%.(.*)$",
			"^(.-)'s (.+) hits (.+) for (%d+) damage%.(.*)$",
			"^(.-)'s (.+) crits (.+) for (%d+) damage%.(.*)$",
			"^(.-)'s (.+) hits (.+) for (%d+)%.(.*)$",
			"^(.-)'s (.+) crits (.+) for (%d+)%.(.*)$"
			},
		fn = function(matches)
			return {
				source_name = matches[1],
				skill = matches[2],
				target_name = matches[3],
				attack = true
			}
		 end
	},

	------------------------------------------------------------------------------------------------
	-- SKILL/SPELL MISSES / FULL ABSORBS/RESISTS
	{
		event = "skill-avoided-by-target",
		pattern = {
			"^Your (.+) missed (.+)%.$",
			"^Your (.+) was parried by (.+)%.$",
			"^Your (.+) was blocked by (.+)%.$",
			"^Your (.+) was dodged by (.+)%.$",
			"^Your (.+) was evaded by (.+)%.$",
			"^Your (.+) is absorbed by (.+)%.$",
			"^Your (.+) was resisted by (.+)%.$",
			"^Your (.+) failed%. (.+) is immune%.$"
		},
		fn = function(matches)
			return {
				skill = matches[1],
				target_name = matches[2],
				source_name = "you",
				attack = true
			}
		end
	},
	{
		event = "skill-avoided-by-target",
		pattern = {
			"^(.+) resists your (.+)%.$"
		},
		fn = function(matches)
			return {
				target_name = matches[1],
				skill = matches[2],
				source_name = "you",
				attack = true
			}
		end
	},
	{
		event = "skill-avoided-by-target",
		pattern = {
			"^(.-)'s (.+) was parried%.$",
			"^(.-)'s (.+) was dodged%.$",
			"^(.-)'s (.+) was resisted%.$"
		},
		fn = function(matches)
			return {
				source_name = matches[1],
				skill = matches[2],
				target_name = "you",
				attack = true
			}
		end
	},
	{
		event = "skill-avoided-by-target",
		pattern = {
			"^(.-)'s (.+) misses (.+)%.$",
			"^(.-)'s (.+) missed (.+)%.$",
			"^(.-)'s (.+) was parried by (.+)%.$",
			"^(.-)'s (.+) was blocked by (.+)%.$",
			"^(.-)'s (.+) was dodged by (.+)%.$",
			"^(.-)'s (.+) was evaded by (.+)%.$",
			"^(.-)'s (.+) is absorbed by (.+)%.$",
			"^(.-)'s (.+) was resisted by (.+)%.$",
			"^(.-)'s (.+) fails%. (.+) is immune%.$"
		},
		fn = function(matches)
			return {
				source_name = matches[1],
				skill = matches[2],
				target_name = matches[3],
				attack = true
			}
		end
	},
	{
		event = "skill-avoided-by-target",
		pattern = {
			"^(.+) resists? (.-)'s (.+)%.$",
			"^(.+) absorbs? (.-)'s (.+)%.$"
		},
		fn = function(matches)
			return {
				target_name = matches[1],
				source_name = matches[2],
				skill = matches[3],
				attack = true
			}
		end
	},

	------------------------------------------------------------------------------------------------
	-- REFLECTED DAMAGE
	{
		event = "damage-reflected",
		pattern = {
			"^(.+) reflects? (%d+) (.+) damage to (.+)%.$"
		},
		fn = function(matches)
			return {
				source_name = matches[1],
				target_name = matches[4],
				attack = true
			}
		end
	},

	------------------------------------------------------------------------------------------------
	-- MELEE DAMAGE
	{
		event = "melee-damage-to-target",
		pattern = {
			"^(.+) hits? (.+) for (%d+)%.(.*)$",
			"^(.+) crits? (.+) for (%d+)%.(.*)$",
			"^(.+) hits? (.+) for (%d+) (.+) damage%.(.*)$",
			"^(.+) crits? (.+) for (%d+) (.+) damage%.(.*)$"
		},
		fn = function(matches)
			return {
				source_name = matches[1],
				target_name = matches[2],
				attack = true
			}
		end
	},

	------------------------------------------------------------------------------------------------
	-- MELEE DAMAGE AVOIDANCE (ABSORB/RESIST/MISS/BLOCK/DODGE/PARRY/EVADE)
	{
		event = "melee-avoided-by-target",
		pattern = {
			"^(.+) attacks?%. (.+) absorbs? all the damage%.$",
			"^(.+) attacks?%. (.+) resists? all the damage%.$",
			"^(.+) misses (.+)%.$",
			"^(.+) miss (.+)%.$",
			"^(.+) attacks?%. (.+) parry%.$",
			"^(.+) attacks?%. (.+) parries%.$",
			"^(.+) attacks?%. (.+) dodges?%.$",
			"^(.+) attacks?%. (.+) blocks?%.$",
			"^(.+) attacks?%. (.+) evades?%.$",
			"^(.+) attacks? but (.+) is immune%.$"
		},
		fn = function(matches)
			return {
				source_name = matches[1],
				target_name = matches[2],
				attack = true
			}
		end
	},

	------------------------------------------------------------------------------------------------
	-- SKILL INTERRUPTION
	{
		event = "skill-interrupted-by-target",
		pattern = {
			"^(.+) interrupts? your (.+)%.$"
		},
		fn = function(matches)
			return {
				source_name = matches[1],
				skill = matches[2],
				target_name = "you",
				attack = true
			}
		end
	},
	{
		event = "skill-interrupted-by-target",
		pattern = {
			"^(.+) interrupts? (.-)'s (.+)%.$"
		},
		fn = function(matches)
			return {
				source_name = matches[1],
				target_name = matches[2],
				skill = matches[3],
				attack = true
			}
		end
	},

	------------------------------------------------------------------------------------------------
	-- DAMAGE OVER TIME (DOT)
	{
		event = "dot-damages-target",
		pattern = {
			"^(.+) suffers? (%d+) (.+) damage from your (.+)%.(.*)$"
		},
		fn = function(matches)
			return {
				target_name = matches[1],
				skill = matches[4],
				source_name = "you",
				attack = true
			}
		end
	},
	{
		event = "dot-damages-target",
		pattern = {
			"^(.+) suffers? (%d+) (.+) damage from (.-)'s (.+)%.(.*)$"
		},
		fn = function(matches)
			return {
				target_name = matches[1],
				source_name = matches[4],
				skill = matches[5],
				attack = true
			}
		end
	},

	------------------------------------------------------------------------------------------------
	-- CAST NOTIFICATION / INSTANT CAST ABILITIES PERFORMED
	{
		event = "cast-begins",
		pattern = {
			"^(.+) begins? to perform (.+)%.$",
			"^(.+) begins? to cast (.+)%.$"
		},
		fn = function(matches)
			return {
				source_name = matches[1],
				skill = matches[2]
			}
		end
	},
	{
		event = "skill-performed-on-target",
		pattern = {
			"^(.+) casts? (.+) on (.+): (.+)%.$",
			"^(.+) performs? (.+) on (.+): (.+)%.$",
			"^(.+) casts? (.+) on (.+)%.$",
			"^(.+) performs? (.+) on (.+)%.$"
		},
		fn = function(matches)
			return {
				source_name = matches[1],
				skill = matches[2],
				target_name = matches[3]
			}
		end
	},
	{
		event = "cast",
		pattern = {
			"^(.+) casts? (.+)%.$",
			"^(.+) performs? (.+)%.$"
		},
		fn = function(matches)
			return {
				source_name = matches[1],
				skill = matches[2]
			}
		end
	},

	------------------------------------------------------------------------------------------------
	-- DIRECT HEALING FROM SKILLS
	{
		event = "skill-heals-target",
		pattern = {
			"^Your (.+) critically heals (.+) for (%d+)%.$",
			"^Your (.+) heals (.+) for (%d+)%.$"
		},
		fn = function(matches)
			return {
				skill = matches[1],
				target_name = matches[2],
				source_name = "you"
			}
		end
	},
	{
		event = "skill-heals-target",
		pattern = {
			"^(.-)'s (.+) critically heals (.+) for (%d+)%.$",
			"^(.-)'s (.+) heals (.+) for (%d+)%.$"
		},
		fn = function(matches)
			return {
				source_name = matches[1],
				skill = matches[2],
				target_name = matches[3]
			}
		end
	},

	------------------------------------------------------------------------------------------------
	-- RESOURCE (HEALTH/MANA/RAGE/ENERGY/HAPPINESS) GAIN / LOSS
	{
		event = "resource-gained",
		pattern = {
			"^(.+) gains? (%d+) (.+) from your (.+)%.$"
		},
		fn = function(matches)
			return {
				target_name = matches[1],
				resource_type = string.lower(matches[3]),
				skill = matches[4],
				source_name = "you"
			}
		end
	},
	{
		event = "resource-gained",
		pattern = {
			"^(.+) gains? (%d+) (.+) from (.-)'s (.+)%.$"
		},
		fn = function(matches)
			return {
				target_name = matches[1],
				resource_type = string.lower(matches[3]),
				source_name = matches[4],
				skill = matches[5]
			}
		end
	},
	{
		event = "resource-gained",
		pattern = {
			"^(.+) gains? (%d+) (.+) from (.+)%.$"
		},
		fn = function(matches)
			return {
				target_name = matches[1],
				resource_type = string.lower(matches[3]),
				skill = matches[4],
				source_name = matches[1]
			}
		end
	},
	{
		event = "resource-lost",
		pattern = {
			"^Your (.+) drains (%d+) (.+) from (.+)%.$"
		},
		fn = function(matches)
			return {
				skill = matches[1],
				resource_type = string.lower(matches[3]),
				target_name = matches[4],
				source_name = "you"
			}
		end
	},
	{
		event = "resource-lost",
		pattern = {
			"^(.-)'s (.+) drains (%d+) (.+) from (.+)%.$"
		},
		fn = function(matches)
			return {
				source_name = matches[1],
				skill = matches[2],
				resource_type = string.lower(matches[4]),
				target_name = matches[5]
			}
		end
	},

	------------------------------------------------------------------------------------------------
	-- OTHER SPECIAL ABILITY GAINS
	{
		event = "special-gained",
		pattern = {
			"^(.+) gains? (.+) through (.+)%.$"
		},
		fn = function(matches)
			return {
				target_name = matches[1],
				special = matches[2],
				source = matches[3]  -- not an entity name
			}
		end
	},

	------------------------------------------------------------------------------------------------
	-- BUFF/DEBUFF
	{
		event = "aura-gained",
		pattern = {
			"^(.+) gains? (.+) %((%d+)%)%.$",
			"^(.+) gains? (.+)%.$"
		},
		fn = function(matches)
			return {
				target_name = matches[1],
				aura_name = matches[2],
				aura_type = "buff"
			}
		end
	},
	{
		event = "aura-gained",
		pattern = {
			"^(.+) is afflicted by (.+) %((%d+)%)%.$",
			"^(.+) are afflicted by (.+) %((%d+)%)%.$",
			"^(.+) is afflicted by (.+)%.$",
			"^(.+) are afflicted by (.+)%.$",
		},
		fn = function(matches)
			return {
				target_name = matches[1],
				aura_name = matches[2],
				aura_type = "debuff",
				attack = true
			}
		end
	},
	{
		event = "aura-lost",
		pattern = {
			"^(.+) fades from (.+)%.$"
		},
		fn = function(matches)
			return {
				aura_name = matches[1],
				target_name = matches[2],
			}
		end
	},
	{
		event = "aura-lost",
		pattern = {
			"^Your (.+) is removed%.$"
		},
		fn = function(matches)
			return {
				aura_name = matches[1],
				target_name = "you",
			}
		end
	},
	{
		event = "aura-lost",
		pattern = {
			"^(.-)'s (.+) is removed%.$"
		},
		fn = function(matches)
			return {
				target_name = matches[1],
				aura_name = matches[2],
			}
		end
	},

	------------------------------------------------------------------------------------------------
	-- ENVIRONMENTAL / OTHER DAMAGE
	{
		event = "other-damage",
		pattern = {
			"^(.+) suffers? (%d+) points of (.+) damage%.(.*)$"
		},
		fn = function(matches)
			return {
				target_name = matches[1],
			}
		end
	},
	{
		event = "other-damage",
		pattern = {
			"^(.+) loses? (%d+) health for swimming in lava%.(.*)$"
		},
		fn = function(matches)
			return {
				target_name = matches[1],
			}
		end
	},
	{
		event = "other-damage",
		pattern = {
			"^(.+) falls? and loses? (%d+) health%.$"
		},
		fn = function(matches)
			return {
				target_name = matches[1],
			}
		end
	},

	------------------------------------------------------------------------------------------------
	-- DEATH
	{
		event = "death",
		pattern = {
			"^(.+) dies?%.$"
		},
		fn = function(matches)
			return {
				source_name = matches[1],
			}
		end
	}
};
