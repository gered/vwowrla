(ns vwowrla.core.matchers.skill-damage-to-target-test
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

(deftest skill-damages-target-elemental-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-damages-target-elemental-self)))

  (is (= (parse-line "5/25 21:42:29.230  Your Frostbolt hits Lava Surger for 881 Frost damage." options)
         {:id          :skill-damages-target-elemental-self
          :logfmt      :skill-damages-target-elemental
          :event       :skill-damage-to-target
          :line        "5/25 21:42:29.230  Your Frostbolt hits Lava Surger for 881 Frost damage."
          :timestamp   (parse-log-timestamp "5/25 21:42:29.230" options)
          :source-name owner-char-name
          :target-name "Lava Surger"
          :skill       "Frostbolt"
          :damage      881
          :damage-type :frost
          :crit?       false
          :absorbed    nil
          :resisted    nil}))

  (is (= (parse-line "5/25 21:45:50.850  Your Frostbolt crits Flamewaker Protector for 1784 Frost damage." options)
         {:id          :skill-damages-target-elemental-self
          :logfmt      :skill-damages-target-elemental
          :event       :skill-damage-to-target
          :line        "5/25 21:45:50.850  Your Frostbolt crits Flamewaker Protector for 1784 Frost damage."
          :timestamp   (parse-log-timestamp "5/25 21:45:50.850" options)
          :source-name owner-char-name
          :target-name "Flamewaker Protector"
          :skill       "Frostbolt"
          :damage      1784
          :damage-type :frost
          :crit?       true
          :absorbed    nil
          :resisted    nil}))

  (is (= (parse-line "5/25 21:46:03.864  Your Fire Blast hits Flamewaker Protector for 503 Fire damage. (167 resisted)" options)
         {:id          :skill-damages-target-elemental-self
          :logfmt      :skill-damages-target-elemental
          :event       :skill-damage-to-target
          :line        "5/25 21:46:03.864  Your Fire Blast hits Flamewaker Protector for 503 Fire damage. (167 resisted)"
          :timestamp   (parse-log-timestamp "5/25 21:46:03.864" options)
          :source-name owner-char-name
          :target-name "Flamewaker Protector"
          :skill       "Fire Blast"
          :damage      503
          :damage-type :fire
          :crit?       false
          :absorbed    nil
          :resisted    167})))

(deftest skill-damages-target-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-damages-target-self)))
  ; TODO
  )

(deftest skill-damages-target-short-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-damages-target-short-self)))
  ; TODO
  )

(deftest skill-damages-target-elemental
  (is (valid-matcher? (get-matcher regex-matchers :skill-damages-target-elemental)))

  (is (= (parse-line "5/25 21:42:31.082  Tomaka's Shadow Bolt hits Lava Surger for 1119 Shadow damage." options)
         {:id          :skill-damages-target-elemental
          :logfmt      :skill-damages-target-elemental
          :event       :skill-damage-to-target
          :line        "5/25 21:42:31.082  Tomaka's Shadow Bolt hits Lava Surger for 1119 Shadow damage."
          :timestamp   (parse-log-timestamp "5/25 21:42:31.082" options)
          :source-name "Tomaka"
          :target-name "Lava Surger"
          :skill       "Shadow Bolt"
          :damage      1119
          :damage-type :shadow
          :crit?       false
          :absorbed    nil
          :resisted    nil}))

  (is (= (parse-line "5/25 21:42:33.583  Fervens's Frostbolt crits Lava Surger for 1825 Frost damage." options)
         {:id          :skill-damages-target-elemental
          :logfmt      :skill-damages-target-elemental
          :event       :skill-damage-to-target
          :line        "5/25 21:42:33.583  Fervens's Frostbolt crits Lava Surger for 1825 Frost damage."
          :timestamp   (parse-log-timestamp "5/25 21:42:33.583" options)
          :source-name "Fervens"
          :target-name "Lava Surger"
          :skill       "Frostbolt"
          :damage      1825
          :damage-type :frost
          :crit?       true
          :absorbed    nil
          :resisted    nil}))

  (is (= (parse-line "5/25 21:42:42.644  Magnomage's Fire Blast hits Lava Surger for 171 Fire damage. (513 resisted)" options)
         {:id          :skill-damages-target-elemental
          :logfmt      :skill-damages-target-elemental
          :event       :skill-damage-to-target
          :line        "5/25 21:42:42.644  Magnomage's Fire Blast hits Lava Surger for 171 Fire damage. (513 resisted)"
          :timestamp   (parse-log-timestamp "5/25 21:42:42.644" options)
          :source-name "Magnomage"
          :target-name "Lava Surger"
          :skill       "Fire Blast"
          :damage      171
          :damage-type :fire
          :crit?       false
          :absorbed    nil
          :resisted    513}))

  (is (= (parse-line "5/25 21:51:35.316  Magmadar's Magma Spit hits Eggs for 3 Fire damage. (91 absorbed)" options)
         {:id          :skill-damages-target-elemental
          :logfmt      :skill-damages-target-elemental
          :event       :skill-damage-to-target
          :line        "5/25 21:51:35.316  Magmadar's Magma Spit hits Eggs for 3 Fire damage. (91 absorbed)"
          :timestamp   (parse-log-timestamp "5/25 21:51:35.316" options)
          :source-name "Magmadar"
          :target-name "Eggs"
          :skill       "Magma Spit"
          :damage      3
          :damage-type :fire
          :crit?       false
          :absorbed    91
          :resisted    nil}))

  (is (= (parse-line "5/25 21:56:49.465  Flame Imp's Fire Nova hits you for 89 Fire damage. (409 resisted) (320 absorbed)" options)
         {:id          :skill-damages-target-elemental
          :logfmt      :skill-damages-target-elemental
          :event       :skill-damage-to-target
          :line        "5/25 21:56:49.465  Flame Imp's Fire Nova hits you for 89 Fire damage. (409 resisted) (320 absorbed)"
          :timestamp   (parse-log-timestamp "5/25 21:56:49.465" options)
          :source-name "Flame Imp"
          :target-name owner-char-name
          :skill       "Fire Nova"
          :damage      89
          :damage-type :fire
          :crit?       false
          :absorbed    320
          :resisted    409}))

  (is (= (parse-line "5/25 22:21:33.172  Firewalker's Fire Blossom hits Ruktuku for 1430 Fire damage. (798 resisted) (964 absorbed)" options)
         {:id          :skill-damages-target-elemental
          :logfmt      :skill-damages-target-elemental
          :event       :skill-damage-to-target
          :line        "5/25 22:21:33.172  Firewalker's Fire Blossom hits Ruktuku for 1430 Fire damage. (798 resisted) (964 absorbed)"
          :timestamp   (parse-log-timestamp "5/25 22:21:33.172" options)
          :source-name "Firewalker"
          :target-name "Ruktuku"
          :skill       "Fire Blossom"
          :damage      1430
          :damage-type :fire
          :crit?       false
          :absorbed    964
          :resisted    798})))

