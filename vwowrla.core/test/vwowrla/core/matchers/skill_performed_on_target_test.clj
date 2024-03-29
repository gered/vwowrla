(ns vwowrla.core.matchers.skill-performed-on-target-test
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

(deftest skill-performed-on-target
  (is (valid-matcher? (get-matcher regex-matchers :skill-performed-on-target)))

  (is (= (parse-line "6/16 21:48:26.263  Acal performs Feint on Lava Surger." options)
         {:id          :skill-performed-on-target
          :logfmt      :skill-performed-on-target
          :event       :skill-performed-on-target
          :line        "6/16 21:48:26.263  Acal performs Feint on Lava Surger."
          :timestamp   (parse-log-timestamp "6/16 21:48:26.263" options)
          :target-name "Lava Surger"
          :source-name "Acal"
          :skill       "Feint"
          :spell?      false}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  You perform Feint on Onyxia." options)
         {:id          :skill-performed-on-target
          :logfmt      :skill-performed-on-target
          :event       :skill-performed-on-target
          :line        "1/2 3:45:00.123  You perform Feint on Onyxia."
          :timestamp   (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name "Onyxia"
          :source-name owner-char-name
          :skill       "Feint"
          :spell?      false}))

  (is (= (parse-line "5/25 22:27:13.538  Crayson casts Ancestral Spirit on Acal." options)
         {:id          :skill-performed-on-target
          :logfmt      :skill-performed-on-target
          :event       :skill-performed-on-target
          :line        "5/25 22:27:13.538  Crayson casts Ancestral Spirit on Acal."
          :timestamp   (parse-log-timestamp "5/25 22:27:13.538" options)
          :skill       "Ancestral Spirit"
          :target-name "Acal"
          :source-name "Crayson"
          :spell?      true}))

  (is (= (parse-line "5/25 23:23:44.343  Acal casts Melt Weapon on Acal: Gutgore Ripper damaged." options)
         {:id          :skill-performed-on-target
          :logfmt      :skill-performed-on-target
          :event       :skill-performed-on-target
          :line        "5/25 23:23:44.343  Acal casts Melt Weapon on Acal: Gutgore Ripper damaged."
          :timestamp   (parse-log-timestamp "5/25 23:23:44.343" options)
          :target-name "Acal"
          :source-name "Acal"
          :skill       "Melt Weapon"
          :spell?      true
          :extra       "Gutgore Ripper damaged"}))

  (is (= (parse-line "5/25 23:07:22.282  Aesthetera casts Polymorph: Pig on Flamewaker Healer." options)
         {:id          :skill-performed-on-target
          :logfmt      :skill-performed-on-target
          :event       :skill-performed-on-target
          :line        "5/25 23:07:22.282  Aesthetera casts Polymorph: Pig on Flamewaker Healer."
          :timestamp   (parse-log-timestamp "5/25 23:07:22.282" options)
          :target-name "Flamewaker Healer"
          :source-name "Aesthetera"
          :skill       "Polymorph: Pig"
          :spell?      true})))
