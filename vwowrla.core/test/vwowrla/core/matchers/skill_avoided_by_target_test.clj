(ns vwowrla.core.matchers.skill-avoided-by-target-test
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

(deftest skill-miss-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-miss-self)))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Your Sunder Armor missed Firelord." options)
         {:id               :skill-miss-self
          :logfmt           :skill-miss
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Your Sunder Armor missed Firelord."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Firelord"
          :source-name      owner-char-name
          :skill            "Sunder Armor"
          :avoidance-method :miss})))

(deftest skill-miss
  (is (valid-matcher? (get-matcher regex-matchers :skill-miss)))

  (is (= (parse-line "5/25 23:04:08.426  Eggs's Sunder Armor missed Firelord." options)
         {:id               :skill-miss
          :logfmt           :skill-miss
          :event            :skill-avoided-by-target
          :line             "5/25 23:04:08.426  Eggs's Sunder Armor missed Firelord."
          :timestamp        (parse-log-timestamp "5/25 23:04:08.426" options)
          :target-name      "Firelord"
          :source-name      "Eggs"
          :skill            "Sunder Armor"
          :avoidance-method :miss}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack misses you." options)
         {:id               :skill-miss
          :logfmt           :skill-miss
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack misses you."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      owner-char-name
          :source-name      "Onyxia's Elite Guard"
          :skill            "Unit Test's Attack"
          :avoidance-method :miss}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack misses you." options)
         {:id               :skill-miss
          :logfmt           :skill-miss
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack misses you."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      owner-char-name
          :source-name      "Onyxia's Elite Guard"
          :skill            "Test Attack"
          :avoidance-method :miss})))

(deftest skill-parry-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-parry-self)))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Your Test Attack was parried by Firelord." options)
         {:id               :skill-parry-self
          :logfmt           :skill-parry
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Your Test Attack was parried by Firelord."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Firelord"
          :source-name      owner-char-name
          :skill            "Test Attack"
          :avoidance-method :parry})))

(deftest skill-parry
  (is (valid-matcher? (get-matcher regex-matchers :skill-parry)))

  (is (= (parse-line "5/25 23:02:30.319  Futilian's Heroic Strike was parried by Golemagg the Incinerator." options)
         {:id               :skill-parry
          :logfmt           :skill-parry
          :event            :skill-avoided-by-target
          :line             "5/25 23:02:30.319  Futilian's Heroic Strike was parried by Golemagg the Incinerator."
          :timestamp        (parse-log-timestamp "5/25 23:02:30.319" options)
          :target-name      "Golemagg the Incinerator"
          :source-name      "Futilian"
          :skill            "Heroic Strike"
          :avoidance-method :parry}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was parried by Tester." options)
         {:id               :skill-parry
          :logfmt           :skill-parry
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was parried by Tester."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Tester"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Unit Test's Attack"
          :avoidance-method :parry}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was parried by Tester." options)
         {:id               :skill-parry
          :logfmt           :skill-parry
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was parried by Tester."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Tester"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Test Attack"
          :avoidance-method :parry})))

(deftest skill-parry-implied-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-parry-implied-self)))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was parried." options)
         {:id               :skill-parry-implied-self
          :logfmt           :skill-parry
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was parried."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      owner-char-name
          :source-name      "Onyxia's Elite Guard"
          :skill            "Unit Test's Attack"
          :avoidance-method :parry}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was parried." options)
         {:id               :skill-parry-implied-self
          :logfmt           :skill-parry
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was parried."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      owner-char-name
          :source-name      "Onyxia's Elite Guard"
          :skill            "Test Attack"
          :avoidance-method :parry})))

(deftest skill-block-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-block-self)))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Your Test Attack was blocked by Firelord." options)
         {:id               :skill-block-self
          :logfmt           :skill-block
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Your Test Attack was blocked by Firelord."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Firelord"
          :source-name      owner-char-name
          :skill            "Test Attack"
          :avoidance-method :block})))

