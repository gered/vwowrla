(ns vwowrla.core.handlers
  (:require
    [vwowrla.core.encounters :as encounters]))

(defmulti handle-event
  (fn [{:keys [event]} _]
    (keyword event)))

(defmethod handle-event :skill-damage-to-target
  [{:keys [source-name skill target-name damage damage-type absorbed resisted blocked crit? timestamp] :as parsed} data]
  (encounters/process-source-to-target-damage
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

(defmethod handle-event :skill-avoided-by-target
  [{:keys [source-name target-name skill avoidance-method timestamp] :as parsed} data]
  (encounters/process-source-to-target-damage
    source-name
    target-name
    {:skill            skill
     :actual-skill?    true
     :avoidance-method avoidance-method}
    timestamp
    data))

(defmethod handle-event :damage-reflected
  [{:keys [source-name target-name damage damage-type timestamp] :as parsed} data]
  (encounters/process-source-to-target-damage
    source-name
    target-name
    {:skill         "Reflect"
     :actual-skill? false
     :damage        damage
     :damage-type   damage-type
     :crit?         false}
    timestamp
    data))

(defmethod handle-event :melee-damage-to-target
  [{:keys [source-name target-name damage damage-type hit-type absorbed resisted blocked crit? timestamp] :as parsed} data]
  (encounters/process-source-to-target-damage
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

(defmethod handle-event :melee-avoided-by-target
  [{:keys [source-name target-name avoidance-method timestamp] :as parsed} data]
  (encounters/process-source-to-target-damage
    source-name
    target-name
    {:skill            "Melee"
     :actual-skill?    false
     :avoidance-method avoidance-method}
    timestamp
    data))

(defmethod handle-event :skill-interrupted-by-target
  [{:keys [source-name target-name skill timestamp] :as parsed} data]
  data)

(defmethod handle-event :dot-damages-target
  [{:keys [source-name skill target-name damage damage-type absorbed resisted timestamp] :as parsed} data]
  (encounters/process-source-to-target-damage
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

(defmethod handle-event :cast-begins
  [{:keys [source-name skill spell? timestamp] :as parsed} data]
  ; don't think we really care about this ?
  data)

(defmethod handle-event :skill-performed-on-target
  [{:keys [source-name target-name skill spell? extra timestamp] :as parsed} data]
  (encounters/process-source-to-target-cast source-name target-name skill timestamp data))

(defmethod handle-event :cast
  [{:keys [source-name skill spell? timestamp] :as parsed} data]
  (encounters/process-entity-cast source-name skill timestamp data))

(defmethod handle-event :skill-heals-target
  [{:keys [source-name skill crit? target-name amount timestamp] :as parsed} data]
  data)

(defmethod handle-event :resource-gained
  [{:keys [target-name amount resource-type source-name skill timestamp] :as parsed} data]
  data)

(defmethod handle-event :resource-lost
  [{:keys [target-name amount resource-type source-name skill timestamp] :as parsed} data]
  (condp = resource-type
    :health (encounters/process-source-to-target-damage
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

(defmethod handle-event :special-gained
  [{:keys [target-name special source timestamp] :as parsed} data]
  data)

(defmethod handle-event :aura-gained
  [{:keys [target-name aura-name aura-type stacks timestamp] :as parsed} data]
  data)

(defmethod handle-event :aura-lost
  [{:keys [target-name aura-name faded? stacks timestamp] :as parsed} data]
  data)

(defmethod handle-event :other-damage
  [{:keys [target-name damage damage-type resisted absorbed source timestamp] :as parsed} data]
  data)

(defmethod handle-event :death
  [{:keys [source-name timestamp] :as parsed} data]
  (encounters/process-entity-death source-name timestamp data))


(defmethod handle-event :ignored
  [{:keys [line] :as parsed} data]
  #_(println "[WARN] *** IGNORED ***" line)
  data)

(defmethod handle-event :default
  [{:keys [line] :as parsed} data]
  (println "[WARN] *** UNRECOGNIZED ***" line)
  data)
