(ns vwowrla.core.parser
  (:import
    (java.util TimeZone))
  (:require
    [clojure.tools.logging :refer [info error warn]]
    [clojure.java.io :as io]
    [schema.core :as s]
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

(s/defn handle-line
  [event :- CombatEvent
   data  :- RaidAnalysis]
  (handle-event event data))

(s/defn ^:private active-encounter-processing
  [event :- CombatEvent
   data  :- RaidAnalysis]
  (let [data (handle-line event data)]
    (if-let [encounter-end (detect-encounter-end event data)]
      (end-encounter event encounter-end data)
      data)))

(s/defn ^:private out-of-encounter-processing
  [event :- CombatEvent
   data  :- RaidAnalysis]
  (if-let [encounter-name (detect-encounter-triggered event data)]
    (->> data
         (begin-encounter encounter-name event)
         (handle-line event))
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
  (let [line-ending-type (detect-file-line-ending-type f)]
    (if-not line-ending-type
      (warn "Could not detect line-ending type in log file. Assuming" :windows)
      (info "Detected" line-ending-type "line-ending type in log file."))
    (parse-log* f (merge
                    {:windows? (= :windows (or line-ending-type :windows))
                     :timezone (TimeZone/getDefault)
                     :year     (current-year)}
                    options))))