(deftest skill-block
  (is (valid-matcher? (get-matcher regex-matchers :skill-block)))

  (is (= (parse-line "5/25 23:08:34.885  Boompow's Shield Bash was blocked by Flamewaker Healer." options)
         {:id               :skill-block
          :logfmt           :skill-block
          :event            :skill-avoided-by-target
          :line             "5/25 23:08:34.885  Boompow's Shield Bash was blocked by Flamewaker Healer."
          :timestamp        (parse-log-timestamp "5/25 23:08:34.885" options)
          :target-name      "Flamewaker Healer"
          :source-name      "Boompow"
          :skill            "Shield Bash"
          :avoidance-method :block}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was blocked by Tester." options)
         {:id               :skill-block
          :logfmt           :skill-block
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was blocked by Tester."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Tester"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Unit Test's Attack"
          :avoidance-method :block}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was blocked by Tester." options)
         {:id               :skill-block
          :logfmt           :skill-block
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was blocked by Tester."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Tester"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Test Attack"
          :avoidance-method :block})))

(deftest skill-dodge-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-dodge-self)))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Your Test Attack was dodged by Firelord." options)
         {:id               :skill-dodge-self
          :logfmt           :skill-dodge
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Your Test Attack was dodged by Firelord."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Firelord"
          :source-name      owner-char-name
          :skill            "Test Attack"
          :avoidance-method :dodge})))

(deftest skill-dodge
  (is (valid-matcher? (get-matcher regex-matchers :skill-dodge)))

  (is (= (parse-line "5/25 23:24:42.147  Victore's Mortal Strike was dodged by Ragnaros." options)
         {:id               :skill-dodge
          :logfmt           :skill-dodge
          :event            :skill-avoided-by-target
          :line             "5/25 23:24:42.147  Victore's Mortal Strike was dodged by Ragnaros."
          :timestamp        (parse-log-timestamp "5/25 23:24:42.147" options)
          :target-name      "Ragnaros"
          :source-name      "Victore"
          :skill            "Mortal Strike"
          :avoidance-method :dodge}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was dodged by Tester." options)
         {:id               :skill-dodge
          :logfmt           :skill-dodge
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was dodged by Tester."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Tester"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Unit Test's Attack"
          :avoidance-method :dodge}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was dodged by Tester." options)
         {:id               :skill-dodge
          :logfmt           :skill-dodge
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was dodged by Tester."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Tester"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Test Attack"
          :avoidance-method :dodge})))

(deftest skill-dodge-implied-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-dodge-implied-self)))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was dodged." options)
         {:id               :skill-dodge-implied-self
          :logfmt           :skill-dodge
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was dodged."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      owner-char-name
          :source-name      "Onyxia's Elite Guard"
          :skill            "Unit Test's Attack"
          :avoidance-method :dodge}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was dodged." options)
         {:id               :skill-dodge-implied-self
          :logfmt           :skill-dodge
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was dodged."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      owner-char-name
          :source-name      "Onyxia's Elite Guard"
          :skill            "Test Attack"
          :avoidance-method :dodge})))

(deftest skill-evade-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-evade-self)))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Your Test Attack was evaded by Firelord." options)
         {:id               :skill-evade-self
          :logfmt           :skill-evade
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Your Test Attack was evaded by Firelord."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Firelord"
          :source-name      owner-char-name
          :skill            "Test Attack"
          :avoidance-method :evade})))

(deftest skill-evade
  (is (valid-matcher? (get-matcher regex-matchers :skill-evade)))

  (is (= (parse-line "5/25 23:10:18.706  Futilian's Heroic Strike was evaded by Majordomo Executus." options)
         {:id               :skill-evade
          :logfmt           :skill-evade
          :event            :skill-avoided-by-target
          :line             "5/25 23:10:18.706  Futilian's Heroic Strike was evaded by Majordomo Executus."
          :timestamp        (parse-log-timestamp "5/25 23:10:18.706" options)
          :target-name      "Majordomo Executus"
          :source-name      "Futilian"
          :skill            "Heroic Strike"
          :avoidance-method :evade}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was evaded by you." options)
         {:id               :skill-evade
          :logfmt           :skill-evade
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was evaded by you."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      owner-char-name
          :source-name      "Onyxia's Elite Guard"
          :skill            "Unit Test's Attack"
          :avoidance-method :evade}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was evaded by you." options)
         {:id               :skill-evade
          :logfmt           :skill-evade
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was evaded by you."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      owner-char-name
          :source-name      "Onyxia's Elite Guard"
          :skill            "Test Attack"
          :avoidance-method :evade})))

(deftest skill-resist-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-resist-self)))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Firelord resists your Test Attack." options)
         {:id               :skill-resist-self
          :logfmt           :skill-resist
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Firelord resists your Test Attack."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Firelord"
          :source-name      owner-char-name
          :skill            "Test Attack"
          :avoidance-method :resist})))

