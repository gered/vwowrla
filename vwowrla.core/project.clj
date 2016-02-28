(defproject vwowrla.core "0.1.0-SNAPSHOT"
  :description "Vanilla World of Warcraft Raid Log Analyzer - Core Parser and Analyzer"
  :url         "https://github.com/gered/vwowrla/vwowrla.core"
  :license     {:name "MIT License"
                :url  "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j "1.2.16"]
                 [prismatic/schema "1.0.5"]
                 [cheshire "5.5.0"]]

  :profiles {:repl {:source-paths ["repl"]}

             :dev {:dependencies [[pjstadig/humane-test-output "0.7.1"]]
                   :injections   [(require 'pjstadig.humane-test-output)
                                  (pjstadig.humane-test-output/activate!)]}})
