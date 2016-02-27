(ns vwowrla.core.encounters
  (:import
    (java.util Date))
  (:require
    [clojure.java.io :as io]
    [clojure.tools.logging :refer [info error]]
    [cheshire.core :as json])
  (:use
    vwowrla.core.utils))

(def defined-encounters (get-edn-resource "encounters.edn"))
(def non-combat-starting-auras (get-text-resource-as-lines "non_combat_starting_auras.txt"))
(def non-combat-starting-skills (get-text-resource-as-lines "non_combat_starting_skills.txt"))

(def wipe-or-timeout-period (* 60 1000))

(declare calculate-encounter-stats)
(declare count-currently-dead)
(declare get-entity-last-activity)

(defn find-defined-encounter-name
  "returns the name of a defined encounter which includes the given entity in it's
   list of trigger entities. returns nil if there is no encounter which includes the
   given entity"
  [entity-name]
  (->> defined-encounters
       (filter (fn [[_ {:keys [entities]}]]
                 (->> entities
                      (filter #(= (first %) entity-name))
                      (first))))
       (ffirst)))

(defn find-past-encounters
  "return a list of all previously parsed encounters (successful and not) matching the encounter name"
  [encounter-name data]
  (->> (:encounters data)
       (filter #(= (:name %) encounter-name))))

(defn any-successful-encounters?
  "returns true if there are any successful parsed encounters matching the encounter name"
  [encounter-name data]
  (->> (find-past-encounters encounter-name data)
       (map :wipe-or-timeout?)
       (filter false?)
       (empty?)
       (not)))

(defn update-active-encounter
  "updates the active encounter using function f which will take the current active
   encounter and any supplied args, returning a new active encounter which is
   'updated' in the original full parsed data and then finally returned."
  [data f & args]
  (apply update-in data [:active-encounter] f args))

(defn update-all-entities
  "updates all entities in the encounter using function f which takes the current
   entity and any supplied args, returning a new entity which is 'updated' in the
   original encounter. returns the encounter with the modified entity data."
  [encounter f & args]
  (reduce
    (fn [encounter [entity-name entity]]
      (assoc-in encounter [:entities entity-name] (apply f entity args)))
    encounter
    (:entities encounter)))

(defn update-all-active-encounter-entities
  "updates all entities in the current active encounter in the full parsed data
   using function f which takes the current entity and any supplied args, returning
   a new entity which is 'updated' in the original encounter. returns the updated
   full parsed data."
  [data f & args]
  (update-active-encounter data #(update-all-entities % f args)))

(defn update-entity
  "updates an entity in the full parsed data's active encounter using function f
   which takes the current entity and any supplied args, returning the new entity
   which is 'updated' in the active encounter. returns the updated full parsed data."
  [data entity-name f & args]
  (apply update-in data [:active-encounter :entities entity-name] f args))

(defn update-entity-field
  "updates a specific field within an entity pointed to by ks in the full parsed
   data's active encounter using function f which takes the current entity and any
   supplied args, returning the new entity which is 'updated' in the active encounter.
   returns the updated full parsed data."
  [data entity-name ks f & args]
  (apply update-in data (concat [:active-encounter :entities entity-name] ks) f args))

(defn- ignore-interaction?
  [entity-name ignore-entity-list {:keys [target-name source-name] :as parsed-line}]
  (and (or (= entity-name target-name)
           (= entity-name source-name))
       (or (contained-in? target-name ignore-entity-list)
           (contained-in? source-name ignore-entity-list))))

(defn ignored-interaction-event?
  "returns true if the given parsed combat log line is between entities that have
   been specified to ignore interactions between for the purposes of detecting
   an encounter trigger"
  [encounter parsed-line]
  (->> (:entities encounter)
       (filter
         (fn [[entity-name entity-props]]
           (seq (:ignore-interactions-with entity-props))))
       (filter
         (fn [[entity-name entity-props]]
           (ignore-interaction? entity-name (:ignore-interactions-with entity-props) parsed-line)))
       (seq)))

(defn- ignore-skill?
  [entity-name ignore-skill-list {:keys [source-name skill] :as parsed-line}]
  (and (= entity-name source-name)
       (contained-in? skill ignore-skill-list)))

(defn ignored-skill-event?
  "returns true if the given parsed combat log line is for an encounter entity
   that is using a skill that has been specifically indicated should be ignored
   for the purposes of triggering an encounter"
  [encounter parsed-line]
  (->> (:entities encounter)
       (filter
         (fn [[entity-name entity-props]]
           (seq (:ignore-skills entity-props))))
       (filter
         (fn [[entity-name entity-props]]
           (ignore-skill? entity-name (:ignore-skills entity-props) parsed-line)))
       (seq)))

;;;
;;; encounter start/stop
;;;

(defn detect-encounter-triggered
  "determines if the parsed combat log line is for an event involving any specific encounter entities which
   should cause an encounter to begin, returning the name of the encounter if it should begin, or nil if no
   encounter begin was detected"
  [{:keys [target-name source-name damage aura-name type skill] :as parsed-line} data]
  (if-let [encounter-name (or (find-defined-encounter-name target-name)
                              (find-defined-encounter-name source-name))]
    (if (and (not (any-successful-encounters? encounter-name data))
             (not (contained-in? aura-name non-combat-starting-auras))
             (not (contained-in? skill non-combat-starting-skills)))
      (let [encounter (get defined-encounters encounter-name)]
        (cond
          (ignored-interaction-event? encounter parsed-line)
          nil

          (ignored-skill-event? encounter parsed-line)
          nil

          ; if either of these are defined, then their criteria MUST pass to
          ; trigger an encounter
          (or (:trigger-on-damage? encounter)
              (:trigger-on-aura? encounter)
              (:trigger-on-buff? encounter)
              (:trigger-on-debuff? encounter))
          (cond
            (and (:trigger-on-damage? encounter) damage)           encounter-name
            (and (:trigger-on-aura? encounter) aura-name)          encounter-name
            (and (:trigger-on-buff? encounter) (= :buff type))     encounter-name
            (and (:trigger-on-debuff? encounter) (= :debuff type)) encounter-name)

          :else
          encounter-name)))))

(defn begin-encounter
  "sets up a new active encounter in the parsed data, returning the new parsed data set ready to use for
   parsing a new encounter."
  [encounter-name {:keys [timestamp line] :as parsed-line} data]
  (info "Beginning encounter" (str "\"" encounter-name "\"") "detected on line:" line)
  (assoc data :active-encounter
              {:name             encounter-name
               :started-at       timestamp
               :entities         {}
               :skills           {}
               :trigger-entities (get-in defined-encounters [encounter-name :entities])}))

(defn detect-encounter-end
  "determines if the currently active encounter should end based on the active encounter parsed data.
   returns :killed if the encounter should end due to a successful kill, :wipe-or-timeout if the
   encounter was found to be over due to a raid wipe or other non-activity timeout, or nil if the
   active encounter is not over yet."
  [{:keys [^Date timestamp] :as parsed-line} data]
  (let [trigger-entites (get-in data [:active-encounter :trigger-entities])]
    (cond
      (every?
        (fn [[entity-name {:keys [count must-kill-count]}]]
          (let [count-dead (count-currently-dead data entity-name)]
            (>= count-dead (or must-kill-count count))))
        trigger-entites)
      :killed

      (every?
        (fn [[entity-name _]]
          ; HACK: what is the right thing to do when the entity we want to check the activity of hasn't even been
          ;       added to the encounter's entity list yet? most likely because the encounter has probably just begun
          ;       and there have been no combat log lines yet for one or more of the trigger entities.
          ;       should we have a minimum encounter length time? something like 15-30 seconds? that also feels hacky...
          (>= (- (.getTime timestamp)
                 (.getTime (or (get-entity-last-activity entity-name data)
                               timestamp)))
              wipe-or-timeout-period))
        trigger-entites)
      :wipe-or-timeout)))

(defn end-encounter
  "ends the current active encounter in the parsed data, moving it from :active-encounter and inserted it into the
   end of the :encounters list. finalizes the encounter by performing various final entity statistic calculations and
   marks the encounter as successful or not. returns the new parsed data set without any active encounter set."
  [{:keys [timestamp line] :as parsed-line} encounter-end-cause data]
  (let [wipe-or-timeout? (= encounter-end-cause :wipe-or-timeout)]
    (info "Ending encounter" (str "\"" (get-in data [:active-encounter :name]) "\"") "detected on line:" line)
    (if wipe-or-timeout?
      (info "Encounter ending due to wipe or trigger entity activity timeout (unsuccessful encounter kill attempt)."))
    (let [data (-> data
                   (update-active-encounter assoc :ended-at timestamp)
                   (update-active-encounter assoc :wipe-or-timeout? wipe-or-timeout?)
                   (calculate-encounter-stats))]
      (-> data
          (assoc-in [:active-encounter] nil)
          (update-in [:encounters] #(conj %1 (:active-encounter data)))))))

;;;
;;; entity manipulation
;;;

(defn touch-entity
  "updates an entity within the current active encounter by resetting it's :last-activity-at timestamp
   or adds a new entity under the given name to the active encounter if it does not already exist. returns
   the new parsed data set with the updated entity information."
  [data entity-name timestamp]
  (if-not (get-in data [:active-encounter :entities entity-name])
    (assoc-in data [:active-encounter :entities entity-name]
              {:name                 entity-name
               :added-at             timestamp
               :last-activity-at     timestamp
               :damage-out-total     0
               :damage-out-totals    {}
               :damage-in-total      0
               :damage-in-totals     {}
               :alive-dps            0
               :encounter-dps        0
               :damage-out           {}
               :damage-out-by-entity {}
               :damage-in            {}
               :damage-in-by-entity  {}
               :casts                {}
               :other-powers         {}
               :auras                {}
               :deaths               []
               :resurrections        []
               :alive-duration       0})
    (assoc-in data [:active-encounter :entities entity-name :last-activity-at] timestamp)))

(defn get-entity-last-activity
  [entity-name data]
  (get-in data [:active-encounter :entities entity-name :last-activity-at]))

(defn get-entity-alive-time
  "returns the number of milliseconds of the encounter that the entity was alive for"
  [{:keys [deaths resurrections] :as entity} {:keys [started-at ended-at] :as encounter}]
  ;(println (:name entity))
  ;(println "started-at" started-at)
  ;(println "ended-at  " ended-at)
  ;(println "deaths    " deaths)
  ;(println "resurrects" resurrections)
  (if (and (= 0 (count deaths))
           (= 0 (count resurrections)))
    (:duration encounter)
    (let [segments (as-> (concat
                           (map (fn [death] {:status :dead :at (:timestamp death)}) deaths)
                           (map (fn [resurrection] {:status :alive :at (:timestamp resurrection)}) resurrections)
                           [{:status :end :at ended-at}]) x
                         (remove empty? x)
                         (sort-by :at x))]
      ;(println ">" segments)
      (reduce
        (fn [{:keys [total current-status from] :as result} {:keys [status at]}]
          ;(println ">>" current-status from status at)
          (cond
            ; is the first state change we find a resurrect? (e.g. they were dead when the fight began)
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

            ; resurrected after a death
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

(defn finalize-entity-auras
  [entity timestamp]
  ; TODO
  entity)

(defn finalize-entities
  [data]
  (update-active-encounter
    data
    (fn [encounter]
      (-> encounter
          (update-all-entities
            #(assoc % :alive-duration (get-entity-alive-time % encounter)))
          (update-all-entities
            #(assoc %
              :encounter-dps (Math/round ^double
                                         (/ (:damage-out-total %)
                                            (/ (:duration encounter)
                                               1000)))
              :alive-dps (Math/round ^double
                                     (/ (:damage-out-total %)
                                        (/ (:alive-duration %)
                                           1000)))))
          (update-all-entities finalize-entity-auras (:ended-at encounter))))))

(defn calculate-encounter-stats
  [data]
  (-> data
      (update-active-encounter
        (fn [{:keys [started-at ended-at] :as encounter}]
          (assoc encounter :duration (time-between started-at ended-at))))
      (finalize-entities)))

(defn count-currently-dead
  [data entity-name]
  (if-let [entity (get-in data [:active-encounter :entities entity-name])]
    (let [num-deaths     (count (:deaths entity))
          num-resurrects (count (:resurrections entity))]
      (- num-deaths num-resurrects))
    0))

(defn update-damage-averages
  [{:keys [num-hits total-hit-damage total-crit-damage num-crits] :as totals}]
  (-> totals
      (update-in [:average-hit] #(if (> num-hits 0) (int (/ total-hit-damage num-hits)) %))
      (update-in [:average-crit] #(if (> num-crits 0) (int (/ total-crit-damage num-crits)) %))))

(defn add-from-damage-properties
  [totals
   {:keys [damage damage-type hit-type crit? partial-absorb partial-resist partial-block avoidance-method] :as damage-properties}]
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

(defn entity-takes-damage
  [data entity-name from-entity-name {:keys [skill damage damage-type] :as damage-properties} timestamp]
  (-> data
      (update-entity-field entity-name [:damage-in-total] #(if damage (+ (or % 0) damage) %))
      (update-entity-field entity-name [:damage-in-totals damage-type] #(if damage (+ (or % 0) damage) %))
      (update-entity-field entity-name [:damage-in skill] #(add-from-damage-properties % damage-properties))
      (update-entity-field entity-name [:damage-in-by-entity from-entity-name skill] #(add-from-damage-properties % damage-properties))))

(defn entity-deals-damage
  [data entity-name to-entity-name {:keys [skill damage damage-type] :as damage-properties} timestamp]
  (-> data
      (update-entity-field entity-name [:damage-out-total] #(if damage (+ (or % 0) damage) %))
      (update-entity-field entity-name [:damage-out-totals damage-type] #(if damage (+ (or % 0) damage) %))
      (update-entity-field entity-name [:damage-out skill] #(add-from-damage-properties % damage-properties))
      (update-entity-field entity-name [:damage-out-by-entity to-entity-name skill] #(add-from-damage-properties % damage-properties))))

;;;
;;; main combat log entry processing entry points
;;;

(defn process-source-to-target-damage
  [source-name target-name damage-properties timestamp data]
  (-> data
      (touch-entity source-name timestamp)
      (touch-entity target-name timestamp)
      (entity-takes-damage target-name source-name damage-properties timestamp)
      (entity-deals-damage source-name target-name damage-properties timestamp))
  )

(defn process-entity-death
  [entity-name timestamp data]
  (-> data
      (touch-entity entity-name timestamp)
      (update-entity-field entity-name [:deaths] #(conj % {:timestamp timestamp}))
      (update-entity entity-name finalize-entity-auras timestamp)))

(defn process-source-to-target-cast
  [source-name target-name skill-name timestamp data]
  data)

(defn process-entity-cast
  [entity-name skill-name timestamp data]
  data)