; TODO: might not need this one... see comments for the associated matcher
(deftest skill-damages-target
  (is (valid-matcher? (get-matcher regex-matchers :skill-damages-target)))
  ; TODO
  )

(deftest skill-damages-target-short
  (is (valid-matcher? (get-matcher regex-matchers :skill-damages-target-short)))

  (is (= (parse-line "5/25 21:42:27.247  Peasemold's Ambush crits Lava Surger for 1398." options)
         {:id          :skill-damages-target-short
          :logfmt      :skill-damages-target-short
          :event       :skill-damage-to-target
          :line        "5/25 21:42:27.247  Peasemold's Ambush crits Lava Surger for 1398."
          :timestamp   (parse-log-timestamp "5/25 21:42:27.247" options)
          :source-name "Peasemold"
          :target-name "Lava Surger"
          :skill       "Ambush"
          :damage      1398
          :damage-type :physical
          :crit?       true
          :absorbed    nil
          :blocked     nil}))

  (is (= (parse-line "5/25 21:42:28.379  Eggs's Shield Slam hits Lava Surger for 240." options)
         {:id          :skill-damages-target-short
          :logfmt      :skill-damages-target-short
          :event       :skill-damage-to-target
          :line        "5/25 21:42:28.379  Eggs's Shield Slam hits Lava Surger for 240."
          :timestamp   (parse-log-timestamp "5/25 21:42:28.379" options)
          :source-name "Eggs"
          :target-name "Lava Surger"
          :skill       "Shield Slam"
          :damage      240
          :damage-type :physical
          :crit?       false
          :absorbed    nil
          :blocked     nil}))

  (is (= (parse-line "5/25 21:42:32.488  Victore's Whirlwind crits Lava Surger for 902. (31 blocked)" options)
         {:id          :skill-damages-target-short
          :logfmt      :skill-damages-target-short
          :event       :skill-damage-to-target
          :line        "5/25 21:42:32.488  Victore's Whirlwind crits Lava Surger for 902. (31 blocked)"
          :timestamp   (parse-log-timestamp "5/25 21:42:32.488" options)
          :source-name "Victore"
          :target-name "Lava Surger"
          :skill       "Whirlwind"
          :damage      902
          :damage-type :physical
          :crit?       true
          :absorbed    nil
          :blocked     31}))

  (is (= (parse-line "6/9 22:31:18.633  Golemagg the Incinerator's Earthquake hits Architrex for 1034. (460 absorbed)" options)
         {:id          :skill-damages-target-short
          :logfmt      :skill-damages-target-short
          :event       :skill-damage-to-target
          :line        "6/9 22:31:18.633  Golemagg the Incinerator's Earthquake hits Architrex for 1034. (460 absorbed)"
          :timestamp   (parse-log-timestamp "6/9 22:31:18.633" options)
          :source-name "Golemagg the Incinerator"
          :target-name "Architrex"
          :skill       "Earthquake"
          :damage      1034
          :damage-type :physical
          :crit?       false
          :absorbed    460
          :blocked     nil})))
