(ns vwowrla.core.schema-utils
  (:require
    [schema.core :as s]))

(def array
  (s/pred
    #(if % (.isArray (class %)))))

(defn one-of [allowed-values]
  (s/pred
    #(boolean (some #{%} allowed-values))))
