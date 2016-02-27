(ns vwowrla.core.parser
  (:import (java.util TimeZone))
  (:require
    [clojure.tools.logging :refer [info error warn]]
    [clojure.java.io :as io]
    [vwowrla.core.encounters :as encounters]
    [vwowrla.core.handlers :refer [handle-event]]
    [vwowrla.core.matchers :refer [regex-matchers]])
  (:use
    vwowrla.core.preparsing
    vwowrla.core.utils))

(defn active-encounter?
  [data]
  (not (nil? (:active-encounter data))))

(defn parse-line
  [^String line {:keys [log-owner-char-name] :as options}]
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
        (process-parsed-line parsed-line log-owner-char-name))
      line-metadata)))

(defn handle-line
  [parsed-line data]
  (handle-event parsed-line data))

(defn- active-encounter-processing
  [parsed-line data]
  (let [data (handle-line parsed-line data)]
    (if-let [encounter-end (encounters/detect-encounter-end parsed-line data)]
      (encounters/end-encounter parsed-line encounter-end data)
      data)))

(defn- out-of-encounter-processing
  [parsed-line data]
  (if-let [encounter-name (encounters/detect-encounter-triggered parsed-line data)]
    (->> data
         (encounters/begin-encounter encounter-name parsed-line)
         (handle-line parsed-line))
    data))

(defn- parse-log*
  [f options]
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

(defn parse-log
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
