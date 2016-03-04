(ns vwowrla.core.parser
  (:import
    (java.util TimeZone))
  (:require
    [clojure.tools.logging :refer [info error warn]]
    [clojure.java.io :as io]
    [schema.core :as s]
    [vwowrla.core.encounters.core :refer [update-active-encounter]]
    [vwowrla.core.encounters.detection :refer [detect-encounter-end detect-encounter-triggered]]
    [vwowrla.core.encounters.analysis :refer [begin-encounter end-encounter active-encounter?]]
    [vwowrla.core.events.handlers :refer [handle-event]]
    [vwowrla.core.events.matchers :refer [find-matcher get-line-regex-matches]])
  (:use
    vwowrla.core.schemas
    vwowrla.core.preparsing
    vwowrla.core.utils))

(defn- ->ignored-event
  [event]
  (assoc event
    :id :ignored
    :logfmt :ignored))

(defn ->unrecognized-event
  [event]
  (assoc event
    :id :unknown
    :logfmt :unknown
    :event :unknown))

(s/defn parse-line :- CombatEvent
  [line    :- s/Str
   options :- ParserOptions]
  "parses a single complete line from a combat log and returns a corresponding
   combat event map with all the descrete pieces of data picked out from the
   line. returns :id = :ignored for ignored combat log events, and
   :id = :unknown for unrecognized events"
  (let [[timestamp stripped-line] (split-log-timestamp-and-content line)
        sanitized-line            (-> stripped-line
                                      (undo-swstats-fixlogstring)
                                      (sanitize-entity-names))
        event-metadata            {:timestamp (parse-log-timestamp timestamp options)
                                   :line      line}]
    (if-let [matcher (find-matcher sanitized-line)]
      (let [matches (get-line-regex-matches sanitized-line matcher)
            args-fn (or (:args matcher) (fn [& _]))
            event   (merge
                      event-metadata
                      (select-keys matcher [:logfmt :event :id])
                      (apply args-fn matches))]
        (if (= :ignored (:event event))
          (->ignored-event event)
          (process-event event (:log-owner-char-name options))))
      (->unrecognized-event event-metadata))))

(s/defn ^:private active-encounter-processing
  [event :- CombatEvent
   data  :- RaidAnalysis]
  "processes a combat event within the context of an active encounter. first
   performs encounter analysis for the event, then attempts to detect if the
   current active encounter has ended as a result of this event, and if so
   ends the encounter.
   should only be called if an encounter is currently active in the current
   raid analysis data."
  (let [data (update-active-encounter data #(handle-event event %))]
    (if-let [encounter-end (detect-encounter-end event (:active-encounter data))]
      (end-encounter event encounter-end data)
      data)))

(s/defn ^:private out-of-encounter-processing
  [event :- CombatEvent
   data  :- RaidAnalysis]
  "processes a combat event outside the context of an active encounter. all
   this really does is try to determine if the event triggers the start of a
   new encounter, and if so begins the encounter + performs encounter analysis
   for the event.
   should only be called if an encounter is NOT currently active in the
   current raid analysis data."
  (if-let [encounter-name (detect-encounter-triggered event data)]
    (as-> data x
         (begin-encounter encounter-name event x)
         (update-active-encounter x #(handle-event event %)))
    data))

(s/defn ^:private parse-log* :- (s/maybe RaidAnalysis)
  [f
   options :- ParserOptions]
  (with-open [rdr (io/reader f)]
    (try
      (reduce
        (fn [data ^String line]
          (try
            (let [event (parse-line line options)]
              (if (active-encounter? data)
                (active-encounter-processing event data)
                (out-of-encounter-processing event data)))
            (catch Exception ex
              (throw (ex-info "Parser error" {:line line} ex)))))
        {:encounters       []
         :active-encounter nil}
        (line-seq rdr))
      (catch Exception ex
        (flush)
        (error ex "Parser error.")))))

(s/defn parse-log :- (s/maybe RaidAnalysis)
  [f options]
  "parses and performs encounter/raid analysis on the given combat log. returns
   a raid analysis data set if successful."
  (let [line-ending-type (detect-file-line-ending-type f)]
    (if-not line-ending-type
      (warn "Could not detect line-ending type in log file. Assuming" :windows)
      (info "Detected" line-ending-type "line-ending type in log file."))
    (parse-log* f (merge
                    {:windows? (= :windows (or line-ending-type :windows))
                     :timezone (TimeZone/getDefault)
                     :year     (current-year)}
                    options))))
