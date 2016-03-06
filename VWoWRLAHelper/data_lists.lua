defined_encounters = {
	["Lucifron"] = {
		entities = {
			["Lucifron"] = {count = 1},
			["Flamewaker Protector"] = {count = 2}
		}
	},
	["Magmadar"] = {
		entities = {
			["Magmadar"] = {count = 1}
		}
	},
	["Gehennas"] = {
		entities = {
			["Gehennas"] = {count = 1},
			["Flamewaker"] = {count = 2}
		}
	},
	["Garr"] = {
		entities = {
			["Garr"] = {count = 1},
			["Firesworn"] = {count = 8}
		}
	},
	["Baron Geddon"] = {
		entities = {
			["Baron Geddon"] = {count = 1}
		}
	},
	["Shazzrah"] = {
		entities = {
			["Shazzrah"] = {count = 1}
		}
	},
	["Sulfuron Harbinger"] = {
		entities = {
			["Sulfuron Harbinger"] = {count = 1},
			["Flamewaker Priest"] = {count = 4}
		}
	},
	["Golemagg the Incinerator"] = {
		entities = {
			["Golemagg the Incinerator"] = {count = 1},
			["Core Rager"] = {count = 2}
		}
	},
	["Majordomo Executus"] = {
		entities = {
			["Majordomo Executus"] = {count = 1, must_kill_count = 0},
			["Flamewaker Healer"] = {count = 4},
			["Flamewaker Elite"] = {count = 4}
		}
	},
	["Ragnaros"] = {
		entities = {
			["Ragnaros"] = {count = 1}
		},
		trigger_on_attack = true
	},
	["Onyxia"] = {
		entities = {
			["Onyxia"] = {count = 1}
		}
	}
}

non_combat_starting_auras = {
	"Hunter's Mark",
	"Detect Magic",
	"Mind Soothe",
	"Distract"
}

vwowrla_events_to_register = {
	"CHAT_MSG_SPELL_AURA_GONE_OTHER",
	"CHAT_MSG_SPELL_AURA_GONE_PARTY",
	"CHAT_MSG_SPELL_AURA_GONE_SELF",
	"CHAT_MSG_SPELL_BREAK_AURA",
	"CHAT_MSG_SPELL_CREATURE_VS_CREATURE_BUFF",
	"CHAT_MSG_SPELL_CREATURE_VS_CREATURE_DAMAGE",
	"CHAT_MSG_SPELL_CREATURE_VS_PARTY_BUFF",
	"CHAT_MSG_SPELL_CREATURE_VS_PARTY_DAMAGE",
	"CHAT_MSG_SPELL_CREATURE_VS_SELF_BUFF",
	"CHAT_MSG_SPELL_CREATURE_VS_SELF_DAMAGE",
	"CHAT_MSG_SPELL_DAMAGESHIELDS_ON_OTHERS",
	"CHAT_MSG_SPELL_DAMAGESHIELDS_ON_SELF",
	"CHAT_MSG_SPELL_FRIENDLYPLAYER_BUFF",
	"CHAT_MSG_SPELL_FRIENDLYPLAYER_DAMAGE",
	"CHAT_MSG_SPELL_HOSTILEPLAYER_BUFF",
	"CHAT_MSG_SPELL_HOSTILEPLAYER_DAMAGE",
	"CHAT_MSG_SPELL_ITEM_ENCHANTMENTS",
	"CHAT_MSG_SPELL_PARTY_BUFF",
	"CHAT_MSG_SPELL_PARTY_DAMAGE",
	"CHAT_MSG_SPELL_PERIODIC_CREATURE_BUFFS",
	"CHAT_MSG_SPELL_PERIODIC_CREATURE_DAMAGE",
	"CHAT_MSG_SPELL_PERIODIC_FRIENDLYPLAYER_BUFFS",
	"CHAT_MSG_SPELL_PERIODIC_FRIENDLYPLAYER_DAMAGE",
	"CHAT_MSG_SPELL_PERIODIC_HOSTILEPLAYER_BUFFS",
	"CHAT_MSG_SPELL_PERIODIC_HOSTILEPLAYER_DAMAGE",
	"CHAT_MSG_SPELL_PERIODIC_PARTY_BUFFS",
	"CHAT_MSG_SPELL_PERIODIC_PARTY_DAMAGE",
	"CHAT_MSG_SPELL_PERIODIC_SELF_BUFFS",
	"CHAT_MSG_SPELL_PERIODIC_SELF_DAMAGE",
	"CHAT_MSG_SPELL_PET_BUFF",
	"CHAT_MSG_SPELL_PET_DAMAGE",
	"CHAT_MSG_SPELL_SELF_BUFF",
	"CHAT_MSG_SPELL_SELF_DAMAGE",
	"CHAT_MSG_COMBAT_CREATURE_VS_CREATURE_HITS",
	"CHAT_MSG_COMBAT_CREATURE_VS_CREATURE_MISSES",
	"CHAT_MSG_COMBAT_CREATURE_VS_PARTY_HITS",
	"CHAT_MSG_COMBAT_CREATURE_VS_PARTY_MISSES",
	"CHAT_MSG_COMBAT_CREATURE_VS_SELF_HITS",
	"CHAT_MSG_COMBAT_CREATURE_VS_SELF_MISSES",
	"CHAT_MSG_COMBAT_FRIENDLYPLAYER_HITS",
	"CHAT_MSG_COMBAT_FRIENDLYPLAYER_MISSES",
	"CHAT_MSG_COMBAT_FRIENDLY_DEATH",
	"CHAT_MSG_COMBAT_HOSTILEPLAYER_HITS",
	"CHAT_MSG_COMBAT_HOSTILEPLAYER_MISSES",
	"CHAT_MSG_COMBAT_HOSTILE_DEATH",
	"CHAT_MSG_COMBAT_MISC_INFO",
	"CHAT_MSG_COMBAT_PARTY_HITS",
	"CHAT_MSG_COMBAT_PARTY_MISSES",
	"CHAT_MSG_COMBAT_PET_HITS",
	"CHAT_MSG_COMBAT_PET_MISSES",
	"CHAT_MSG_COMBAT_SELF_HITS",
	"CHAT_MSG_COMBAT_SELF_MISSES"
};

