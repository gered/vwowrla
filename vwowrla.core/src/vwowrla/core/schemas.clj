(ns vwowrla.core.schemas
  (:require
    [schema.core :as s])
  (:use
    vwowrla.core.schema-utils))

;; constants

(def damage-types      [:physical :arcane :fire :frost :nature :shadow :holy])
(def avoidance-methods [:miss :parry :dodge :block :evade :immune :resist :absorb])
(def hit-types         [:normal :glancing :crushing])
(def resource-types    [:health :mana :rage :energy :happiness])
(def aura-types        [:buff :debuff])

;; model schemas

(def Milliseconds java.lang.Long)
(def UnixTimestamp java.lang.Long)

(def ParserOptions
  {:log-owner-char-name       s/Str
   :year                      s/Int
   :timezone                  java.util.TimeZone
   (s/optional-key :windows?) s/Bool})

(def CombatEvent
  {:id                                s/Keyword
   :logfmt                            s/Keyword
   :event                             s/Keyword
   :line                              s/Str
   :timestamp                         java.util.Date
   (s/optional-key :source-name)      s/Str
   (s/optional-key :source)           s/Str                 ; could be an entity name, a skill/talent name, or even something else
   (s/optional-key :target-name)      s/Str
   (s/optional-key :skill)            s/Str
   (s/optional-key :crit?)            s/Bool
   (s/optional-key :damage)           s/Num
   (s/optional-key :damage-type)      (one-of damage-types)
   (s/optional-key :resisted)         (s/maybe s/Num)
   (s/optional-key :absorbed)         (s/maybe s/Num)
   (s/optional-key :blocked)          (s/maybe s/Num)
   (s/optional-key :avoidance-method) (one-of avoidance-methods)
   (s/optional-key :hit-type)         (one-of hit-types)
   (s/optional-key :spell?)           s/Bool
   (s/optional-key :extra)            s/Str
   (s/optional-key :amount)           s/Num
   (s/optional-key :resource-type)    (one-of resource-types)
   (s/optional-key :special)          s/Str
   (s/optional-key :aura-name)        s/Str
   (s/optional-key :aura-type)        (one-of aura-types)
   (s/optional-key :stacks)           (s/maybe s/Num)
   (s/optional-key :faded?)           s/Bool})

; TODO
(def SkillStatistics
  {s/Any s/Any})

; TODO
(def Entity
  {s/Any s/Any})

; TODO
(def Encounter
  {s/Any s/Any})

; TODO
(def RaidAnalysis
  {:encounters       [Encounter]
   :active-encounter (s/maybe Encounter)})

; TODO
(def DamageProperties
  {s/Any s/Any})