(ns vwowrla.core.preparsing
  (:import
    (java.util Calendar GregorianCalendar TimeZone))
  (:require
    [clojure.string :as string]
    [clojure.java.io :as io])
  (:use
    vwowrla.core.utils))

(def problem-entity-names (get-text-resource-as-lines "problem_entity_names.txt"))

(def problem-entity-name-to-fixed-name
  (reduce
    (fn [m problem-name]
      (let [fixed-name (.replace problem-name "'s" "s")]
        (-> m
            (assoc-in [:problem-to-fixed problem-name] fixed-name)
            (assoc-in [:fixed-to-problem fixed-name] problem-name))))
    {:problem-to-fixed {}
     :fixed-to-problem {}}
    problem-entity-names))

(defn sanitize-entity-name
  [^String entity-name]
  (get-in problem-entity-name-to-fixed-name [:problem-to-fixed entity-name] entity-name))

(defn get-original-entity-name
  [^String potentially-sanitized-entity-name]
  (get-in problem-entity-name-to-fixed-name [:fixed-to-problem potentially-sanitized-entity-name] potentially-sanitized-entity-name))

(defn sanitize-entity-names
  [^String line]
  (reduce
    (fn [^String line [^String problem-name ^String fixed-name]]
      (.replace line problem-name fixed-name))
    line
    (:problem-to-fixed problem-entity-name-to-fixed-name)))

(defn undo-swstats-fixlogstring
  [^String line]
  (.replace line " 's" "'s"))

(defn parse-log-timestamp
  [^String timestamp {:keys [^long year ^TimeZone timezone windows?] :as options}]
  (if-let [matches (re-matches #"^(\d{1,2})\/(\d{1,2}) (\d{1,2}):(\d{2}):(\d{2})\.(\d{3})$" timestamp)]
    (let [c (GregorianCalendar.)
          [month day hour minute second millis] (rest matches)]
      (.clear c)
      (.setTimeZone c timezone)
      (.set c year (if windows? (dec (->int month)) (->int month)) (->int day) (->int hour) (->int minute) (->int second))
      (.set c Calendar/MILLISECOND (->int millis))
      (.getTime c))))

(defn split-log-timestamp-and-content
  [^String line]
  (clojure.string/split line #"  " 2))

(defn process-parsed-line
  [{:keys [source-name target-name source] :as parsed-line} ^String log-owner-char-name]
  (merge
    parsed-line
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