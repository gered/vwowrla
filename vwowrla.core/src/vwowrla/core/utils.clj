(ns vwowrla.core.utils
  (:import
    (java.io Reader)
    (java.util Date))
  (:require
    [clojure.string :as string]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [cheshire.core :as json]))


(defn ->kw
  [^String s]
  (keyword (string/lower-case s)))

(defn ->int
  [^String s]
  (if s
    (Integer/parseInt s)))

(defn one-of?
  [x & more]
  (boolean
    (some #{x} more)))

(defn contained-in?
  [x coll]
  (boolean
    (some #{x} coll)))

(defn detect-file-line-ending-type
  [f]
  (if-let [^Reader reader (io/reader f)]
    (loop [ch (.read reader)]
      (cond
        (= -1 ch)
        nil

        (= 10 ch)
        :linux

        (= 13 ch)
        (let [ch (.read reader)]
          (if (= 10 ch)
            :windows
            :unix))

        :default
        (recur (.read reader))))))

(defn current-year
  []
  (+ 1900 (.getYear (Date.))))

(defn time-between
  [^Date a ^Date b]
  (- (.getTime b)
     (.getTime a)))

(defn get-text-resource-as-lines
  [f]
  (with-open [rdr (io/reader (io/resource f))]
    (doall (line-seq rdr))))

(defn get-json-resource
  [f]
  (-> (io/resource f)
      (io/reader)
      (json/parse-stream true)))

(defn get-edn-resource
  [f]
  (-> (io/resource f)
      (slurp)
      (edn/read-string)))