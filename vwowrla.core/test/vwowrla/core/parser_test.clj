(ns vwowrla.core.parser-test
  (:import
    (java.util TimeZone))
  (:use
    clojure.test
    vwowrla.core.parser))

(deftest barebones-line-parse-results
  (let [parsed (parse-line "1/2 3:45:00.000  Test combat log line  with 2 spaces in content part."
                           {:log-owner-char-name "Blasticus"
                            :year                2015
                            :timezone            (TimeZone/getDefault)
                            :windows?            false})]
    (is (and (map? parsed)
             (contains? parsed :timestamp)
             (contains? parsed :line)))))
