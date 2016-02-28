(ns vwowrla.core.encounters.core
  (:require
    [schema.core :as s])
  (:use
    vwowrla.core.schemas
    vwowrla.core.utils))

(def wipe-or-timeout-period (* 60 1000))

(def defined-encounters (get-edn-resource "encounters.edn"))
(def non-combat-starting-auras (get-text-resource-as-lines "non_combat_starting_auras.txt"))
(def non-combat-starting-skills (get-text-resource-as-lines "non_combat_starting_skills.txt"))

(s/defn find-defined-encounter-name :- (s/maybe s/Str)
  "returns the name of a defined encounter which includes the given entity in it's
   list of trigger entities. returns nil if there is no encounter which includes the
   given entity"
  [entity-name :- (s/maybe s/Str)]
  (->> defined-encounters
       (filter (fn [[_ {:keys [entities]}]]
                 (->> entities
                      (filter #(= (first %) entity-name))
                      (first))))
       (ffirst)))

(s/defn find-past-encounters :- [Encounter]
  "return a list of all previously parsed encounters (successful and not) matching the encounter name"
  [encounter-name :- s/Str
   data           :- RaidAnalysis]
  (->> (:encounters data)
       (filter #(= (:name %) encounter-name))))

(s/defn any-successful-encounters? :- s/Bool
  "returns true if there are any successful parsed encounters matching the encounter name"
  [encounter-name :- s/Str
   data           :- RaidAnalysis]
  (->> (find-past-encounters encounter-name data)
       (map :wipe-or-timeout?)
       (filter false?)
       (empty?)
       (not)))

(s/defn update-active-encounter :- RaidAnalysis
  "updates the active encounter using function f which will take the current active
   encounter and any supplied args, returning a new active encounter which is
   'updated' in the original full parsed data and then finally returned."
  [data :- RaidAnalysis
   f & args]
  (apply update-in data [:active-encounter] f args))

(s/defn update-all-entities :- Encounter
  "updates all entities in the encounter using function f which takes the current
   entity and any supplied args, returning a new entity which is 'updated' in the
   original encounter. returns the encounter with the modified entity data."
  [encounter :- Encounter
   f & args]
  (reduce
    (fn [encounter [entity-name entity]]
      (assoc-in encounter [:entities entity-name] (apply f entity args)))
    encounter
    (:entities encounter)))

(s/defn update-all-active-encounter-entities :- RaidAnalysis
  "updates all entities in the current active encounter in the full parsed data
   using function f which takes the current entity and any supplied args, returning
   a new entity which is 'updated' in the original encounter. returns the updated
   full parsed data."
  [data :- RaidAnalysis
   f & args]
  (update-active-encounter data #(update-all-entities % f args)))

(s/defn update-entity :- RaidAnalysis
  "updates an entity in the full parsed data's active encounter using function f
   which takes the current entity and any supplied args, returning the new entity
   which is 'updated' in the active encounter. returns the updated full parsed data."
  [data        :- RaidAnalysis
   entity-name :- s/Str
   f & args]
  (apply update-in data [:active-encounter :entities entity-name] f args))

(s/defn update-entity-field :- RaidAnalysis
  "updates a specific field within an entity pointed to by ks in the full parsed
   data's active encounter using function f which takes the current entity and any
   supplied args, returning the new entity which is 'updated' in the active encounter.
   returns the updated full parsed data."
  [data        :- RaidAnalysis
   entity-name :- s/Str
   ks f & args]
  (apply update-in data (concat [:active-encounter :entities entity-name] ks) f args))
