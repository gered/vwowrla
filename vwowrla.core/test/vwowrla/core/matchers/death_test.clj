(ns vwowrla.core.matchers.death-test
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

(deftest death
  (is (valid-matcher? (get-matcher regex-matchers :death)))

  (is (= (parse-line "5/25 21:17:18.944  Onyxia dies." options)
         {:id          :death
          :logfmt      :death
          :event       :death
          :line        "5/25 21:17:18.944  Onyxia dies."
          :timestamp   (parse-log-timestamp "5/25 21:17:18.944" options)
          :source-name "Onyxia"}))

  (is (= (parse-line "5/25 22:24:54.829  You die." options)
         {:id          :death
          :logfmt      :death
          :event       :death
          :line        "5/25 22:24:54.829  You die."
          :timestamp   (parse-log-timestamp "5/25 22:24:54.829" options)
          :source-name owner-char-name})))
