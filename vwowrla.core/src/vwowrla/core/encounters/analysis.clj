(ns vwowrla.core.encounters.analysis
  (:import
    (java.util Date))
  (:require
    [clojure.tools.logging :refer [info warn error]]
    [schema.core :as s])
  (:use
    vwowrla.core.encounters.core
    vwowrla.core.schemas
    vwowrla.core.utils))

(s/defn active-encounter? :- s/Bool
  "returns true if the log analysis data contains an active encounter"
  [data :- RaidAnalysis]
  (not (nil? (:active-encounter data))))

(s/defn touch-entity :- Encounter
  "updates an entity within the encounter by resetting it's :last-activity-at timestamp to the timestamp
   provided, or adds a new entity under the given name to the active encounter if it does not already
   exist. returns encounter with the updated entity information."
  [encounter   :- Encounter
   entity-name :- s/Str
   timestamp   :- Date]
  (if-not (get-in encounter [:entities entity-name])
    (assoc-in encounter [:entities entity-name]
              {:name             entity-name
               :added-at         timestamp
               :last-activity-at timestamp
               :damage           {:in  {}
                                  :out {}}
               :skill-uses       {}
               :alive-dps        0
               :encounter-dps    0
               :deaths           []
               :resurrections    []
               :alive-duration   0})
    (assoc-in encounter [:entities entity-name :last-activity-at] timestamp)))

(s/defn get-entity-last-activity :- (s/maybe Date)
  [entity-name :- s/Str
   encounter   :- Encounter]
  "returns timestamp of the given entity's last activity (that is, the timestamp of the most
   recent combat event that was regarding the named entity)"
  (get-in encounter [:entities entity-name :last-activity-at]))

(s/defn get-entity-alive-time :- Long
  "returns the total time that the given entity was alive for in the encounter (time in milliseconds)"
  [{:keys [deaths resurrections]}              :- Entity
   {:keys [started-at ended-at] :as encounter} :- Encounter]
  (if (and (= 0 (count deaths))
           (= 0 (count resurrections)))
    (:duration encounter)
    (let [segments (->> (concat
                          (map (fn [death] {:status :dead :at (:timestamp death)}) deaths)
                          (map (fn [resurrection] {:status :alive :at (:timestamp resurrection)}) resurrections)
                          [{:status :end :at ended-at}])
                        (remove empty?)
                        (sort-by :at))]
      (reduce
        (fn [{:keys [total current-status from] :as result} {:keys [status at]}]
          (cond
            ; is the first state change we find a resurrect? (that is, they were dead when the fight began)
            ; NOTE: technically this could also happen if for some reason the combat log missed a previous death event
            (and (nil? current-status)
                 (= :alive status))
            (assoc result
              :current-status :alive
              :from at)

            ; first state change we find is a death
            (and (nil? current-status)
                 (= :dead status))
            (assoc result
              :current-status :dead
              :from at
              :total (+ total (time-between from at)))

            ; resurrected after a death during the encounter
            (and (= :dead current-status)
                 (= :alive status))
            (assoc result
              :current-status :alive
              :from at)

            ; another death state when already dead? this can happen if there are multiple entities in the encounter
            ; with the same name. pretty much impossible to separate them with just the info in the combat log
            ; available to us. so, we just tack on the time since the last death since at least one of the entities
            ; was alive for that entire time period. in this way, the "entity alive time" for the entity with this
            ; name will just be a counter of "at least one entity with this name was alive"
            ; NOTE: technically this "double death" thing could also happen for entities for which there really is
            ;       only one if for some reason the combat log missed a previous resurrect event
            (and (= :dead current-status)
                 (= :dead status))
            (assoc result
              :from at
              :total (+ total (time-between from at)))

            ; fight has ended (always should be the last iteration, just return the total value here)
            (= :end status)
            (if (= :dead current-status)
              total
              (+ total (time-between from at)))))
        {:total 0
         :from started-at}
        segments))))

(s/defn finalize-entity-auras :- Entity
  [entity    :- Entity
   timestamp :- Date]
  ; TODO
  entity)

(s/defn finalize-entities :- Encounter
  [encounter :- Encounter]
  (-> encounter
      (update-all-entities
        #(assoc % :alive-duration (get-entity-alive-time % encounter)))
      (update-all-entities
        #(assoc %
          :encounter-dps (Math/round ^double
                                     (/ (get-in % [:damage :out :total])
                                        (/ (:duration encounter)
                                           1000)))
          :alive-dps (Math/round ^double
                                 (/ (get-in % [:damage :out :total])
                                    (/ (:alive-duration %)
                                       1000)))))
      (update-all-entities finalize-entity-auras (:ended-at encounter))))