(deftest skill-resist
  (is (valid-matcher? (get-matcher regex-matchers :skill-resist)))

  (is (= (parse-line "6/9 21:37:10.291  Vamp resists Acal's Sap." options)
         {:id               :skill-resist
          :logfmt           :skill-resist
          :event            :skill-avoided-by-target
          :line             "6/9 21:37:10.291  Vamp resists Acal's Sap."
          :timestamp        (parse-log-timestamp "6/9 21:37:10.291" options)
          :target-name      "Vamp"
          :source-name      "Acal"
          :skill            "Sap"
          :avoidance-method :resist}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard resists Umi's Mechanical Yeti's Unit Test's Attack." options)
         {:id               :skill-resist
          :logfmt           :skill-resist
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard resists Umi's Mechanical Yeti's Unit Test's Attack."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Onyxia's Elite Guard"
          :source-name      "Umi's Mechanical Yeti"
          :skill            "Unit Test's Attack"
          :avoidance-method :resist}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard resists Umi's Mechanical Yeti's Test Attack." options)
         {:id               :skill-resist
          :logfmt           :skill-resist
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard resists Umi's Mechanical Yeti's Test Attack."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Onyxia's Elite Guard"
          :source-name      "Umi's Mechanical Yeti"
          :skill            "Test Attack"
          :avoidance-method :resist})))

(deftest skill-absorb
  (is (valid-matcher? (get-matcher regex-matchers :skill-absorb)))

  (is (= (parse-line "5/25 21:15:43.435  Onyxia's Eruption is absorbed by Leaf." options)
         {:id               :skill-absorb
          :logfmt           :skill-absorb
          :event            :skill-avoided-by-target
          :line             "5/25 21:15:43.435  Onyxia's Eruption is absorbed by Leaf."
          :timestamp        (parse-log-timestamp "5/25 21:15:43.435" options)
          :target-name      "Leaf"
          :source-name      "Onyxia"
          :skill            "Eruption"
          :avoidance-method :absorb}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack is absorbed by Tester." options)
         {:id               :skill-absorb
          :logfmt           :skill-absorb
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack is absorbed by Tester."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Tester"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Unit Test's Attack"
          :avoidance-method :absorb}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack is absorbed by Tester." options)
         {:id               :skill-absorb
          :logfmt           :skill-absorb
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack is absorbed by Tester."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Tester"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Test Attack"
          :avoidance-method :absorb})))

(deftest skill-absorb-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-absorb-self)))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Your Test Attack is absorbed by Firelord." options)
         {:id               :skill-absorb-self
          :logfmt           :skill-absorb
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Your Test Attack is absorbed by Firelord."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Firelord"
          :source-name      owner-char-name
          :skill            "Test Attack"
          :avoidance-method :absorb})))

(deftest skill-absorb-2
  (is (valid-matcher? (get-matcher regex-matchers :skill-absorb-2)))

  (is (= (parse-line "5/25 21:43:08.285  You absorb Flame Imp's Fire Nova." options)
         {:id               :skill-absorb-2
          :logfmt           :skill-absorb-2
          :event            :skill-avoided-by-target
          :line             "5/25 21:43:08.285  You absorb Flame Imp's Fire Nova."
          :timestamp        (parse-log-timestamp "5/25 21:43:08.285" options)
          :target-name      owner-char-name
          :source-name      "Flame Imp"
          :skill            "Fire Nova"
          :avoidance-method :absorb}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Umi's Mechanical Yeti absorbs Onyxia's Elite Guard's Unit Test's Attack." options)
         {:id               :skill-absorb-2
          :logfmt           :skill-absorb-2
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Umi's Mechanical Yeti absorbs Onyxia's Elite Guard's Unit Test's Attack."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Umi's Mechanical Yeti"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Unit Test's Attack"
          :avoidance-method :absorb}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Umi's Mechanical Yeti absorbs Onyxia's Elite Guard's Test Attack." options)
         {:id               :skill-absorb-2
          :logfmt           :skill-absorb-2
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Umi's Mechanical Yeti absorbs Onyxia's Elite Guard's Test Attack."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Umi's Mechanical Yeti"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Test Attack"
          :avoidance-method :absorb}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  You absorb Onyxia's Elite Guard's Test Attack." options)
         {:id               :skill-absorb-2
          :logfmt           :skill-absorb-2
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  You absorb Onyxia's Elite Guard's Test Attack."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      owner-char-name
          :source-name      "Onyxia's Elite Guard"
          :skill            "Test Attack"
          :avoidance-method :absorb}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  You absorb Onyxia's Elite Guard's Unit Test's Attack." options)
         {:id               :skill-absorb-2
          :logfmt           :skill-absorb-2
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  You absorb Onyxia's Elite Guard's Unit Test's Attack."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      owner-char-name
          :source-name      "Onyxia's Elite Guard"
          :skill            "Unit Test's Attack"
          :avoidance-method :absorb})))

