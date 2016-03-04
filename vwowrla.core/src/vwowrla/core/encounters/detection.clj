(ns vwowrla.core.encounters.detection
  (:import
    (java.util Date))
  (:require
    [clojure.tools.logging :refer [info warn error]]
    [schema.core :as s]
    [vwowrla.core.encounters.analysis :refer [count-currently-dead get-entity-last-activity]])
  (:use
    vwowrla.core.encounters.core
    vwowrla.core.schemas
    vwowrla.core.utils))

(defn- ignore-interaction?
  [entity-name ignore-entity-list {:keys [target-name source-name] :as event}]
  (and (or (= entity-name target-name)
           (= entity-name source-name))
       (or (contained-in? target-name ignore-entity-list)
           (contained-in? source-name ignore-entity-list))))

(s/defn ignored-interaction-event? :- s/Bool
  "returns true if the given parsed combat log line is between entities that have
   been specified to ignore interactions between for the purposes of detecting
   an encounter trigger"
  [encounter :- DefinedEncounter
   event     :- CombatEvent]
  (->> (:entities encounter)
       (filter
         (fn [[entity-name entity-props]]
           (seq (:ignore-interactions-with entity-props))))
       (filter
         (fn [[entity-name entity-props]]
           (ignore-interaction? entity-name (:ignore-interactions-with entity-props) event)))
       (seq)
       (boolean)))

(defn- ignore-skill?
  [entity-name ignore-skill-list {:keys [source-name skill] :as event}]
  (and (= entity-name source-name)
       (contained-in? skill ignore-skill-list)))

(s/defn ignored-skill-event? :- s/Bool
  "returns true if the given parsed combat log line is for an encounter entity
   that is using a skill that has been specifically indicated should be ignored
   for the purposes of triggering an encounter"
  [encounter :- DefinedEncounter
   event     :- CombatEvent]
  (->> (:entities encounter)
       (filter
         (fn [[entity-name entity-props]]
           (seq (:ignore-skills entity-props))))
       (filter
         (fn [[entity-name entity-props]]
           (ignore-skill? entity-name (:ignore-skills entity-props) event)))
       (seq)
       (boolean)))

(s/defn detect-encounter-triggered :- (s/maybe s/Str)
  "determines if the given combat event should trigger the beginning of an encounter or not.
   returns the name of the encounter that should begin or nil if no encounter trigger was
   detected"
  [{:keys [target-name source-name damage aura-name type skill] :as event} :- CombatEvent
   data :- RaidAnalysis]
  (if-let [encounter-name (or (find-defined-encounter-name target-name)
                              (find-defined-encounter-name source-name))]
    (if (and (not (any-successful-encounters? encounter-name data))
             (not (contained-in? aura-name non-combat-starting-auras))
             (not (contained-in? skill non-combat-starting-skills)))
      (let [encounter (get defined-encounters encounter-name)]
        (cond
          (ignored-interaction-event? encounter event)
          nil

          (ignored-skill-event? encounter event)
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

(s/defn detect-encounter-end :- (s/maybe s/Keyword)
  "determines if the encounter should end based on the given combat event and the current state
   of the encounter.
   returns :killed for a successful encounter end (all encounter entities killed).
   returns :wipe-or-timeout if there was a wipe or other encounter entity activity timeout.
   returns nil if the encounter is not over yet."
  [{:keys [^Date timestamp]} :- CombatEvent
   encounter                 :- Encounter]
  (let [trigger-entites (:trigger-entities encounter)]
    (cond
      (every?
        (fn [[entity-name {:keys [count must-kill-count]}]]
          (let [count-dead (count-currently-dead encounter entity-name)]
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
                 (.getTime (or (get-entity-last-activity entity-name encounter)
                               timestamp)))
              wipe-or-timeout-period))
        trigger-entites)
      :wipe-or-timeout)))
