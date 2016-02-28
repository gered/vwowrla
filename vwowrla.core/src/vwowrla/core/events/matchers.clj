(ns vwowrla.core.events.matchers
  (:use
    vwowrla.core.utils))

;;; *** IMPORTANT!! ***
;;; The order that the matchers are listed is **NOT** completely arbitrary!!
;;; Some of the regex patterns won't ever have a problem matching some line they shouldn't, but some others
;;; can match the wrong lines if the regex patterns appear after others. e.g. the skill/spell and melee patterns.
;;; The unit tests should pick this up if something gets moved out of the proper order, but be very careful!!

(def regex-matchers
  [

   ;;; ---------------------------------------------------------------------------------------------
   ;;; CATCH-ALL REGEX MATCHERS
   ;;; These are only here to prevent extra "unrecognized" warnings while parsing logs for messages
   ;;; that we simply don't care at all about.
   ;;; NOTE: these are at the beginning so we catch them first and move on. otherwise some could
   ;;;       potentially get caught and misinterpreted as other types of combat log messages

   {:event :ignored :regex #"^You fail to cast (.+): (.+)\.$"}
   {:event :ignored :regex #"^You fail to perform (.+): (.+)\.$"}
   {:event :ignored :regex #"^You have slain (.+)!$"}
   {:event :ignored :regex #"^(.+) is slain by (.+)!$"}
   {:event :ignored :regex #"^(.+) (?:creates|create) (.+)\.$"}
   {:event :ignored :regex #"^Your pet begins eating a (.+)\.$"}
   {:event :ignored :regex #"^(.+)'s pet begins eating a (.+)\.$"}
   {:event :ignored :regex #"^Your (.+) is reflected back by (.+)\.$"}
   {:event :ignored :regex #"^(.+?)'s (.+) is reflected back by (.+)\.$"}
   {:event :ignored :regex #"^(.+) is destroyed\.$"}
   {:event :ignored :regex #"^Your (.+) reputation has increased by (\d+)\.$"}
   {:event :ignored :regex #"^Your equipped items suffer a 10% durability loss\.$"}
   {:event :ignored :regex #"^(.+) dies, honorable kill (.+)$"}
   ; TODO: keep an eye on these types of entries. seems safe to ignore so far with Onyxia + MC though...
   {:event :ignored :regex #"^(.+) is killed by (.+)\.$"}

   ;;; ---------------------------------------------------------------------------------------------
   ;;; SKILL/SPELL DAMAGE

   {:regex  #"^Your (.+) (hits|crits) (.+) for (\d+) (.+) damage\.(?: \((\d+) resisted\))?(?: \((\d+) absorbed\))?$"
    :id     :skill-damages-target-elemental-self
    :logfmt :skill-damages-target-elemental
    :event  :skill-damage-to-target
    :args   #(hash-map
              :skill       %1
              :crit?       (= %2 "crits")
              :target-name %3
              :damage      (->int %4)
              :damage-type (->kw %5)
              :resisted    (->int %6)
              :absorbed    (->int %7)
              :source-name "you")}

   {:regex  #"^Your (.+) (hits|crits) (.+) for (\d+) damage\.(?: \((\d+) blocked\))?(?: \((\d+) absorbed\))?$"
    :id     :skill-damages-target-self
    :logfmt :skill-damages-target
    :event  :skill-damage-to-target
    :args   #(hash-map
              :skill       %1
              :crit?       (= %2 "crits")
              :target-name %3
              :damage      (->int %4)
              :blocked     (->int %5)
              :absorbed    (->int %6)
              :source-name "you"
              :damage-type :physical)}

   {:regex  #"^Your (.+) (hits|crits) (.+) for (\d+)\.(?: \((\d+) blocked\))?(?: \((\d+) absorbed\))?$"
    :id     :skill-damages-target-short-self
    :logfmt :skill-damages-target-short
    :event  :skill-damage-to-target
    :args   #(hash-map
              :skill       %1
              :crit?       (= %2 "crits")
              :target-name %3
              :damage      (->int %4)
              :blocked     (->int %5)
              :absorbed    (->int %6)
              :source-name "you"
              :damage-type :physical)}

   {:regex  #"^(.+?)'s (.+) (hits|crits) (.+) for (\d+) (.+) damage\.(?: \((\d+) resisted\))?(?: \((\d+) absorbed\))?$"
    :id     :skill-damages-target-elemental
    :logfmt :skill-damages-target-elemental
    :event  :skill-damage-to-target
    :args   #(hash-map
              :source-name %1
              :skill       %2
              :crit?       (= %3 "crits")
              :target-name %4
              :damage      (->int %5)
              :damage-type (->kw %6)
              :resisted    (->int %7)
              :absorbed    (->int %8))}

   ; TODO: is this ever emitted in a combat log? why did i add this one... ?
   {:regex  #"^(.+?)'s (.+) (hits|crits) (.+) for (\d+) damage\.(?: \((\d+) blocked\))?(?: \((\d+) absorbed\))?$"
    :id     :skill-damages-target
    :logfmt :skill-damages-target
    :event  :skill-damage-to-target
    :args   #(hash-map
              :source-name %1
              :skill       %2
              :crit?       (= %3 "crits")
              :target-name %4
              :damage      (->int %5)
              :blocked     (->int %6)
              :absorbed    (->int %7)
              :damage-type :physical)}

   {:regex  #"^(.+?)'s (.+) (hits|crits) (.+) for (\d+)\.(?: \((\d+) blocked\))?(?: \((\d+) absorbed\))?$"
    :id     :skill-damages-target-short
    :logfmt :skill-damages-target-short
    :event  :skill-damage-to-target
    :args   #(hash-map
              :source-name %1
              :skill       %2
              :crit?       (= %3 "crits")
              :target-name %4
              :damage      (->int %5)
              :blocked     (->int %6)
              :absorbed    (->int %7)
              :damage-type :physical)}

   ;;; ---------------------------------------------------------------------------------------------
   ;;; SKILL/SPELL MISSES / FULL-ABSORBS / FULL-RESISTS

   {:regex  #"^Your (.+) missed (.+)\.$"
    :id     :skill-miss-self
    :logfmt :skill-miss
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :skill            %1
              :target-name      %2
              :source-name      "you"
              :avoidance-method :miss)}

   {:regex  #"^(.+?)'s (.+) (?:missed|misses) (.+)\.$"
    :id     :skill-miss
    :logfmt :skill-miss
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :skill            %2
              :target-name      %3
              :avoidance-method :miss)}

   {:regex  #"^Your (.+) was parried by (.+)\.$"
    :id     :skill-parry-self
    :logfmt :skill-parry
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :skill            %1
              :target-name      %2
              :source-name      "you"
              :avoidance-method :parry)}

   {:regex  #"^(.+?)'s (.+) was parried by (.+)\.$"
    :id     :skill-parry
    :logfmt :skill-parry
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :skill            %2
              :target-name      %3
              :avoidance-method :parry)}

   {:regex  #"^(.+?)'s (.+) was parried\.$"
    :id     :skill-parry-implied-self
    :logfmt :skill-parry
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :skill            %2
              :target-name      "you"
              :avoidance-method :parry)}

   {:regex  #"^Your (.+) was blocked by (.+)\.$"
    :id     :skill-block-self
    :logfmt :skill-block
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :skill            %1
              :target-name      %2
              :source-name      "you"
              :avoidance-method :block)}

   {:regex  #"^(.+?)'s (.+) was blocked by (.+)\.$"
    :id     :skill-block
    :logfmt :skill-block
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :skill            %2
              :target-name      %3
              :avoidance-method :block)}

   {:regex  #"^Your (.+) was dodged by (.+)\.$"
    :id     :skill-dodge-self
    :logfmt :skill-dodge
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :skill            %1
              :target-name      %2
              :source-name      "you"
              :avoidance-method :dodge)}

   {:regex  #"^(.+?)'s (.+) was dodged by (.+)\.$"
    :id     :skill-dodge
    :logfmt :skill-dodge
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :skill            %2
              :target-name      %3
              :avoidance-method :dodge)}

   {:regex  #"^(.+?)'s (.+) was dodged\.$"
    :id     :skill-dodge-implied-self
    :logfmt :skill-dodge
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :skill            %2
              :target-name      "you"
              :avoidance-method :dodge)}

   {:regex  #"^Your (.+) was evaded by (.+)\.$"
    :id     :skill-evade-self
    :logfmt :skill-evade
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :skill            %1
              :target-name      %2
              :source-name      "you"
              :avoidance-method :evade)}

   {:regex  #"^(.+?)'s (.+) was evaded by (.+)\.$"
    :id     :skill-evade
    :logfmt :skill-evade
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :skill            %2
              :target-name      %3
              :avoidance-method :evade)}

   {:regex  #"^(.+) (?:resist|resists) your (.+)\.$"
    :id     :skill-resist-self
    :logfmt :skill-resist
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :target-name      %1
              :skill            %2
              :source-name      "you"
              :avoidance-method :resist)}

   {:regex  #"^(.+?) (?:resist|resists) (.+?)'s (.+)\.$"
    :id     :skill-resist
    :logfmt :skill-resist
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :target-name      %1
              :source-name      %2
              :skill            %3
              :avoidance-method :resist)}

   ; i don't think target is ever "you" for this one
   {:regex  #"^(.+?)'s (.+) is absorbed by (.+)\.$"
    :id     :skill-absorb
    :logfmt :skill-absorb
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :skill            %2
              :target-name      %3
              :avoidance-method :absorb)}

   {:regex  #"^Your (.+) is absorbed by (.+)\.$"
    :id     :skill-absorb-self
    :logfmt :skill-absorb
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :skill            %1
              :target-name      %2
              :source-name      "you"
              :avoidance-method :absorb)}

   {:regex  #"^(.+?) (?:absorb|absorbs) (.+?)'s (.+)\.$"
    :id     :skill-absorb-2
    :logfmt :skill-absorb-2
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :target-name      %1
              :source-name      %2
              :skill            %3
              :avoidance-method :absorb)}

   ; i don't think target is ever "you" for this one
   {:regex  #"^(.+?)'s (.+) was resisted by (.+)\.$"
    :id     :skill-resist-2
    :logfmt :skill-resist-2
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :skill            %2
              :target-name      %3
              :avoidance-method :resist)}

   {:regex  #"^Your (.+) was resisted by (.+)\.$"
    :id     :skill-resist-2-self
    :logfmt :skill-resist-2
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :skill            %1
              :target-name      %2
              :source-name      "you"
              :avoidance-method :resist)}

   {:regex  #"^(.+?)'s (.+) was resisted\.$"
    :id     :skill-resist-implied-self
    :logfmt :skill-resist-2
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :skill            %2
              :target-name      "you"
              :avoidance-method :resist)}

   {:regex  #"^Your (.+) failed\. (.+) is immune\.$"
    :id     :skill-immune-self
    :logfmt :skill-immune
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :skill            %1
              :target-name      %2
              :source-name      "you"
              :avoidance-method :immune)}

   {:regex  #"^(.+?)'s (.+) fails\. (.+) is immune\.$"
    :id     :skill-immune
    :logfmt :skill-immune
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :skill            %2
              :target-name      %3
              :avoidance-method :immune)}

   {:regex  #"^(.+) is immune to your (.+)\.$"
    :id     :skill-immune-2-self
    :logfmt :skill-immune-2
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :target-name      %1
              :skill            %2
              :source-name      "you"
              :avoidance-method :immune)}

   {:regex  #"^(.+?) is immune to (.+?)'s (.+)\.$"
    :id     :skill-immune-2
    :logfmt :skill-immune-2
    :event  :skill-avoided-by-target
    :args   #(hash-map
              :target-name      %1
              :source-name      %2
              :skill            %3
              :avoidance-method :immune)}

   ;;; ---------------------------------------------------------------------------------------------
   ;;; REFLECTS

   {:regex  #"^(.+) (?:reflects|reflect) (\d+) (.+) damage to (.+)\.$"
    :id     :target-reflects-elemental-damage
    :logfmt :target-reflects-elemental-damage
    :event  :damage-reflected
    :args   #(hash-map
              :source-name %1
              :damage      (->int %2)
              :damage-type (->kw %3)
              :target-name %4)}

   ;;; ---------------------------------------------------------------------------------------------
   ;;; MELEE DAMAGE

   {:regex  #"^(.+) (hit|hits|crit|crits) (.+) for (\d+)\.(?: \((glancing)\))?(?: \((crushing)\))?(?: \((\d+) blocked\))?(?: \((\d+) absorbed\))?$"
    :id     :melee-damages-target
    :logfmt :melee-damages-target
    :event  :melee-damage-to-target
    :args   #(hash-map
              :source-name %1
              :crit?       (one-of? %2 "crit" "crits")
              :target-name %3
              :damage      (->int %4)
              :hit-type    (cond
                             (= %5 "glancing") :glancing
                             (= %6 "crushing") :crushing
                             :else             :normal)
              :blocked     (->int %7)
              :absorbed    (->int %8)
              :damage-type :physical)}

   {:regex  #"^(.+) (hit|hits|crit|crits) (.+) for (\d+) (.+) damage\.(?: \((glancing)\))?(?: \((crushing)\))?(?: \((\d+) resisted\))?(?: \((\d+) absorbed\))?$"
    :id     :melee-damages-target-elemental
    :logfmt :melee-damages-target-elemental
    :event  :melee-damage-to-target
    :args   #(hash-map
              :source-name %1
              :crit?       (one-of? %2 "crit" "crits")
              :target-name %3
              :damage      (->int %4)
              :damage-type (->kw %5)
              :hit-type    (cond
                             (= %6 "glancing") :glancing
                             (= %7 "crushing") :crushing
                             :else             :normal)
              :resisted    (->int %8)
              :absorbed    (->int %9)
              :damage-type :physical)}

   ;;; ---------------------------------------------------------------------------------------------
   ;;; MELEE DAMAGE AVOIDANCE (ABSORB/RESIST/MISS/BLOCK/DODGE/PARRY/EVADE)

   {:regex  #"^(.+) (?:attack|attacks)\. (.+) (?:absorb|absorbs) all the damage\.$"
    :id     :melee-full-absorb
    :logfmt :melee-full-absorb
    :event  :melee-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :target-name      %2
              :avoidance-method :absorb)}

   {:regex  #"^(.+) (?:attack|attacks)\. (.+) (?:resist|resists) all the damage\.$"
    :id     :melee-full-resist
    :logfmt :melee-full-resist
    :event  :melee-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :target-name      %2
              :avoidance-method :resist)}

   {:regex  #"^(.+) (?:miss|misses) (.+)\.$"
    :id     :melee-miss
    :logfmt :melee-miss
    :event  :melee-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :target-name      %2
              :avoidance-method :miss)}

   {:regex  #"^(.+) (?:attack|attacks)\. (.+) (?:parry|parries)\.$"
    :id     :melee-parry
    :logfmt :melee-parry
    :event  :melee-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :target-name      %2
              :avoidance-method :parry)}

   {:regex  #"^(.+) (?:attack|attacks)\. (.+) (?:dodge|dodges)\.$"
    :id     :melee-dodge
    :logfmt :melee-dodge
    :event  :melee-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :target-name      %2
              :avoidance-method :dodge)}

   {:regex  #"^(.+) (?:attack|attacks)\. (.+) (?:block|blocks)\.$"
    :id     :melee-block
    :logfmt :melee-block
    :event  :melee-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :target-name      %2
              :avoidance-method :block)}

   {:regex  #"^(.+) (?:attack|attacks)\. (.+) (?:evade|evades)\.$"
    :id     :melee-evade
    :logfmt :melee-evade
    :event  :melee-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :target-name      %2
              :avoidance-method :evade)}

   {:regex  #"^(.+) (?:attacks|attack) but (.+) is immune\.$"
    :id     :melee-immune
    :logfmt :melee-immune
    :event  :melee-avoided-by-target
    :args   #(hash-map
              :source-name      %1
              :target-name      %2
              :avoidance-method :immune)}

   ;;; ---------------------------------------------------------------------------------------------
   ;;; SPELL INTERRUPTION

   {:regex  #"^(.+?) (?:interrupts|interrupt) (.+?)'s (.+)\.$"
    :id     :skill-interrupt
    :logfmt :skill-interrupt
    :event  :skill-interrupted-by-target
    :args   #(hash-map
              :source-name %1
              :target-name %2
              :skill       %3)}

   {:regex  #"^(.+) (?:interrupts|interrupt) your (.+)\.$"
    :id     :skill-interrupt-self
    :logfmt :skill-interrupt
    :event  :skill-interrupted-by-target
    :args   #(hash-map
              :source-name %1
              :skill       %2
              :target-name "you")}

   ;;; ---------------------------------------------------------------------------------------------
   ;;; DOT
   ;;; Note that these are not used for just DoT's in the traditional sense (e.g. from applied debuffs, such as curses)
   ;;; but also channeled spells such as Blizzard / Arcane Missle ticks generate these same combat log events.

   {:regex  #"^(.+?) (?:suffers|suffer) (\d+) (.+?) damage from (.+?)'s (.+)\.(?: \((\d+) resisted\))?(?: \((\d+) absorbed\))?$"
    :id     :dot-damages-target
    :logfmt :dot-damages-target
    :event  :dot-damages-target
    :args   #(hash-map
              :target-name %1
              :damage      (->int %2)
              :damage-type (->kw %3)
              :source-name %4
              :skill       %5
              :resisted    (->int %6)
              :absorbed    (->int %7))}

   {:regex  #"^(.+) (?:suffers|suffer) (\d+) (.+) damage from your (.+)\.(?: \((\d+) resisted\))?(?: \((\d+) absorbed\))?$"
    :id     :dot-damages-target-self
    :logfmt :dot-damages-target
    :event  :dot-damages-target
    :args   #(hash-map
              :target-name %1
              :damage      (->int %2)
              :damage-type (->kw %3)
              :skill       %4
              :resisted    (->int %5)
              :absorbed    (->int %6)
              :source-name "you")}

   ;;; ---------------------------------------------------------------------------------------------
   ;;; CAST NOTIFICATION / INSTANT CAST ABILITIES

   ; always source != "you" .. ?
   {:regex  #"^(.+) (?:begins|begin) to (perform|cast) (.+)\.$"
    :id     :cast-begins
    :logfmt :cast-begins
    :event  :cast-begins
    :args   #(hash-map
              :source-name %1
              :spell?      (= %2 "cast")
              :skill       %3)}

   {:regex  #"^(.+) (cast|casts|performs|perform) (.+) on (.+): (.+)\.$"
    :id     :skill-performed-on-target
    :logfmt :skill-performed-on-target
    :event  :skill-performed-on-target
    :args   #(hash-map
              :source-name %1
              :spell?      (one-of? %2 "casts" "cast")
              :skill       %3
              :target-name %4
              :extra       %5)}

   {:regex  #"^(.+) (cast|casts|performs|perform) (.+) on (.+)\.$"
    :id     :skill-performed-on-target
    :logfmt :skill-performed-on-target
    :event  :skill-performed-on-target
    :args   #(hash-map
              :source-name %1
              :spell?      (one-of? %2 "casts" "cast")
              :skill       %3
              :target-name %4)}

   ; only for instant cast stuff .. ?
   {:regex  #"^(.+) (casts|cast|performs|perform) (.+)\.$"
    :id     :cast
    :logfmt :cast
    :event  :cast
    :args   #(hash-map
              :source-name %1
              :spell?      (one-of? %2 "casts" "cast")
              :skill       %3)}

   ;;; ---------------------------------------------------------------------------------------------
   ;;; DIRECT HEALING FROM SKILLS (not HoT's)

   {:regex  #"^Your (.+?)(?: (critically))? heals (.+) for (\d+)\.$"
    :id     :skill-heals-target-self
    :logfmt :skill-heals-target
    :event  :skill-heals-target
    :args   #(hash-map
              :skill       %1
              :crit?       (= %2 "critically")
              :target-name %3
              :amount      (->int %4)
              :source-name "you")}

   {:regex  #"^(.+?)'s (.+?)(?: (critically))? heals (.+) for (\d+)\.$"
    :id     :skill-heals-target
    :logfmt :skill-heals-target
    :event  :skill-heals-target
    :args   #(hash-map
              :source-name %1
              :skill       %2
              :crit?       (= %3 "critically")
              :target-name %4
              :amount      (->int %5))}

   ;;; ---------------------------------------------------------------------------------------------
   ;;; BONUS HEALING / MANA / RESOURCE REGEN (i.e. not heals, but received via other effects)

   {:regex  #"^(.+?) (?:gain|gains) (\d+) (health|Mana|Rage|Energy|Happiness) from (.+?)'s (.+)\.$"
    :id     :resource-gained-from-skill
    :logfmt :resource-gained-from-skill
    :event  :resource-gained
    :args   #(hash-map
              :target-name   %1
              :amount        (->int %2)
              :resource-type (->kw %3)
              :source-name   %4
              :skill         %5)}

   ; apparently for this one, target will never be "you" .. ?
   {:regex  #"^(.+) (?:gain|gains) (\d+) (health|Mana|Rage|Energy|Happiness) from your (.+)\.$"
    :id     :resource-gained-from-skill-self
    :logfmt :resource-gained-from-skill
    :event  :resource-gained
    :args   #(hash-map
              :target-name   %1
              :amount        (->int %2)
              :resource-type (->kw %3)
              :skill         %4
              :source-name   "you")}

   ; this seems to only ever be for target=you and never for health potions ? the name after
   ; "from" at the end always seems to refer to a skill, never an entity. "you" seems to always
   ; be the implied source entity... so basically, target=you and source=you always ... ?
   ; (for this line, seen mana potions, mana gems, talents that restore mana ...)
   {:regex  #"^(.+) (?:gain|gains) (\d+) (health|Mana|Rage|Energy|Happiness) from (.+)\.$"
    :id     :resource-gained
    :logfmt :resource-gained
    :event  :resource-gained
    :args   #(hash-map
              :target-name   %1
              :amount        (->int %2)
              :resource-type (->kw %3)
              :skill         %4
              :source-name   %1)}

   {:regex  #"^(.+?)'s (.+) drains (\d+) (health|Mana|Rage|Energy|Happiness) from (.+)\.$"
    :id     :resource-drained-from-skill
    :logfmt :resource-drained-from-skill
    :event  :resource-lost
    :args   #(hash-map
              :source-name   %1
              :skill         %2
              :amount        (->int %3)
              :resource-type (->kw %4)
              :target-name   %5)}

   ; TODO: how does this one look if target=you? does the end bit after "from" just say "you"?
   {:regex  #"^Your (.+) drains (\d+) (health|Mana|Rage|Energy|Happiness) from (.+)\.$"
    :id     :resource-drained-from-skill-self
    :logfmt :resource-drained-from-skill
    :event  :resource-lost
    :args   #(hash-map
              :skill         %1
              :amount        (->int %2)
              :resource-type (->kw %3)
              :target-name   %4
              :source-name   "you")}


   ;;; ---------------------------------------------------------------------------------------------
   ;;; OTHER SPECIAL ABILITY/BUFF GAINS
   ;;; note that these are for things like skill procs or whatever that show up in the combat log
   ;;; with text similar to that of buffs/debuffs but these are not buffs/debuffs. they need to be
   ;;; caught first or they will be misinterpreted as buffs by the regex's in the next section

   {:regex  #"^(.+) (?:gain|gains) (.+) through (.+)\.$"
    :id     :special-gained
    :logfmt :special-gained
    :event  :special-gained
    :args   #(hash-map
              :target-name %1
              :special     %2
              ; NOTE: could be an entity name or a skill/talent name
              :source      %3)}

   ;;; ---------------------------------------------------------------------------------------------
   ;;; BUFF/DEBUFF

   {:regex  #"^(.+) (?:gain|gains) (.+?)(?: \((\d+)\))?\.$"
    :id     :aura-buff-gained
    :logfmt :aura-buff-gained
    :event  :aura-gained
    :args   #(hash-map
              :target-name %1
              :aura-name   %2
              :stacks      (->int %3)
              :aura-type   :buff)}

   {:regex  #"^(.+) (?:is|are) afflicted by (.+?)(?: \((\d+)\))?\.$"
    :id     :aura-debuff-gained
    :logfmt :aura-debuff-gained
    :event  :aura-gained
    :args   #(hash-map
              :target-name %1
              :aura-name   %2
              :stacks      (->int %3)
              :aura-type   :debuff)}

   {:regex  #"^(.+) fades from (.+)\.$"
    :id     :aura-fades
    :logfmt :aura-fades
    :event  :aura-lost
    :args   #(hash-map
              :aura-name   %1
              :target-name %2
              :faded?      true)}

   {:regex  #"^Your (.+) is removed\.$"
    :id     :aura-removed-self
    :logfmt :aura-removed
    :event  :aura-lost
    :args   #(hash-map
              :aura-name   %1
              :target-name "you"
              :faded?      false)}

   {:regex  #"^(.+?)'s (.+) is removed\.$"
    :id     :aura-removed
    :logfmt :aura-removed
    :event  :aura-lost
    :args   #(hash-map
              :target-name %1
              :aura-name   %2
              :faded?      false)}

   ;;; ---------------------------------------------------------------------------------------------
   ;;; ENVIRONMENTAL / OTHER DAMAGE

   {:regex  #"^(.+) (?:suffers|suffer) (\d+) points of (.+) damage\.(?: \((\d+) resisted\))?(?: \((\d+) absorbed\))?$"
    :id     :environmental-damage
    :logfmt :environmental-damage
    :event  :other-damage
    :args   #(hash-map
              :target-name %1
              :damage      (->int %2)
              :damage-type (->kw %3)
              :resisted    (->int %4)
              :absorbed    (->int %5))}

   {:regex  #"^(.+) (?:lose|loses) (\d+) health for swimming in lava\.(?: \((\d+) resisted\))?(?: \((\d+) absorbed\))?$"
    :id     :lava-swim-damage
    :logfmt :lava-swim-damage
    :event  :other-damage
    :args   #(hash-map
              :target-name %1
              :damage      (->int %2)
              :resisted    (->int %3)
              :absorbed    (->int %4)
              :damage-type :fire
              :source      "Swimming in lava")}

   {:regex  #"^(.+) (?:fall|falls) and (?:lose|loses) (\d+) health\.$"
    :id     :fall-damage
    :logfmt :fall-damage
    :event  :other-damage
    :args   #(hash-map
              :damage      (->int %2)
              :target-name %1
              :source      "Falling")}

   ;;; ---------------------------------------------------------------------------------------------
   ;;; DEATH

   {:regex  #"^(.+) (?:die|dies)\.$"
    :id     :death
    :logfmt :death
    :event  :death
    :args   #(hash-map
              :source-name %1)}

   ])