(deftest skill-absorb-2-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-absorb-2-self)))

  (is (= (parse-line "2/7 22:35:41.365  You absorb your Poisonous Blood." options)
         {:id               :skill-absorb-2-self
          :logfmt           :skill-absorb-2-self
          :event            :skill-avoided-by-target
          :line             "2/7 22:35:41.365  You absorb your Poisonous Blood."
          :timestamp        (parse-log-timestamp "2/7 22:35:41.365" options)
          :target-name      owner-char-name
          :source-name      owner-char-name
          :skill            "Poisonous Blood"
          :avoidance-method :absorb})))

(deftest skill-resist-2
  (is (valid-matcher? (get-matcher regex-matchers :skill-resist-2)))

  (is (= (parse-line "5/25 21:16:31.690  Ruktuku's Mind Flay was resisted by Onyxia." options)
         {:id               :skill-resist-2
          :logfmt           :skill-resist-2
          :event            :skill-avoided-by-target
          :line             "5/25 21:16:31.690  Ruktuku's Mind Flay was resisted by Onyxia."
          :timestamp        (parse-log-timestamp "5/25 21:16:31.690" options)
          :target-name      "Onyxia"
          :source-name      "Ruktuku"
          :skill            "Mind Flay"
          :avoidance-method :resist}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was resisted by Tester." options)
         {:id               :skill-resist-2
          :logfmt           :skill-resist-2
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Unit Test's Attack was resisted by Tester."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Tester"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Unit Test's Attack"
          :avoidance-method :resist}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was resisted by Tester." options)
         {:id               :skill-resist-2
          :logfmt           :skill-resist-2
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Test Attack was resisted by Tester."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Tester"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Test Attack"
          :avoidance-method :resist})))

(deftest skill-resist-2-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-resist-2-self)))

  (is (= (parse-line "5/25 21:14:15.457  Your Frostbolt was resisted by Onyxia." options)
         {:id               :skill-resist-2-self
          :logfmt           :skill-resist-2
          :event            :skill-avoided-by-target
          :line             "5/25 21:14:15.457  Your Frostbolt was resisted by Onyxia."
          :timestamp        (parse-log-timestamp "5/25 21:14:15.457" options)
          :target-name      "Onyxia"
          :source-name      owner-char-name
          :skill            "Frostbolt"
          :avoidance-method :resist})))

(deftest skill-resist-no-source
  (is (valid-matcher? (get-matcher regex-matchers :skill-resist-implied-self)))

  (is (= (parse-line "5/25 22:40:54.804  Shazzrah's Shazzrah's Curse was resisted." options)
         {:id               :skill-resist-implied-self
          :logfmt           :skill-resist-2
          :event            :skill-avoided-by-target
          :line             "5/25 22:40:54.804  Shazzrah's Shazzrah's Curse was resisted."
          :timestamp        (parse-log-timestamp "5/25 22:40:54.804" options)
          :target-name      owner-char-name
          :source-name      "Shazzrah"
          :skill            "Shazzrah's Curse"
          :avoidance-method :resist}))

  (is (= (parse-line "5/25 23:08:26.297  Flamewaker Healer's Shadow Shock was resisted." options)
         {:id               :skill-resist-implied-self
          :logfmt           :skill-resist-2
          :event            :skill-avoided-by-target
          :line             "5/25 23:08:26.297  Flamewaker Healer's Shadow Shock was resisted."
          :timestamp        (parse-log-timestamp "5/25 23:08:26.297" options)
          :target-name      owner-char-name
          :source-name      "Flamewaker Healer"
          :skill            "Shadow Shock"
          :avoidance-method :resist}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Tester's Curse was resisted." options)
         {:id               :skill-resist-implied-self
          :logfmt           :skill-resist-2
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Tester's Curse was resisted."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      owner-char-name
          :source-name      "Onyxia's Elite Guard"
          :skill            "Tester's Curse"
          :avoidance-method :resist}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Test Curse was resisted." options)
         {:id               :skill-resist-implied-self
          :logfmt           :skill-resist-2
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Test Curse was resisted."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      owner-char-name
          :source-name      "Onyxia's Elite Guard"
          :skill            "Test Curse"
          :avoidance-method :resist})))

