(ns vwowrla.core.matchers.melee-avoided-by-target-test
  (:import
    (java.util TimeZone))
  (:use
    clojure.test
    vwowrla.core.matchers.matchers-test-utils)
  (:require
    [vwowrla.core.parser :refer [parse-line]]
    [vwowrla.core.preparsing :refer [parse-log-timestamp]]
    [vwowrla.core.matchers :refer [regex-matchers]]))

(def options {:log-owner-char-name "Blasticus"
              :year                2015
              :timezone            (TimeZone/getDefault)
              :windows?            false})

(def owner-char-name (:log-owner-char-name options))
(def year (:year options))
(def timezone (:timezone options))

(deftest melee-full-absorb
  (is (valid-matcher? (get-matcher regex-matchers :melee-full-absorb)))

  (is (= (parse-line "5/25 21:51:35.315  Magmadar attacks. Eggs absorbs all the damage." options)
         {:id               :melee-full-absorb
          :logfmt           :melee-full-absorb
          :event            :melee-avoided-by-target
          :line             "5/25 21:51:35.315  Magmadar attacks. Eggs absorbs all the damage."
          :timestamp        (parse-log-timestamp "5/25 21:51:35.315" options)
          :target-name      "Eggs"
          :source-name      "Magmadar"
          :avoidance-method :absorb})))

(deftest melee-full-resist
  (is (valid-matcher? (get-matcher regex-matchers :melee-full-resist)))

  (is (= (parse-line "5/25 22:33:14.279  Baron Geddon attacks. Futilian resists all the damage." options)
         {:id               :melee-full-resist
          :logfmt           :melee-full-resist
          :event            :melee-avoided-by-target
          :line             "5/25 22:33:14.279  Baron Geddon attacks. Futilian resists all the damage."
          :timestamp        (parse-log-timestamp "5/25 22:33:14.279" options)
          :target-name      "Futilian"
          :source-name      "Baron Geddon"
          :avoidance-method :resist})))

(deftest melee-miss
  (is (valid-matcher? (get-matcher regex-matchers :melee-miss)))

  (is (= (parse-line "5/25 23:27:21.695  Architrex misses Ragnaros." options)
         {:id               :melee-miss
          :logfmt           :melee-miss
          :event            :melee-avoided-by-target
          :line             "5/25 23:27:21.695  Architrex misses Ragnaros."
          :timestamp        (parse-log-timestamp "5/25 23:27:21.695" options)
          :target-name      "Ragnaros"
          :source-name      "Architrex"
          :avoidance-method :miss})))

(deftest melee-parry
  (is (valid-matcher? (get-matcher regex-matchers :melee-parry)))

  (is (= (parse-line "5/25 23:07:34.127  Flamewaker Elite attacks. Boompow parries." options)
         {:id               :melee-parry
          :logfmt           :melee-parry
          :event            :melee-avoided-by-target
          :line             "5/25 23:07:34.127  Flamewaker Elite attacks. Boompow parries."
          :timestamp        (parse-log-timestamp "5/25 23:07:34.127" options)
          :target-name      "Boompow"
          :source-name      "Flamewaker Elite"
          :avoidance-method :parry})))

(deftest melee-dodge
  (is (valid-matcher? (get-matcher regex-matchers :melee-dodge)))

  (is (= (parse-line "5/25 23:03:44.619  Laurent attacks. Ancient Core Hound dodges." options)
         {:id               :melee-dodge
          :logfmt           :melee-dodge
          :event            :melee-avoided-by-target
          :line             "5/25 23:03:44.619  Laurent attacks. Ancient Core Hound dodges."
          :timestamp        (parse-log-timestamp "5/25 23:03:44.619" options)
          :target-name      "Ancient Core Hound"
          :source-name      "Laurent"
          :avoidance-method :dodge})))

(deftest melee-block
  (is (valid-matcher? (get-matcher regex-matchers :melee-block)))

  (is (= (parse-line "5/25 22:10:51.639  Molten Destroyer attacks. Futilian blocks." options)
         {:id               :melee-block
          :logfmt           :melee-block
          :event            :melee-avoided-by-target
          :line             "5/25 22:10:51.639  Molten Destroyer attacks. Futilian blocks."
          :timestamp        (parse-log-timestamp "5/25 22:10:51.639" options)
          :target-name      "Futilian"
          :source-name      "Molten Destroyer"
          :avoidance-method :block})))

(deftest melee-evade
  (is (valid-matcher? (get-matcher regex-matchers :melee-evade)))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  You attack. Onyxia evades." options)
         {:id               :melee-evade
          :logfmt           :melee-evade
          :event            :melee-avoided-by-target
          :line             "1/2 3:45:00.123  You attack. Onyxia evades."
          :timestamp        (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name      "Onyxia"
          :source-name      owner-char-name
          :avoidance-method :evade})))

(deftest melee-immune
  (is (valid-matcher? (get-matcher regex-matchers :melee-immune)))

  (is (= (parse-line "5/25 22:38:40.210  Agusto attacks but Lava Elemental is immune." options)
         {:id               :melee-immune
          :logfmt           :melee-immune
          :event            :melee-avoided-by-target
          :line             "5/25 22:38:40.210  Agusto attacks but Lava Elemental is immune."
          :timestamp        (parse-log-timestamp "5/25 22:38:40.210" options)
          :target-name      "Lava Elemental"
          :source-name      "Agusto"
          :avoidance-method :immune})))
