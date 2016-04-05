(ns vwowrla.core.events.handlers
  (:require
    [schema.core :as s]
    [vwowrla.core.encounters.analysis :as analysis])
  (:use
    vwowrla.core.schemas))

(defmulti handle-event
  (fn [{:keys [event]} _]
    (keyword event)))

(s/defmethod handle-event :skill-damage-to-target :- Encounter
  [{:keys [source-name skill target-name damage damage-type absorbed resisted blocked crit? timestamp]} :- CombatEvent
   encounter :- Encounter]
  (analysis/process-source-to-target-damage
    source-name
    target-name
    {:skill          skill
     :actual-skill?  true
     :damage         damage
     :damage-type    (or damage-type :physical)
     :crit?          crit?
     :partial-absorb absorbed
     :partial-resist resisted
     :partial-block  blocked}
    timestamp
    encounter))

(s/defmethod handle-event :skill-avoided-by-target :- Encounter
  [{:keys [source-name target-name skill avoidance-method timestamp]} :- CombatEvent
   encounter :- Encounter]
  (analysis/process-source-to-target-damage
    source-name
    target-name
    {:skill            skill
     :actual-skill?    true
     :avoidance-method avoidance-method}
    timestamp
    encounter))

(s/defmethod handle-event :damage-reflected :- Encounter
  [{:keys [source-name target-name damage damage-type timestamp]} :- CombatEvent
   encounter :- Encounter]
  (analysis/process-source-to-target-damage
    source-name
    target-name
    {:skill         "Reflect"
     :actual-skill? false
     :damage        damage
     :damage-type   damage-type
     :crit?         false}
    timestamp
    encounter))

(s/defmethod handle-event :melee-damage-to-target :- Encounter
  [{:keys [source-name target-name damage damage-type hit-type absorbed resisted blocked crit? timestamp]} :- CombatEvent
   encounter :- Encounter]
  (analysis/process-source-to-target-damage
    source-name
    target-name
    {:skill          "Melee"
     :actual-skill?  false
     :damage         damage
     :damage-type    (or damage-type :physical)
     :hit-type       hit-type
     :crit?          crit?
     :partial-absorb absorbed
     :partial-resist resisted
     :partial-block  blocked}
    timestamp
    encounter))

(s/defmethod handle-event :melee-avoided-by-target :- Encounter
  [{:keys [source-name target-name avoidance-method timestamp]} :- CombatEvent
   encounter :- Encounter]
  (analysis/process-source-to-target-damage
    source-name
    target-name
    {:skill            "Melee"
     :actual-skill?    false
     :avoidance-method avoidance-method}
    timestamp
    encounter))

(s/defmethod handle-event :skill-interrupted-by-target :- Encounter
  [{:keys [source-name target-name skill timestamp]} :- CombatEvent
   encounter :- Encounter]
  encounter)

(s/defmethod handle-event :dot-damages-target :- Encounter
  [{:keys [source-name skill target-name damage damage-type absorbed resisted timestamp]} :- CombatEvent
   encounter :- Encounter]
  (analysis/process-source-to-target-damage
    source-name
    target-name
    {:skill          skill
     :actual-skill?  true
     :damage         damage
     :damage-type    damage-type
     :crit?          false
     :partial-absorb absorbed
     :partial-resist resisted}
    timestamp
    encounter))

(s/defmethod handle-event :cast-begins :- Encounter
  [{:keys [source-name skill spell? timestamp]} :- CombatEvent
   encounter :- Encounter]
  ; don't think we really care about this ?
  encounter)

(s/defmethod handle-event :skill-performed-on-target :- Encounter
  [{:keys [source-name target-name skill spell? extra timestamp]} :- CombatEvent
   encounter :- Encounter]
  (analysis/process-source-to-target-cast source-name target-name skill timestamp encounter))

(s/defmethod handle-event :cast :- Encounter
  [{:keys [source-name skill spell? timestamp]} :- CombatEvent
   encounter :- Encounter]
  (analysis/process-entity-cast source-name skill timestamp encounter))

(s/defmethod handle-event :skill-heals-target :- Encounter
  [{:keys [source-name skill crit? target-name amount timestamp]} :- CombatEvent
   encounter :- Encounter]
  (analysis/process-source-to-target-healing
    source-name
    target-name
    {:skill         skill
     :actual-skill? true
     :amount        amount
     :crit?         crit?}
    timestamp
    encounter))

(s/defmethod handle-event :resource-gained :- Encounter
  [{:keys [target-name amount resource-type source-name skill timestamp]} :- CombatEvent
   encounter :- Encounter]
  (condp = resource-type
    :health (analysis/process-source-to-target-healing
              source-name
              target-name
              {:skill         skill
               ;:actual-skill? true                          ; TODO: this is not always true. e.g. if a potion is used (how to determine this?)
               :amount        amount
               :crit?         false}
              timestamp
              encounter)
    encounter))

(s/defmethod handle-event :resource-lost :- Encounter
  [{:keys [target-name amount resource-type source-name skill timestamp]} :- CombatEvent
   encounter :- Encounter]
  (condp = resource-type
    :health (analysis/process-source-to-target-damage
              source-name
              target-name
              {:skill         skill
               :actual-skill? true
               :damage        amount
               :damage-type   :physical
               :crit?         false}
              timestamp
              encounter)
    encounter))

(s/defmethod handle-event :special-gained :- Encounter
  [{:keys [target-name special source timestamp]} :- CombatEvent
   encounter :- Encounter]
  encounter)

(s/defmethod handle-event :aura-gained :- Encounter
  [{:keys [target-name aura-name aura-type stacks timestamp]} :- CombatEvent
   encounter :- Encounter]
  encounter)

(s/defmethod handle-event :aura-lost :- Encounter
  [{:keys [target-name aura-name faded? stacks timestamp]} :- CombatEvent
   encounter :- Encounter]
  encounter)

(s/defmethod handle-event :other-damage :- Encounter
  [{:keys [target-name damage damage-type resisted absorbed source timestamp]} :- CombatEvent
   encounter :- Encounter]
  encounter)

(s/defmethod handle-event :death :- Encounter
  [{:keys [source-name timestamp]} :- CombatEvent
   encounter :- Encounter]
  (analysis/process-entity-death source-name timestamp encounter))


(s/defmethod handle-event :ignored :- Encounter
  [{:keys [line]} :- CombatEvent
   encounter :- Encounter]
  encounter)

(s/defmethod handle-event :default :- Encounter
  [{:keys [line]} :- CombatEvent
   encounter :- Encounter]
  (println "[WARN] *** UNRECOGNIZED ***" line)
  encounter)