(deftest skill-immune-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-immune-self)))

  (is (= (parse-line "5/25 22:02:47.619  Your Fire Blast failed. Gehennas is immune." options)
         {:id               :skill-immune-self
          :logfmt           :skill-immune
          :event            :skill-avoided-by-target
          :line             "5/25 22:02:47.619  Your Fire Blast failed. Gehennas is immune."
          :timestamp        (parse-log-timestamp "5/25 22:02:47.619" options)
          :target-name      "Gehennas"
          :source-name      owner-char-name
          :skill            "Fire Blast"
          :avoidance-method :immune})))

(deftest skill-immune
  (is (valid-matcher? (get-matcher regex-matchers :skill-immune)))

  (is (= (parse-line "5/25 21:16:35.991  Onyxia's Bellowing Roar fails. Slater is immune." options)
         {:id               :skill-immune
          :logfmt           :skill-immune
          :event            :skill-avoided-by-target
          :line             "5/25 21:16:35.991  Onyxia's Bellowing Roar fails. Slater is immune."
          :timestamp        (parse-log-timestamp "5/25 21:16:35.991" options)
          :target-name      "Slater"
          :source-name      "Onyxia"
          :skill            "Bellowing Roar"
          :avoidance-method :immune}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Tester's Curse fails. Slater is immune." options)
         {:id               :skill-immune
          :logfmt           :skill-immune
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Tester's Curse fails. Slater is immune."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Slater"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Tester's Curse"
          :avoidance-method :immune}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Onyxia's Elite Guard's Test Curse fails. Slater is immune." options)
         {:id               :skill-immune
          :logfmt           :skill-immune
          :event            :skill-avoided-by-target
          :line             "1/2 3:45:00.123  Onyxia's Elite Guard's Test Curse fails. Slater is immune."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Slater"
          :source-name      "Onyxia's Elite Guard"
          :skill            "Test Curse"
          :avoidance-method :immune})))

(deftest skill-immune-2-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-immune-2-self)))

  (is (= (parse-line "1/15 22:22:00.503  Babyorc is immune to your Polymorph: Pig." options)
         {:id               :skill-immune-2-self
          :logfmt           :skill-immune-2
          :event            :skill-avoided-by-target
          :line             "1/15 22:22:00.503  Babyorc is immune to your Polymorph: Pig."
          :timestamp        (parse-log-timestamp "1/15 22:22:00.503" options)
          :target-name      "Babyorc"
          :source-name      owner-char-name
          :skill            "Polymorph: Pig"
          :avoidance-method :immune})))

(deftest skill-immune-2
  (is (valid-matcher? (get-matcher regex-matchers :skill-immune-2)))

  (is (= (parse-line "1/18 22:17:10.168  Nefarian is immune to Impale's Immolation." options)
         {:id               :skill-immune-2
          :logfmt           :skill-immune-2
          :event            :skill-avoided-by-target
          :line             "1/18 22:17:10.168  Nefarian is immune to Impale's Immolation."
          :timestamp        (parse-log-timestamp "1/18 22:17:10.168" options)
          :target-name      "Nefarian"
          :source-name      "Impale"
          :skill            "Immolation"
          :avoidance-method :immune}))

  (is (= (parse-line "1/18 22:15:45.379  Spookee is immune to Aesthetera's Polymorph: Pig." options)
         {:id               :skill-immune-2
          :logfmt           :skill-immune-2
          :event            :skill-avoided-by-target
          :line             "1/18 22:15:45.379  Spookee is immune to Aesthetera's Polymorph: Pig."
          :timestamp        (parse-log-timestamp "1/18 22:15:45.379" options)
          :target-name      "Spookee"
          :source-name      "Aesthetera"
          :skill            "Polymorph: Pig"
          :avoidance-method :immune})))
