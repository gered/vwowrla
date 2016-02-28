(ns vwowrla.core.preparsing
  (:import
    (java.util Calendar Date GregorianCalendar))
  (:require
    [clojure.string :as string]
    [clojure.java.io :as io]
    [schema.core :as s])
  (:use
    vwowrla.core.schemas
    vwowrla.core.utils))

(def problem-entity-names (get-text-resource-as-lines "problem_entity_names.txt"))

(def problem-entity-name-to-fixed-name
  (reduce
    (fn [m problem-name]
      (let [fixed-name (.replace ^String problem-name "'s" "s")]
        (-> m
            (assoc-in [:problem-to-fixed problem-name] fixed-name)
            (assoc-in [:fixed-to-problem fixed-name] problem-name))))
    {:problem-to-fixed {}
     :fixed-to-problem {}}
    problem-entity-names))

(s/defn sanitize-entity-name :- s/Str
  [entity-name :- s/Str]
  (get-in problem-entity-name-to-fixed-name [:problem-to-fixed entity-name] entity-name))

(s/defn get-original-entity-name :- s/Str
  [potentially-sanitized-entity-name :- s/Str]
  (get-in problem-entity-name-to-fixed-name [:fixed-to-problem potentially-sanitized-entity-name] potentially-sanitized-entity-name))

(s/defn sanitize-entity-names :- s/Str
  [line :- s/Str]
  (reduce
    (fn [^String line [^String problem-name ^String fixed-name]]
      (.replace line problem-name fixed-name))
    line
    (:problem-to-fixed problem-entity-name-to-fixed-name)))

(s/defn undo-swstats-fixlogstring :- s/Str
  [line :- s/Str]
  (.replace ^String line " 's" "'s"))

(s/defn parse-log-timestamp :- (s/maybe Date)
  [timestamp :- s/Str
   options   :- ParserOptions]
  (if-let [matches (re-matches #"^(\d{1,2})\/(\d{1,2}) (\d{1,2}):(\d{2}):(\d{2})\.(\d{3})$" timestamp)]
    (let [c (GregorianCalendar.)
          [month day hour minute second millis] (rest matches)]
      (.clear c)
      (.setTimeZone c (:timezone options))
      (.set c (:year options) (if (:windows? options) (dec (->int month)) (->int month)) (->int day) (->int hour) (->int minute) (->int second))
      (.set c Calendar/MILLISECOND (->int millis))
      (.getTime c))))

(s/defn split-log-timestamp-and-content :- [s/Str]
  [line :- s/Str]
  (string/split line #"  " 2))

(s/defn process-event :- CombatEvent
  [{:keys [source-name target-name source] :as event} :- CombatEvent
   log-owner-char-name :- s/Str]
  (merge
    event
    (if source-name
      {:source-name (if (= "you" (string/lower-case source-name))
                      log-owner-char-name
                      (get-original-entity-name source-name))})
    (if target-name
      {:target-name (if (= "you" (string/lower-case target-name))
                      log-owner-char-name
                      (get-original-entity-name target-name))})
    (if source
      {:source (if (= "you" (string/lower-case source))
                 log-owner-char-name
                 (get-original-entity-name source))})))