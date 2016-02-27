(ns vwowrla.core.matchers.aura-gained-test
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

(deftest aura-buff-gained
  (is (valid-matcher? (get-matcher regex-matchers :aura-buff-gained)))

  (is (= (parse-line "5/25 21:21:16.385  Vasling gains Blink." options)
         {:id          :aura-buff-gained
          :logfmt      :aura-buff-gained
          :event       :aura-gained
          :line        "5/25 21:21:16.385  Vasling gains Blink."
          :timestamp   (parse-log-timestamp "5/25 21:21:16.385" options)
          :target-name "Vasling"
          :aura-name   "Blink"
          :aura-type   :buff
          :stacks      nil}))

  (is (= (parse-line "5/25 21:16:27.257  Eggs gains Renew." options)
         {:id          :aura-buff-gained
          :logfmt      :aura-buff-gained
          :event       :aura-gained
          :line        "5/25 21:16:27.257  Eggs gains Renew."
          :timestamp   (parse-log-timestamp "5/25 21:16:27.257" options)
          :target-name "Eggs"
          :aura-name   "Renew"
          :aura-type   :buff
          :stacks      nil}))

  (is (= (parse-line "5/25 23:26:03.093  Victore gains Bonereaver's Edge (2)." options)
         {:id          :aura-buff-gained
          :logfmt      :aura-buff-gained
          :event       :aura-gained
          :line        "5/25 23:26:03.093  Victore gains Bonereaver's Edge (2)."
          :timestamp   (parse-log-timestamp "5/25 23:26:03.093" options)
          :target-name "Victore"
          :aura-name   "Bonereaver's Edge"
          :aura-type   :buff
          :stacks      2}))

  (is (= (parse-line "5/25 21:42:59.537  You gain Regrowth." options)
         {:id          :aura-buff-gained
          :logfmt      :aura-buff-gained
          :event       :aura-gained
          :line        "5/25 21:42:59.537  You gain Regrowth."
          :timestamp   (parse-log-timestamp "5/25 21:42:59.537" options)
          :target-name owner-char-name
          :aura-name   "Regrowth"
          :aura-type   :buff
          :stacks      nil})))

(deftest aura-debuff-gained
  (is (valid-matcher? (get-matcher regex-matchers :aura-debuff-gained)))

  (is (= (parse-line "5/25 21:16:46.564  Vasling is afflicted by Gnomish Death Ray." options)
         {:id          :aura-debuff-gained
          :logfmt      :aura-debuff-gained
          :event       :aura-gained
          :line        "5/25 21:16:46.564  Vasling is afflicted by Gnomish Death Ray."
          :timestamp   (parse-log-timestamp "5/25 21:16:46.564" options)
          :target-name "Vasling"
          :aura-name   "Gnomish Death Ray"
          :aura-type   :debuff
          :stacks      nil}))

  (is (= (parse-line "5/25 21:16:43.064  Onyxia is afflicted by Shadow Vulnerability (5)." options)
         {:id          :aura-debuff-gained
          :logfmt      :aura-debuff-gained
          :event       :aura-gained
          :line        "5/25 21:16:43.064  Onyxia is afflicted by Shadow Vulnerability (5)."
          :timestamp   (parse-log-timestamp "5/25 21:16:43.064" options)
          :target-name "Onyxia"
          :aura-name   "Shadow Vulnerability"
          :aura-type   :debuff
          :stacks      5}))

  (is (= (parse-line "5/25 21:43:05.511  You are afflicted by Weakened Soul." options)
         {:id          :aura-debuff-gained
          :logfmt      :aura-debuff-gained
          :event       :aura-gained
          :line        "5/25 21:43:05.511  You are afflicted by Weakened Soul."
          :timestamp   (parse-log-timestamp "5/25 21:43:05.511" options)
          :target-name owner-char-name
          :aura-name   "Weakened Soul"
          :aura-type   :debuff
          :stacks      nil})))