problem_entity_names = {
	"\"Plucky\" Johnson's Human Form",
	"Alzzin's Minion",
	"Antu'sul",
	"Anub'shiah",
	"Arin'sor",
	"Arugal's Voidwalker",
	"Atal'ai Deathwalker's Spirit",
	"Chok'sul",
	"Commander Gor'shak",
	"Darkreaver's Fallen Charger",
	"Death's Head Acolyte",
	"Death's Head Adept",
	"Death's Head Cultist",
	"Death's Head Geomancer",
	"Death's Head Necromancer",
	"Death's Head Priest",
	"Death's Head Sage",
	"Death's Head Seer",
	"Death's Head Ward Keeper",
	"Doctor Weavil's Flying Machine",
	"Dreka'Sur",
	"Eliza's Guard",
	"Faldreas Goeth'Shael",
	"Father Winter's Helper",
	"Fellicent's Shade",
	"Flik's Frog",
	"Franclorn's Spirit",
	"Gizlock's Dummy",
	"Great-father Winter's Helper",
	"Greatfather Winter's Helper",
	"Gunther's Visage",
	"Guse's War Rider",
	"Hammertoe's Spirit",
	"Helcular's Remains",
	"Hukku's Imp",
	"Hukku's Succubus",
	"Hukku's Voidwalker",
	"Ichman's Gryphon",
	"Jen'shan",
	"Jezelle's Felhunter",
	"Jezelle's Felsteed",
	"Jezelle's Imp",
	"Jezelle's Succubus",
	"Jezelle's Voidwalker",
	"Jeztor's War Rider",
	"Jin'sora",
	"Jugkar Grim'rod's Image",
	"Krakle's Thermometer",
	"Kurzen's Agent",
	"Lord Azrethoc's Image",
	"Maiden's Virtue Crewman",
	"Merithra's Wake",
	"Mulverick's War Rider",
	"Nefarian's Troops",
	"Nijel's Point Guard",
	"Noxxion's Spawn",
	"Officer Vu'Shalay",
	"Onyxia's Elite Guard",
	"Rak'shiri",
	"Ralo'shan the Eternal Watcher",
	"Ribbly's Crony",
	"Ryson's Eye in the Sky",
	"Sartura's Royal Guard",
	"Sentinel Glynda Nal'Shea",
	"Sergeant Ba'sha",
	"Servant of Antu'sul",
	"Sharpbeak's Father",
	"Sharpbeak's Mother",
	"Slidore's Gryphon",
	"Slim's Friend",
	"Sneed's Shredder",
	"Sri'skulk",
	"The Master's Eye",
	"Twilight's Hammer Ambassador",
	"Twilight's Hammer Executioner",
	"Twilight's Hammer Torturer",
	"Tyrion's Spybot",
	"Umi's Mechanical Yeti",
	"Varo'then's Ghost",
	"Vipore's Gryphon",
	"Warug's Bodyguard",
	"Warug's Target Dummy",
	"Winna's Kitten",
	"Winter's Little Helper",
	"Wizzlecrank's Shredder",
	"Wrenix's Gizmotronic Apparatus",
	"Xiggs Fuselighter's Flyingmachine",
	"Ysida's Trigger",
	"Zaetar's Spirit"
}

problem_entity_name_to_fixed_name = {
	problem_to_fixed = {},
	fixed_to_problem = {}
}

for i, problem_name in ipairs(problem_entity_names) do
	local fixed_name = string.gsub(problem_name, "'s", "s")
	problem_entity_name_to_fixed_name.problem_to_fixed[problem_name] = fixed_name
	problem_entity_name_to_fixed_name.fixed_to_problem[fixed_name] = problem_name
end
