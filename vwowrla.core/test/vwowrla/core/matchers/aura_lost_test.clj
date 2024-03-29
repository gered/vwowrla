(ns vwowrla.core.matchers.aura-lost-test
  (:import
    (java.util TimeZone))
  (:use
    clojure.test
    vwowrla.core.matchers.matchers-test-utils)
  (:require
    [vwowrla.core.parser :refer [parse-line]]
    [vwowrla.core.preparsing :refer [parse-log-timestamp]]
    [vwowrla.core.events.matchers :refer [regex-matchers]]))

(def options {:log-owner-char-name "Blasticus"
              :year                2015
              :timezone            (TimeZone/getDefault)
              :windows?            false})

(def owner-char-name (:log-owner-char-name options))
(def year (:year options))
(def timezone (:timezone options))

(deftest aura-fades
  (is (valid-matcher? (get-matcher regex-matchers :aura-fades)))

  (is (= (parse-line "5/25 21:16:39.701  Defensive Stance fades from Eggs." options)
         {:id          :aura-fades
          :logfmt      :aura-fades
          :event       :aura-lost
          :line        "5/25 21:16:39.701  Defensive Stance fades from Eggs."
          :timestamp   (parse-log-timestamp "5/25 21:16:39.701" options)
          :target-name "Eggs"
          :aura-name   "Defensive Stance"
          :faded?      true}))

  (is (= (parse-line "5/25 21:17:53.550  Fire Shield fades from you." options)
         {:id          :aura-fades
          :logfmt      :aura-fades
          :event       :aura-lost
          :line        "5/25 21:17:53.550  Fire Shield fades from you."
          :timestamp   (parse-log-timestamp "5/25 21:17:53.550" options)
          :target-name owner-char-name
          :aura-name   "Fire Shield"
          :faded?      true})))

(deftest aura-removed-self
  (is (valid-matcher? (get-matcher regex-matchers :aura-removed-self)))

  (is (= (parse-line "5/25 23:09:26.614  Your Winter's Chill is removed." options)
         {:id          :aura-removed-self
          :logfmt      :aura-removed
          :event       :aura-lost
          :line        "5/25 23:09:26.614  Your Winter's Chill is removed."
          :timestamp   (parse-log-timestamp "5/25 23:09:26.614" options)
          :target-name owner-char-name
          :aura-name   "Winter's Chill"
          :faded?      false})))

(deftest aura-removed
  (is (valid-matcher? (get-matcher regex-matchers :aura-removed)))

  (is (= (parse-line "5/25 22:41:56.108  Magnomage's Shazzrah's Curse is removed." options)
         {:id          :aura-removed
          :logfmt      :aura-removed
          :event       :aura-lost
          :line        "5/25 22:41:56.108  Magnomage's Shazzrah's Curse is removed."
          :timestamp   (parse-log-timestamp "5/25 22:41:56.108" options)
          :target-name "Magnomage"
          :aura-name   "Shazzrah's Curse"
          :faded?      false}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Twilight's Hammer Ambassador's Winter's Chill is removed." options)
         {:id          :aura-removed
          :logfmt      :aura-removed
          :event       :aura-lost
          :line        "1/2 3:45:00.123  Twilight's Hammer Ambassador's Winter's Chill is removed."
          :timestamp   (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name "Twilight's Hammer Ambassador"
          :aura-name   "Winter's Chill"
          :faded?      false}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Twilight's Hammer Ambassador's Mana Shield is removed." options)
         {:id          :aura-removed
          :logfmt      :aura-removed
          :event       :aura-lost
          :line        "1/2 3:45:00.123  Twilight's Hammer Ambassador's Mana Shield is removed."
          :timestamp   (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name "Twilight's Hammer Ambassador"
          :aura-name   "Mana Shield"
          :faded?      false})))
