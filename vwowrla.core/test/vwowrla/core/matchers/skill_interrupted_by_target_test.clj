(ns vwowrla.core.matchers.skill-interrupted-by-target-test
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

(deftest skill-interrupt
  (is (valid-matcher? (get-matcher regex-matchers :skill-interrupt)))

  (is (= (parse-line "5/25 22:42:15.079  Shazzrah interrupts Oprawindfury's Lesser Healing Wave." options)
         {:id          :skill-interrupt
          :logfmt      :skill-interrupt
          :event       :skill-interrupted-by-target
          :line        "5/25 22:42:15.079  Shazzrah interrupts Oprawindfury's Lesser Healing Wave."
          :timestamp   (parse-log-timestamp "5/25 22:42:15.079" options)
          :target-name "Oprawindfury"
          :source-name "Shazzrah"
          :skill       "Lesser Healing Wave"}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Warug's Target Dummy interrupts Jen'shan's Unit Tester's Heal." options)
         {:id          :skill-interrupt
          :logfmt      :skill-interrupt
          :event       :skill-interrupted-by-target
          :line        "1/2 3:45:00.123  Warug's Target Dummy interrupts Jen'shan's Unit Tester's Heal."
          :timestamp   (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name "Jen'shan"
          :source-name "Warug's Target Dummy"
          :skill       "Unit Tester's Heal"}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  Warug's Target Dummy interrupts Jen'shan's Test Heal." options)
         {:id          :skill-interrupt
          :logfmt      :skill-interrupt
          :event       :skill-interrupted-by-target
          :line        "1/2 3:45:00.123  Warug's Target Dummy interrupts Jen'shan's Test Heal."
          :timestamp   (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name "Jen'shan"
          :source-name "Warug's Target Dummy"
          :skill       "Test Heal"}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  You interrupt Jen'shan's Unit Tester's Heal." options)
         {:id          :skill-interrupt
          :logfmt      :skill-interrupt
          :event       :skill-interrupted-by-target
          :line        "1/2 3:45:00.123  You interrupt Jen'shan's Unit Tester's Heal."
          :timestamp   (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name "Jen'shan"
          :source-name owner-char-name
          :skill       "Unit Tester's Heal"}))

  ; NOTE: this combat log entry was not generated by the WoW client, it was hand-written for this test
  (is (= (parse-line "1/2 3:45:00.123  You interrupt Jen'shan's Test Heal." options)
         {:id          :skill-interrupt
          :logfmt      :skill-interrupt
          :event       :skill-interrupted-by-target
          :line        "1/2 3:45:00.123  You interrupt Jen'shan's Test Heal."
          :timestamp   (parse-log-timestamp "1/2 3:45:00.123" options)
          :target-name "Jen'shan"
          :source-name owner-char-name
          :skill       "Test Heal"})))

(deftest skill-interrupt-self
  (is (valid-matcher? (get-matcher regex-matchers :skill-interrupt-self)))

  (is (= (parse-line "6/9 22:16:52.007  Shazzrah interrupts your Frostbolt." options)
         {:id          :skill-interrupt-self
          :logfmt      :skill-interrupt
          :event       :skill-interrupted-by-target
          :line        "6/9 22:16:52.007  Shazzrah interrupts your Frostbolt."
          :timestamp   (parse-log-timestamp "6/9 22:16:52.007" options)
          :target-name owner-char-name
          :source-name "Shazzrah"
          :skill       "Frostbolt"})))
