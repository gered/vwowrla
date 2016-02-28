(ns vwowrla.core.matchers.melee-damage-to-target-test
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

(deftest melee-damages-target
  (is (valid-matcher? (get-matcher regex-matchers :melee-damages-target)))

  (is (= (parse-line "5/25 21:42:23.038  Eggs hits Lava Surger for 175." options)
         {:id          :melee-damages-target
          :logfmt      :melee-damages-target
          :event       :melee-damage-to-target
          :line        "5/25 21:42:23.038  Eggs hits Lava Surger for 175."
          :timestamp   (parse-log-timestamp "5/25 21:42:23.038" options)
          :target-name "Lava Surger"
          :source-name "Eggs"
          :damage      175
          :damage-type :physical
          :hit-type    :normal
          :crit?       false
          :absorbed    nil
          :blocked     nil}))

  (is (= (parse-line "6/16 21:29:44.927  You hit Lava Annihilator for 187." options)
         {:id          :melee-damages-target
          :logfmt      :melee-damages-target
          :event       :melee-damage-to-target
          :line        "6/16 21:29:44.927  You hit Lava Annihilator for 187."
          :timestamp   (parse-log-timestamp "6/16 21:29:44.927" options)
          :target-name "Lava Annihilator"
          :source-name owner-char-name
          :damage      187
          :damage-type :physical
          :hit-type    :normal
          :crit?       false
          :absorbed    nil
          :blocked     nil}))

  (is (= (parse-line "5/25 21:42:22.322  Lava Surger crits Futilian for 1382." options)
         {:id          :melee-damages-target
          :logfmt      :melee-damages-target
          :event       :melee-damage-to-target
          :line        "5/25 21:42:22.322  Lava Surger crits Futilian for 1382."
          :timestamp   (parse-log-timestamp "5/25 21:42:22.322" options)
          :target-name "Futilian"
          :source-name "Lava Surger"
          :damage      1382
          :damage-type :physical
          :hit-type    :normal
          :crit?       true
          :absorbed    nil
          :blocked     nil}))

  (is (= (parse-line "5/25 21:42:24.924  Lava Surger hits Eggs for 752. (81 blocked)" options)
         {:id          :melee-damages-target
          :logfmt      :melee-damages-target
          :event       :melee-damage-to-target
          :line        "5/25 21:42:24.924  Lava Surger hits Eggs for 752. (81 blocked)"
          :timestamp   (parse-log-timestamp "5/25 21:42:24.924" options)
          :target-name "Eggs"
          :source-name "Lava Surger"
          :damage      752
          :damage-type :physical
          :hit-type    :normal
          :crit?       false
          :absorbed    nil
          :blocked     81}))

  (is (= (parse-line "5/25 21:42:26.578  Laurent hits Lava Surger for 114. (glancing)" options)
         {:id          :melee-damages-target
          :logfmt      :melee-damages-target
          :event       :melee-damage-to-target
          :line        "5/25 21:42:26.578  Laurent hits Lava Surger for 114. (glancing)"
          :timestamp   (parse-log-timestamp "5/25 21:42:26.578" options)
          :target-name "Lava Surger"
          :source-name "Laurent"
          :damage      114
          :damage-type :physical
          :hit-type    :glancing
          :crit?       false
          :absorbed    nil
          :blocked     nil}))

  (is (= (parse-line "5/25 21:45:32.294  Lucifron hits Mightystroon for 1365. (crushing)" options)
         {:id          :melee-damages-target
          :logfmt      :melee-damages-target
          :event       :melee-damage-to-target
          :line        "5/25 21:45:32.294  Lucifron hits Mightystroon for 1365. (crushing)"
          :timestamp   (parse-log-timestamp "5/25 21:45:32.294" options)
          :target-name "Mightystroon"
          :source-name "Lucifron"
          :damage      1365
          :damage-type :physical
          :hit-type    :crushing
          :crit?       false
          :absorbed    nil
          :blocked     nil}))

  (is (= (parse-line "5/25 21:56:56.337  Flame Imp hits Aesthetera for 147. (395 absorbed)" options)
         {:id          :melee-damages-target
          :logfmt      :melee-damages-target
          :event       :melee-damage-to-target
          :line        "5/25 21:56:56.337  Flame Imp hits Aesthetera for 147. (395 absorbed)"
          :timestamp   (parse-log-timestamp "5/25 21:56:56.337" options)
          :target-name "Aesthetera"
          :source-name "Flame Imp"
          :damage      147
          :damage-type :physical
          :hit-type    :normal
          :crit?       false
          :absorbed    395
          :blocked     nil}))

  (is (= (parse-line "5/25 21:52:56.175  Magmadar hits Eggs for 160. (82 blocked) (607 absorbed)" options)
         {:id          :melee-damages-target
          :logfmt      :melee-damages-target
          :event       :melee-damage-to-target
          :line        "5/25 21:52:56.175  Magmadar hits Eggs for 160. (82 blocked) (607 absorbed)"
          :timestamp   (parse-log-timestamp "5/25 21:52:56.175" options)
          :target-name "Eggs"
          :source-name "Magmadar"
          :damage      160
          :damage-type :physical
          :hit-type    :normal
          :crit?       false
          :absorbed    607
          :blocked     82}))

  (is (= (parse-line "6/9 21:50:40.122  Gehennas hits Mightystroon for 1530. (crushing) (96 absorbed)" options)
         {:id          :melee-damages-target
          :logfmt      :melee-damages-target
          :event       :melee-damage-to-target
          :line        "6/9 21:50:40.122  Gehennas hits Mightystroon for 1530. (crushing) (96 absorbed)"
          :timestamp   (parse-log-timestamp "6/9 21:50:40.122" options)
          :target-name "Mightystroon"
          :source-name "Gehennas"
          :damage      1530
          :damage-type :physical
          :hit-type    :crushing
          :crit?       false
          :absorbed    96
          :blocked     nil})))

(deftest melee-damages-target-elemental
  (is (valid-matcher? (get-matcher regex-matchers :melee-damages-target-elemental)))

  (is (= (parse-line "5/25 21:58:30.162  Firelord hits Crayson for 792 Fire damage." options)
         {:id          :melee-damages-target-elemental
          :logfmt      :melee-damages-target-elemental
          :event       :melee-damage-to-target
          :line        "5/25 21:58:30.162  Firelord hits Crayson for 792 Fire damage."
          :timestamp   (parse-log-timestamp "5/25 21:58:30.162" options)
          :target-name "Crayson"
          :source-name "Firelord"
          :damage      792
          :damage-type :fire
          :hit-type    :normal
          :crit?       false
          :absorbed    nil
          :resisted    nil}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Twilight's Hammer Ambassador hits you for 42 Fire damage." options)
         {:id          :melee-damages-target-elemental
          :logfmt      :melee-damages-target-elemental
          :event       :melee-damage-to-target
          :line        "1/2 3:45:00.123  Twilight's Hammer Ambassador hits you for 42 Fire damage."
          :timestamp   (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name owner-char-name
          :source-name "Twilight's Hammer Ambassador"
          :damage      42
          :damage-type :fire
          :hit-type    :normal
          :crit?       false
          :absorbed    nil
          :resisted    nil}))

  (is (= (parse-line "5/25 22:03:50.816  Lava Spawn crits Strength of Earth Totem IV for 1172 Fire damage." options)
         {:id          :melee-damages-target-elemental
          :logfmt      :melee-damages-target-elemental
          :event       :melee-damage-to-target
          :line        "5/25 22:03:50.816  Lava Spawn crits Strength of Earth Totem IV for 1172 Fire damage."
          :timestamp   (parse-log-timestamp "5/25 22:03:50.816" options)
          :target-name "Strength of Earth Totem IV"
          :source-name "Lava Spawn"
          :damage      1172
          :damage-type :fire
          :hit-type    :normal
          :crit?       true
          :absorbed    nil
          :resisted    nil}))

  (is (= (parse-line "5/25 21:58:42.215  Firelord hits Eggs for 480 Fire damage. (160 resisted)" options)
         {:id          :melee-damages-target-elemental
          :logfmt      :melee-damages-target-elemental
          :event       :melee-damage-to-target
          :line        "5/25 21:58:42.215  Firelord hits Eggs for 480 Fire damage. (160 resisted)"
          :timestamp   (parse-log-timestamp "5/25 21:58:42.215" options)
          :target-name "Eggs"
          :source-name "Firelord"
          :damage      480
          :damage-type :fire
          :hit-type    :normal
          :crit?       false
          :absorbed    nil
          :resisted    160}))

  (is (= (parse-line "5/25 22:24:22.577  Baron Geddon hits Soma for 1592 Fire damage. (967 absorbed)" options)
         {:id          :melee-damages-target-elemental
          :logfmt      :melee-damages-target-elemental
          :event       :melee-damage-to-target
          :line        "5/25 22:24:22.577  Baron Geddon hits Soma for 1592 Fire damage. (967 absorbed)"
          :timestamp   (parse-log-timestamp "5/25 22:24:22.577" options)
          :target-name "Soma"
          :source-name "Baron Geddon"
          :damage      1592
          :damage-type :fire
          :hit-type    :normal
          :crit?       false
          :absorbed    967
          :resisted    nil}))

  (is (= (parse-line "5/25 23:25:53.596  Son of Flame hits Agusto for 269 Fire damage. (135 resisted) (138 absorbed)" options)
         {:id          :melee-damages-target-elemental
          :logfmt      :melee-damages-target-elemental
          :event       :melee-damage-to-target
          :line        "5/25 23:25:53.596  Son of Flame hits Agusto for 269 Fire damage. (135 resisted) (138 absorbed)"
          :timestamp   (parse-log-timestamp "5/25 23:25:53.596" options)
          :target-name "Agusto"
          :source-name "Son of Flame"
          :damage      269
          :damage-type :fire
          :hit-type    :normal
          :crit?       false
          :absorbed    138
          :resisted    135}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Baron Geddon hits Rrahg for 3609 Fire damage. (glancing)" options)
         {:id          :melee-damages-target-elemental
          :logfmt      :melee-damages-target-elemental
          :event       :melee-damage-to-target
          :line        "1/2 3:45:00.123  Baron Geddon hits Rrahg for 3609 Fire damage. (glancing)"
          :timestamp   (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name "Rrahg"
          :source-name "Baron Geddon"
          :damage      3609
          :damage-type :fire
          :hit-type    :glancing
          :crit?       false
          :absorbed    nil
          :resisted    nil}))

  (is (= (parse-line "5/25 22:24:09.229  Baron Geddon hits Rrahg for 3609 Fire damage. (crushing)" options)
         {:id          :melee-damages-target-elemental
          :logfmt      :melee-damages-target-elemental
          :event       :melee-damage-to-target
          :line        "5/25 22:24:09.229  Baron Geddon hits Rrahg for 3609 Fire damage. (crushing)"
          :timestamp   (parse-log-timestamp "5/25 22:24:09.229" options)
          :target-name "Rrahg"
          :source-name "Baron Geddon"
          :damage      3609
          :damage-type :fire
          :hit-type    :crushing
          :crit?       false
          :absorbed    nil
          :resisted    nil}))

  (is (= (parse-line "5/25 22:24:11.225  Baron Geddon hits Hsaru for 2911 Fire damage. (crushing) (646 resisted)" options)
         {:id          :melee-damages-target-elemental
          :logfmt      :melee-damages-target-elemental
          :event       :melee-damage-to-target
          :line        "5/25 22:24:11.225  Baron Geddon hits Hsaru for 2911 Fire damage. (crushing) (646 resisted)"
          :timestamp   (parse-log-timestamp "5/25 22:24:11.225" options)
          :target-name "Hsaru"
          :source-name "Baron Geddon"
          :damage      2911
          :damage-type :fire
          :hit-type    :crushing
          :crit?       false
          :absorbed    nil
          :resisted    646}))

  (is (= (parse-line "5/25 22:33:22.440  Baron Geddon hits Futilian for 1509 Fire damage. (crushing) (1376 resisted) (371 absorbed)" options)
         {:id          :melee-damages-target-elemental
          :logfmt      :melee-damages-target-elemental
          :event       :melee-damage-to-target
          :line        "5/25 22:33:22.440  Baron Geddon hits Futilian for 1509 Fire damage. (crushing) (1376 resisted) (371 absorbed)"
          :timestamp   (parse-log-timestamp "5/25 22:33:22.440" options)
          :target-name "Futilian"
          :source-name "Baron Geddon"
          :damage      1509
          :damage-type :fire
          :hit-type    :crushing
          :crit?       false
          :absorbed    371
          :resisted    1376})))
