(ns vwowrla.core.parser
  (:import
    (java.util TimeZone))
  (:require
    [clojure.tools.logging :refer [info error warn]]
    [clojure.java.io :as io]
    [schema.core :as s]
    [vwowrla.core.encounters.detection :refer [detect-encounter-end detect-encounter-triggered]]
    [vwowrla.core.encounters.analysis :refer [begin-encounter end-encounter]]
    [vwowrla.core.handlers :refer [handle-event]]
    [vwowrla.core.matchers :refer [regex-matchers]])
  (:use
    vwowrla.core.schemas
    vwowrla.core.preparsing
    vwowrla.core.utils))

(s/defn active-encounter? :- s/Bool
  [data :- RaidAnalysis]
  (not (nil? (:active-encounter data))))

(defn- ->ignored-event
  [parsed-line]
  (assoc parsed-line
    :id :ignored
    :logfmt :ignored))

(defn ->unrecognized-event
  [parsed-line]
  (assoc parsed-line
    :id :unknown
    :logfmt :unknown
    :event :unknown))

(s/defn parse-line :- CombatEvent
  [^String line
   options :- ParserOptions]
  (let [[timestamp stripped-line] (split-log-timestamp-and-content line)
        sanitized-line            (-> stripped-line
                                      (undo-swstats-fixlogstring)
                                      (sanitize-entity-names))
        line-metadata             {:timestamp (parse-log-timestamp timestamp options)
                                   :line      line}]
    (if-let [matcher (->> regex-matchers
                          (filter #(re-matches (:regex %) sanitized-line))
                          (first))]
      (let [regex-matches (rest (re-matches (:regex matcher) sanitized-line))
            args-fn       (or (:args matcher) (fn [& _]))
            parsed-line   (merge
                            line-metadata
                            (select-keys matcher [:logfmt :event :id])
                            (apply args-fn regex-matches))]
        (if (= :ignored (:event parsed-line))
          (->ignored-event parsed-line)
          (process-parsed-line parsed-line (:log-owner-char-name options))))
      (->unrecognized-event line-metadata))))

(s/defn handle-line
  [parsed-line :- CombatEvent
   data        :- RaidAnalysis]
  (handle-event parsed-line data))

(s/defn ^:private active-encounter-processing
  [parsed-line :- CombatEvent
   data        :- RaidAnalysis]
  (let [data (handle-line parsed-line data)]
    (if-let [encounter-end (detect-encounter-end parsed-line data)]
      (end-encounter parsed-line encounter-end data)
      data)))

(s/defn ^:private out-of-encounter-processing
  [parsed-line :- CombatEvent
   data        :- RaidAnalysis]
  (if-let [encounter-name (detect-encounter-triggered parsed-line data)]
    (->> data
         (begin-encounter encounter-name parsed-line)
         (handle-line parsed-line))
    data))

(s/defn ^:private parse-log* :- (s/maybe RaidAnalysis)
  [f
   options :- ParserOptions]
  (with-open [rdr (io/reader f)]
    (try
      (reduce
        (fn [data ^String line]
          (try
            (let [parsed (parse-line line options)]
              (if (active-encounter? data)
                (active-encounter-processing parsed data)
                (out-of-encounter-processing parsed data)))
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
