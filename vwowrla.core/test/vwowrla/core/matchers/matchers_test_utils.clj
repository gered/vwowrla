(ns vwowrla.core.matchers.matchers-test-utils)

(defn get-matcher [matchers id]
  (->> matchers
       (filter #(= (:id %) id))
       (first)))

(defn valid-matcher? [matcher]
  (and (map? matcher)
       (:regex matcher)
       (:logfmt matcher)
       (:event matcher)
       (:args matcher)))
