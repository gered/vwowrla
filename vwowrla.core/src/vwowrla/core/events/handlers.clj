(ns vwowrla.core.events.handlers
  (:require
    [schema.core :as s]
    [vwowrla.core.encounters.analysis :as analysis])
  (:use
    vwowrla.core.schemas))

(defmulti handle-event
  (fn [{:keys [event]} _]
    (keyword event)))

(s/defmethod handle-event :skill-damage-to-target :- RaidAnalysis
  [{:keys [source-name skill target-name damage damage-type absorbed resisted blocked crit? timestamp]} :- CombatEvent
   data :- RaidAnalysis]
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
    data))

(s/defmethod handle-event :skill-avoided-by-target :- RaidAnalysis
  [{:keys [source-name target-name skill avoidance-method timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  (analysis/process-source-to-target-damage
    source-name
    target-name
    {:skill            skill
     :actual-skill?    true
     :avoidance-method avoidance-method}
    timestamp
    data))

(s/defmethod handle-event :damage-reflected :- RaidAnalysis
  [{:keys [source-name target-name damage damage-type timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  (analysis/process-source-to-target-damage
    source-name
    target-name
    {:skill         "Reflect"
     :actual-skill? false
     :damage        damage
     :damage-type   damage-type
     :crit?         false}
    timestamp
    data))

(s/defmethod handle-event :melee-damage-to-target :- RaidAnalysis
  [{:keys [source-name target-name damage damage-type hit-type absorbed resisted blocked crit? timestamp]} :- CombatEvent
   data :- RaidAnalysis]
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
    data))

(s/defmethod handle-event :melee-avoided-by-target :- RaidAnalysis
  [{:keys [source-name target-name avoidance-method timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  (analysis/process-source-to-target-damage
    source-name
    target-name
    {:skill            "Melee"
     :actual-skill?    false
     :avoidance-method avoidance-method}
    timestamp
    data))

(s/defmethod handle-event :skill-interrupted-by-target :- RaidAnalysis
  [{:keys [source-name target-name skill timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  data)

(s/defmethod handle-event :dot-damages-target :- RaidAnalysis
  [{:keys [source-name skill target-name damage damage-type absorbed resisted timestamp]} :- CombatEvent
   data :- RaidAnalysis]
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
    data))

(s/defmethod handle-event :cast-begins :- RaidAnalysis
  [{:keys [source-name skill spell? timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  ; don't think we really care about this ?
  data)

(s/defmethod handle-event :skill-performed-on-target :- RaidAnalysis
  [{:keys [source-name target-name skill spell? extra timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  (analysis/process-source-to-target-cast source-name target-name skill timestamp data))

(s/defmethod handle-event :cast :- RaidAnalysis
  [{:keys [source-name skill spell? timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  (analysis/process-entity-cast source-name skill timestamp data))

(s/defmethod handle-event :skill-heals-target :- RaidAnalysis
  [{:keys [source-name skill crit? target-name amount timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  data)

(s/defmethod handle-event :resource-gained :- RaidAnalysis
  [{:keys [target-name amount resource-type source-name skill timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  data)

(s/defmethod handle-event :resource-lost :- RaidAnalysis
  [{:keys [target-name amount resource-type source-name skill timestamp]} :- CombatEvent
   data :- RaidAnalysis]
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
              data)
    data))

(s/defmethod handle-event :special-gained :- RaidAnalysis
  [{:keys [target-name special source timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  data)

(s/defmethod handle-event :aura-gained :- RaidAnalysis
  [{:keys [target-name aura-name aura-type stacks timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  data)

(s/defmethod handle-event :aura-lost :- RaidAnalysis
  [{:keys [target-name aura-name faded? stacks timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  data)

(s/defmethod handle-event :other-damage :- RaidAnalysis
  [{:keys [target-name damage damage-type resisted absorbed source timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  data)

(s/defmethod handle-event :death :- RaidAnalysis
  [{:keys [source-name timestamp]} :- CombatEvent
   data :- RaidAnalysis]
  (analysis/process-entity-death source-name timestamp data))


(s/defmethod handle-event :ignored :- RaidAnalysis
  [{:keys [line]} :- CombatEvent
   data :- RaidAnalysis]
  data)

(s/defmethod handle-event :default :- RaidAnalysis
  [{:keys [line]} :- CombatEvent
   data :- RaidAnalysis]
  (println "[WARN] *** UNRECOGNIZED ***" line)
  data)
