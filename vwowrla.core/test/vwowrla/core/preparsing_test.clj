(ns vwowrla.core.preparsing-test
  (:import (java.util TimeZone))
  (:use
    clojure.test
    vwowrla.core.preparsing))

(deftest entity-name-sanitizing
  (is (= (sanitize-entity-name "Twilight's Hammer Ambassador")
         "Twilights Hammer Ambassador"))
  (is (= (sanitize-entity-name "Ragnaros")
         "Ragnaros"))
  (is (= (sanitize-entity-name "C'Thun")
         "C'Thun")))

(deftest entity-name-unsanitizing
  (is (= (get-original-entity-name "Twilights Hammer Ambassador")
         "Twilight's Hammer Ambassador"))
  (is (= (get-original-entity-name "Ragnaros")
         "Ragnaros"))
  (is (= (get-original-entity-name "C'Thun")
         "C'Thun")))

(deftest combat-log-line-entity-name-sanitizing
  (is (= (sanitize-entity-names "Twilight's Hammer Ambassador's Flame Shock hits you for 1234 Fire damage.")
         "Twilights Hammer Ambassador's Flame Shock hits you for 1234 Fire damage."))
  (is (= (sanitize-entity-names "Magnomage's Shazzrah's Curse is removed.")
         "Magnomage's Shazzrah's Curse is removed.")))

(deftest combat-log-line-undo-swstats-fixlogstring
  (is (= (undo-swstats-fixlogstring "Twilight's Hammer Ambassador 's Flame Shock hits you for 1234 Fire damage.")
         "Twilight's Hammer Ambassador's Flame Shock hits you for 1234 Fire damage."))
  (is (= (undo-swstats-fixlogstring "Magnomage 's Shazzrah's Curse is removed.")
         "Magnomage's Shazzrah's Curse is removed.")))

(deftest date-parsing
  (let [options {:year     2015
                 :timezone (TimeZone/getTimeZone "America/Toronto")
                 :windows? false}]
    (is (= (parse-log-timestamp "6/9 21:36:18.227" options)
           #inst "2015-07-10T01:36:18.227-00:00"))
    (is (= (parse-log-timestamp "11/31 13:37:42.123" options)
           #inst "2015-12-31T18:37:42.123-00:00"))
    (is (= (parse-log-timestamp "0/1 00:00:00.000" options)
           #inst "2015-01-01T05:00:00.000-00:00")))
  (let [options {:year     2015
                 :timezone (TimeZone/getTimeZone "America/Toronto")
                 :windows? true}]
    (is (= (parse-log-timestamp "7/24 10:25:50.444" options)
           #inst "2015-07-24T14:25:50.444-00:00"))))

(deftest raw-log-line-splitting
  (is (= (split-log-timestamp-and-content "6/9 22:50:49.199  Ragnaros dies.")
         ["6/9 22:50:49.199" "Ragnaros dies."]))
  (is (= (split-log-timestamp-and-content "1/2 3:45:00.000  Test combat log line  with 2 spaces in content part.")
         ["1/2 3:45:00.000" "Test combat log line  with 2 spaces in content part."])))
