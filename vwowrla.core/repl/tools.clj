(ns tools
  (:import
    (java.util TimeZone))
  (:require
    [clojure.java.io :as io])
  (:use
    vwowrla.core.parser))

(def opts {:log-owner-char-name "Blasticus"
           :year                2016
           :timezone            (TimeZone/getDefault)
           :windows?            false})

(defn collect-unique-entity-names
  [f options]
  (with-open [rdr (io/reader f)]
    (->> (line-seq rdr)
         (reduce
           (fn [entity-names ^String line]
             (let [event (parse-line line options)]
               (-> entity-names
                   (conj (:source-name event))
                   (conj (:target-name event)))))
           #{})
         (remove nil?))))
