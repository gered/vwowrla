(ns vwowrla.core.matchers.resource-gained-test
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

(deftest resource-gained-from-skill
  (is (valid-matcher? (get-matcher regex-matchers :resource-gained-from-skill)))

  (is (= (parse-line "5/25 21:21:32.915  Pwnstar gains 8 Mana from Pwnstar's Mana Regeneration." options)
         {:id            :resource-gained-from-skill
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "5/25 21:21:32.915  Pwnstar gains 8 Mana from Pwnstar's Mana Regeneration."
          :timestamp     (parse-log-timestamp "5/25 21:21:32.915" options)
          :target-name   "Pwnstar"
          :source-name   "Pwnstar"
          :skill         "Mana Regeneration"
          :amount        8
          :resource-type :mana}))

  (is (= (parse-line "5/25 21:46:01.660  Vasling gains 375 Mana from Vasling's Replenish Mana." options)
         {:id            :resource-gained-from-skill
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "5/25 21:46:01.660  Vasling gains 375 Mana from Vasling's Replenish Mana."
          :timestamp     (parse-log-timestamp "5/25 21:46:01.660" options)
          :target-name   "Vasling"
          :source-name   "Vasling"
          :skill         "Replenish Mana"
          :amount        375
          :resource-type :mana}))

  (is (= (parse-line "5/25 21:16:53.591  Architrex gains 25 Energy from Architrex's Relentless Strikes Effect." options)
         {:id            :resource-gained-from-skill
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "5/25 21:16:53.591  Architrex gains 25 Energy from Architrex's Relentless Strikes Effect."
          :timestamp     (parse-log-timestamp "5/25 21:16:53.591" options)
          :target-name   "Architrex"
          :source-name   "Architrex"
          :skill         "Relentless Strikes Effect"
          :amount        25
          :resource-type :energy}))

  (is (= (parse-line "5/25 21:16:46.844  Boompow gains 1 Rage from Boompow's Bloodrage." options)
         {:id            :resource-gained-from-skill
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "5/25 21:16:46.844  Boompow gains 1 Rage from Boompow's Bloodrage."
          :timestamp     (parse-log-timestamp "5/25 21:16:46.844" options)
          :target-name   "Boompow"
          :source-name   "Boompow"
          :skill         "Bloodrage"
          :amount        1
          :resource-type :rage}))

  (is (= (parse-line "5/25 21:13:17.531  Futilian gains 330 health from Leaf's Rejuvenation." options)
         {:id            :resource-gained-from-skill
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "5/25 21:13:17.531  Futilian gains 330 health from Leaf's Rejuvenation."
          :timestamp     (parse-log-timestamp "5/25 21:13:17.531" options)
          :target-name   "Futilian"
          :source-name   "Leaf"
          :skill         "Rejuvenation"
          :amount        330
          :resource-type :health}))

  (is (= (parse-line "5/25 21:21:16.303  Shivaara gains 35 Happiness from Pwnstar's Feed Pet Effect." options)
         {:id            :resource-gained-from-skill
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "5/25 21:21:16.303  Shivaara gains 35 Happiness from Pwnstar's Feed Pet Effect."
          :timestamp     (parse-log-timestamp "5/25 21:21:16.303" options)
          :target-name   "Shivaara"
          :source-name   "Pwnstar"
          :skill         "Feed Pet Effect"
          :amount        35
          :resource-type :happiness}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Somebody gains 100 health from Somebody's Druid's Potion." options)
         {:id            :resource-gained-from-skill
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "1/2 3:45:00.123  Somebody gains 100 health from Somebody's Druid's Potion."
          :timestamp     (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name   "Somebody"
          :source-name   "Somebody"
          :skill         "Druid's Potion"
          :amount        100
          :resource-type :health}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Chok'sul gains 100 health from Chok'sul's Druid's Potion." options)
         {:id            :resource-gained-from-skill
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "1/2 3:45:00.123  Chok'sul gains 100 health from Chok'sul's Druid's Potion."
          :timestamp     (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name   "Chok'sul"
          :source-name   "Chok'sul"
          :skill         "Druid's Potion"
          :amount        100
          :resource-type :health}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  You gain 100 health from Somebody's Rejuvenation." options)
         {:id            :resource-gained-from-skill
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "1/2 3:45:00.123  You gain 100 health from Somebody's Rejuvenation."
          :timestamp     (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name   owner-char-name
          :source-name   "Somebody"
          :skill         "Rejuvenation"
          :amount        100
          :resource-type :health})))

(deftest resource-gained-from-skill-self
  (is (valid-matcher? (get-matcher regex-matchers :resource-gained-from-skill-self)))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Futilian gains 1337 health from your Rejuvenation." options)
         {:id            :resource-gained-from-skill-self
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "1/2 3:45:00.123  Futilian gains 1337 health from your Rejuvenation."
          :timestamp     (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name   "Futilian"
          :source-name   owner-char-name
          :skill         "Rejuvenation"
          :amount        1337
          :resource-type :health}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  You gain 1 Rage from your Bloodrage." options)
         {:id            :resource-gained-from-skill-self
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "1/2 3:45:00.123  You gain 1 Rage from your Bloodrage."
          :timestamp     (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name   owner-char-name
          :source-name   owner-char-name
          :skill         "Bloodrage"
          :amount        1
          :resource-type :rage}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  You gain 25 Energy from your Relentless Strikes Effect." options)
         {:id            :resource-gained-from-skill-self
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "1/2 3:45:00.123  You gain 25 Energy from your Relentless Strikes Effect."
          :timestamp     (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name   owner-char-name
          :source-name   owner-char-name
          :skill         "Relentless Strikes Effect"
          :amount        25
          :resource-type :energy}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  You gain 1000 Mana from your Replenish Mana." options)
         {:id            :resource-gained-from-skill-self
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "1/2 3:45:00.123  You gain 1000 Mana from your Replenish Mana."
          :timestamp     (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name   owner-char-name
          :source-name   owner-char-name
          :skill         "Replenish Mana"
          :amount        1000
          :resource-type :mana}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  SomePet gains 35 Happiness from your Feed Pet Effect." options)
         {:id            :resource-gained-from-skill-self
          :logfmt        :resource-gained-from-skill
          :event         :resource-gained
          :line          "1/2 3:45:00.123  SomePet gains 35 Happiness from your Feed Pet Effect."
          :timestamp     (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name   "SomePet"
          :source-name   owner-char-name
          :skill         "Feed Pet Effect"
          :amount        35
          :resource-type :happiness})))

(deftest resource-gained
  (is (valid-matcher? (get-matcher regex-matchers :resource-gained)))

  (is (= (parse-line "5/25 22:15:35.674  You gain 1144 Mana from Replenish Mana." options)
         {:id            :resource-gained
          :logfmt        :resource-gained
          :event         :resource-gained
          :line          "5/25 22:15:35.674  You gain 1144 Mana from Replenish Mana."
          :timestamp     (parse-log-timestamp "5/25 22:15:35.674" options)
          :target-name   owner-char-name
          :source-name   owner-char-name
          :skill         "Replenish Mana"
          :amount        1144
          :resource-type :mana}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  You gain 1000 health from Rejuvenation." options)
         {:id            :resource-gained
          :logfmt        :resource-gained
          :event         :resource-gained
          :line          "1/2 3:45:00.123  You gain 1000 health from Rejuvenation."
          :timestamp     (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name   owner-char-name
          :source-name   owner-char-name
          :skill         "Rejuvenation"
          :amount        1000
          :resource-type :health}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  You gain 1 Rage from Bloodrage." options)
         {:id            :resource-gained
          :logfmt        :resource-gained
          :event         :resource-gained
          :line          "1/2 3:45:00.123  You gain 1 Rage from Bloodrage."
          :timestamp     (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name   owner-char-name
          :source-name   owner-char-name
          :skill         "Bloodrage"
          :amount        1
          :resource-type :rage}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  You gain 25 Energy from Relentless Strikes Effect." options)
         {:id            :resource-gained
          :logfmt        :resource-gained
          :event         :resource-gained
          :line          "1/2 3:45:00.123  You gain 25 Energy from Relentless Strikes Effect."
          :timestamp     (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name   owner-char-name
          :source-name   owner-char-name
          :skill         "Relentless Strikes Effect"
          :amount        25
          :resource-type :energy})))

(deftest resource-drained-from-skill
  (is (valid-matcher? (get-matcher regex-matchers :resource-drained-from-skill)))

  (is (= (parse-line "6/16 22:36:47.916  Lazyspawn's Flee drains 225 Mana from Lazyspawn." options)
         {:id            :resource-drained-from-skill
          :logfmt        :resource-drained-from-skill
          :event         :resource-lost
          :line          "6/16 22:36:47.916  Lazyspawn's Flee drains 225 Mana from Lazyspawn."
          :timestamp     (parse-log-timestamp "6/16 22:36:47.916" options)
          :target-name   "Lazyspawn"
          :source-name   "Lazyspawn"
          :skill         "Flee"
          :amount        225
          :resource-type :mana}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Dreka'Sur's Unit Test's Curse drains 100 health from Greatfather Winter's Helper." options)
         {:id            :resource-drained-from-skill
          :logfmt        :resource-drained-from-skill
          :event         :resource-lost
          :line          "1/2 3:45:00.123  Dreka'Sur's Unit Test's Curse drains 100 health from Greatfather Winter's Helper."
          :timestamp     (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name   "Greatfather Winter's Helper"
          :source-name   "Dreka'Sur"
          :skill         "Unit Test's Curse"
          :amount        100
          :resource-type :health})))

(deftest resource-drained-from-skill-self
  (is (valid-matcher? (get-matcher regex-matchers :resource-drained-from-skill-self)))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Your Flee drains 225 Mana from you." options)
         {:id            :resource-drained-from-skill-self
          :logfmt        :resource-drained-from-skill
          :event         :resource-lost
          :line          "1/2 3:45:00.123  Your Flee drains 225 Mana from you."
          :timestamp     (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name   owner-char-name
          :source-name   owner-char-name
          :skill         "Flee"
          :amount        225
          :resource-type :mana})))