(s/defn calculate-encounter-stats :- Encounter
  [encounter :- Encounter]
  (let [{:keys [started-at ended-at]} encounter]
    (-> encounter
        (assoc :duration (time-between started-at ended-at))
        (finalize-entities))))

(s/defn count-currently-dead :- s/Num
  [encounter   :- Encounter
   entity-name :- s/Str]
  (if-let [entity (get-in encounter [:entities entity-name])]
    (let [num-deaths     (count (:deaths entity))
          num-resurrects (count (:resurrections entity))]
      (- num-deaths num-resurrects))
    0))

;; damage

(s/defn update-damage-averages :- DamageStatistics
  [{:keys [num-hits total-hit-damage total-crit-damage num-crits] :as totals} :- DamageStatistics]
  (-> totals
      (update-in [:average-hit] #(if (> num-hits 0) (int (/ total-hit-damage num-hits)) %))
      (update-in [:average-crit] #(if (> num-crits 0) (int (/ total-crit-damage num-crits)) %))))

(s/defn update-damage-statistics :- DamageStatistics
  [totals :- (s/maybe DamageStatistics)
   {:keys [damage hit-type crit? partial-absorb partial-resist partial-block avoidance-method]} :- DamageProperties]
  (let [damage (or damage 0)]
    (-> (or totals {:damage             0
                    :max-hit            0
                    :min-hit            0
                    :total-hit-damage   0
                    :average-hit        0
                    :max-crit           0
                    :min-crit           0
                    :total-crit-damage  0
                    :average-crit       0
                    :num-total-hits     0
                    :num-hits           0
                    :num-crits          0
                    :num-glancing       0
                    :num-crushing       0
                    :num-partial-absorb 0
                    :num-partial-resist 0
                    :num-partial-block  0
                    :num-miss           0
                    :num-dodge          0
                    :num-parry          0
                    :num-resist         0
                    :num-absorb         0
                    :num-evade          0
                    :num-immune         0})
        (update-in [:damage] #(+ % damage))
        (update-in [:max-hit] #(if (and (not crit?) (not avoidance-method)) (max % damage) %))
        (update-in [:min-hit] #(if (and (not crit?) (not avoidance-method)) (if (> damage 0) (if (= % 0) damage (min % damage)) %) %))
        (update-in [:total-hit-damage] #(if (and (not crit?) (not avoidance-method)) (+ % damage) %))
        (update-in [:max-crit] #(if (and crit? (not avoidance-method)) (max % damage) %))
        (update-in [:min-crit] #(if (and crit? (not avoidance-method)) (if (> damage 0) (if (= % 0) damage (min % damage)) %) %))
        (update-in [:total-crit-damage] #(if (and crit? (not avoidance-method)) (+ % damage) %))
        (update-in [:num-total-hits] #(if-not avoidance-method (inc %) %))
        (update-in [:num-hits] #(if (and (not avoidance-method) (not crit?)) (inc %) %))
        (update-in [:num-crits] #(if (and (not avoidance-method) crit?) (inc %) %))
        (update-in [:num-glancing] #(if (= hit-type :glancing) (inc %) %))
        (update-in [:num-crushing] #(if (= hit-type :crushing) (inc %) %))
        (update-in [:num-partial-absorb] #(if partial-absorb (inc %) %))
        (update-in [:num-partial-resist] #(if partial-resist (inc %) %))
        (update-in [:num-partial-block] #(if partial-block (inc %) %))
        (update-in [:num-miss] #(if (= avoidance-method :miss) (inc %) %))
        (update-in [:num-dodge] #(if (= avoidance-method :dodge) (inc %) %))
        (update-in [:num-parry] #(if (= avoidance-method :parry) (inc %) %))
        (update-in [:num-resist] #(if (= avoidance-method :resist) (inc %) %))
        (update-in [:num-absorb] #(if (= avoidance-method :absorb) (inc %) %))
        (update-in [:num-evade] #(if (= avoidance-method :evade) (inc %) %))
        (update-in [:num-immune] #(if (= avoidance-method :immune) (inc %) %))
        (update-damage-averages))))

(s/defn update-entity-damage-stats :- Encounter
  [encounter               :- Encounter
   in-or-out               :- s/Keyword
   entity-name             :- s/Str
   {:keys [skill damage damage-type]
    :as damage-properties} :- DamageProperties
   timestamp               :- Date]
  (-> encounter
      (update-entity-field entity-name [:damage in-or-out :total] #(if damage (+ (or % 0) damage) %))
      (update-entity-field entity-name [:damage in-or-out :totals-by-type damage-type] #(if damage (+ (or % 0) damage) %))
      (update-entity-field entity-name [:damage in-or-out skill] #(update-damage-statistics % damage-properties))))

(s/defn update-damage-stats :- Encounter
  [encounter               :- Encounter
   source-entity-name      :- s/Str
   target-entity-name      :- s/Str
   {:keys [skill damage damage-type]
    :as damage-properties} :- DamageProperties
   timestamp               :- Date]
   (let [k         {:source source-entity-name
                    :target target-entity-name
                    :skill  skill}
         ; if the target is an enemy, then this damage is "out" (as in, outgoing damage from the raid)
         in-or-out (if (enemy-entity? target-entity-name) :out :in)]
     (-> encounter
         (update-in [:damage in-or-out :total] #(if damage (+ (or % 0) damage) %))
         (update-in [:damage in-or-out :totals-by-type damage-type] #(if damage (+ (or % 0) damage) %))
         (update-in [:damage in-or-out skill] #(update-damage-statistics % damage-properties))
         (update-in [:damage k] #(update-damage-statistics % damage-properties)))))

;; healing

(s/defn update-healing-averages :- HealingStatistics
  [{:keys [num-normal num-crits total-normal-amount total-crit-amount] :as totals} :- HealingStatistics]
  (-> totals
      (update-in [:average-normal] #(if (> num-normal 0) (int (/ total-normal-amount num-normal)) %))
      (update-in [:average-crit] #(if (> num-crits 0) (int (/ total-crit-amount num-crits)) %))))

(s/defn update-healing-statistics :- HealingStatistics
  [totals :- (s/maybe HealingStatistics)
   {:keys [amount crit?]} :- HealProperties]
  (let [amount (or amount 0)]
    (-> (or totals {:amount              0
                    :max-normal          0
                    :min-normal          0
                    :total-normal-amount 0
                    :average-normal      0
                    :max-crit            0
                    :min-crit            0
                    :total-crit-amount   0
                    :average-crit        0
                    :num-total-heals     0
                    :num-normal          0
                    :num-crits           0})
        (update-in [:amount] #(+ % amount))
        (update-in [:max-normal] #(if-not crit? (max % amount) %))
        (update-in [:min-normal] #(if-not crit? (if (> amount 0) (if (= % 0) amount (min % amount)) %) %))
        (update-in [:total-normal-amount] #(if-not crit? (+ % amount) %))
        (update-in [:max-crit] #(if crit? (max % amount) %))
        (update-in [:min-crit] #(if crit? (if (> amount 0) (if (= % 0) amount (min % amount)) %) %))
        (update-in [:total-crit-amount] #(if crit? (+ % amount) %))
        (update-in [:num-total-heals] #(inc %))
        (update-in [:num-normal] #(if-not crit? (inc %) %))
        (update-in [:num-crits] #(if crit? (inc %) %))
        (update-healing-averages))))

(s/defn update-entity-healing-stats :- Encounter
  [encounter             :- Encounter
   in-or-out             :- s/Keyword
   entity-name           :- s/Str
   {:keys [skill amount]
    :as heal-properties} :- HealProperties
   timestamp             :- Date]
  (-> encounter
      (update-entity-field entity-name [:healing in-or-out :total] #(if amount (+ (or % 0) amount) %))
      (update-entity-field entity-name [:healing in-or-out skill] #(update-healing-statistics % heal-properties))))

(s/defn update-healing-stats :- Encounter
  [encounter             :- Encounter
   source-entity-name    :- s/Str
   target-entity-name    :- s/Str
   {:keys [skill amount]
    :as heal-properties} :- HealProperties
   timestamp             :- Date]
  (let [k    {:source source-entity-name
              :target target-entity-name
              :skill  skill}
        type (if (enemy-entity? target-entity-name) :hostile :friendly)]
    (-> encounter
        (update-in [:healing type :total] #(if amount (+ (or % 0) amount) %))
        (update-in [:healing type skill] #(update-healing-statistics % heal-properties))
        (update-in [:healing k] #(update-healing-statistics % heal-properties)))))

;; --

(s/defn update-skill-use-count :- Encounter
  [encounter          :- Encounter
   source-entity-name :- s/Str
   target-entity-name :- (s/maybe s/Str)
   skill              :- s/Str
   timestamp          :- Date]
  (-> encounter
      (update-entity-field source-entity-name [:skill-uses skill] #(conj % {:timestamp timestamp :target target-entity-name}))))

;;;
;;; main combat log entry processing entry points
;;;

(s/defn process-source-to-target-damage :- Encounter
  [source-name             :- s/Str
   target-name             :- s/Str
   {:keys [skill]
    :as damage-properties} :- DamageProperties
   timestamp               :- Date
   encounter               :- Encounter]
  (-> encounter
      (touch-entity source-name timestamp)
      (touch-entity target-name timestamp)
      (update-skill-use-count source-name target-name skill timestamp)
      (update-damage-stats source-name target-name damage-properties timestamp)
      (update-entity-damage-stats :in target-name damage-properties timestamp)
      (update-entity-damage-stats :out source-name damage-properties timestamp)))

(s/defn process-source-to-target-healing :- Encounter
  [source-name           :- s/Str
   target-name           :- s/Str
   {:keys [skill]
    :as heal-properties} :- HealProperties
   timestamp             :- Date
   encounter             :- Encounter]
  (-> encounter
      (touch-entity source-name timestamp)
      (touch-entity target-name timestamp)
      (update-skill-use-count source-name target-name skill timestamp)
      (update-healing-stats source-name target-name heal-properties timestamp)
      (update-entity-healing-stats :in target-name heal-properties timestamp)
      (update-entity-healing-stats :out source-name heal-properties timestamp)))

(s/defn process-entity-death :- Encounter
  [entity-name :- s/Str
   timestamp   :- Date
   encounter   :- Encounter]
  (-> encounter
      (touch-entity entity-name timestamp)
      (update-entity-field entity-name [:deaths] #(conj % {:timestamp timestamp}))
      (update-entity entity-name finalize-entity-auras timestamp)))

(s/defn process-source-to-target-cast :- Encounter
  [source-name :- s/Str
   target-name :- s/Str
   skill-name  :- s/Str
   timestamp   :- Date
   encounter   :- Encounter]
  (-> encounter
      (update-skill-use-count source-name target-name skill-name timestamp)))

(s/defn process-entity-cast :- Encounter
  [entity-name :- s/Str
   skill-name  :- s/Str
   timestamp   :- Date
   encounter   :- Encounter]
  (-> encounter
      (update-skill-use-count entity-name nil skill-name timestamp)))

(s/defn begin-encounter :- RaidAnalysis
  "sets up a new active encounter in the parsed data, returning the new parsed data set ready to use for
   parsing a new encounter."
  [encounter-name           :- s/Str
   {:keys [timestamp line]} :- CombatEvent
   data                     :- RaidAnalysis]
  (info "Beginning encounter" (str "\"" encounter-name "\"") "detected on line:" line)
  (assoc data :active-encounter
              {:name             encounter-name
               :started-at       timestamp
               :entities         {}
               :skills           {}
               :damage           {:in {}
                                  :out {}}
               :healing          {}
               :trigger-entities (get-in defined-encounters [encounter-name :entities])}))

(s/defn end-encounter :- RaidAnalysis
  "ends the current active encounter in the parsed data, moving it from :active-encounter and inserted it into the
   end of the :encounters list. finalizes the encounter by performing various final entity statistic calculations and
   marks the encounter as successful or not. returns the new parsed data set without any active encounter set."
  [{:keys [timestamp line]} :- CombatEvent
   encounter-end-cause      :- s/Keyword
   data                     :- RaidAnalysis]
  (let [wipe-or-timeout? (= encounter-end-cause :wipe-or-timeout)]
    (info "Ending encounter" (str "\"" (get-in data [:active-encounter :name]) "\"") "detected on line:" line)
    (if wipe-or-timeout?
      (info "Encounter ending due to wipe or trigger entity activity timeout (unsuccessful encounter kill attempt)."))
    (let [data (-> data
                   (update-active-encounter assoc :ended-at timestamp)
                   (update-active-encounter assoc :wipe-or-timeout? wipe-or-timeout?)
                   (update-active-encounter calculate-encounter-stats))]
      (-> data
          (assoc-in [:active-encounter] nil)
          (update-in [:encounters] #(conj %1 (:active-encounter data)))))))