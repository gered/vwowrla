(ns vwowrla.core.encounters.core
  (:require
    [schema.core :as s])
  (:use
    vwowrla.core.schemas
    vwowrla.core.utils))

(def wipe-or-timeout-period (* 60 1000))

(def defined-encounters (get-edn-resource "encounters.edn"))
(def enemy-entity-names (get-text-resource-as-lines "enemy_entity_names.txt"))
(def non-combat-starting-auras (get-text-resource-as-lines "non_combat_starting_auras.txt"))
(def non-combat-starting-skills (get-text-resource-as-lines "non_combat_starting_skills.txt"))

(s/defn find-defined-encounter-name :- (s/maybe s/Str)
  "returns the name of a defined encounter which includes the given entity in it's
   list of encounter entities. returns nil if there is no encounter which includes the
   given entity"
  [entity-name :- (s/maybe s/Str)]
  (->> defined-encounters
       (filter (fn [[_ {:keys [entities]}]]
                 (->> entities
                      (filter
                        (fn [[name {:keys [cannot-trigger?]}]]
                          (and (= entity-name name)
                               (not cannot-trigger?))))
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
   encounter and any supplied args. f should return a new encounter.
   returns the log analysis data with the modified active encounter."
  [data :- RaidAnalysis
   f & args]
  (apply update-in data [:active-encounter] f args))

(s/defn update-all-entities :- Encounter
  "updates all entities in the encounter using function f which takes the current
   entity and any supplied args. f should return a new/updated entity.
   returns the encounter with the modified entity data."
  [encounter :- Encounter
   f & args]
  (reduce
    (fn [encounter [entity-name entity]]
      (assoc-in encounter [:entities entity-name] (apply f entity args)))
    encounter
    (:entities encounter)))

(s/defn update-entity :- Encounter
  "updates an entity (specified by name) in the encounter using function f which
   takes the current entity (or nil if no such entity exists in the encounter) and
   any supplied args. f should return a new/updated entity.
   returns the encounter with the updated entity."
  [encounter   :- Encounter
   entity-name :- s/Str
   f & args]
  (apply update-in encounter [:entities entity-name] f args))

(s/defn update-entity-field :- Encounter
  "updates a specific field (pointed to by ks) within an entity (specified by name)
   in the encounter using function f which takes the value of the entity field
   specified and any supplied args. f should return the new value for that field.
   returns the encounter with the updated entity."
  [encounter   :- Encounter
   entity-name :- s/Str
   ks f & args]
  (let [ks (concat [:entities entity-name] ks)]
    (apply update-in encounter ks f args)))
