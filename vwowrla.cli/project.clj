(defproject vwowrla.cli "0.1.0-SNAPSHOT"
  :description "Vanilla World of Warcraft Raid Log Analyzer - Command Line Interface"
  :url         "https://github.com/gered/vwowrla/vwowrla.cli"
  :license     {:name "MIT License"
                :url  "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j "1.2.16"]
                 [org.clojure/tools.cli "0.3.3"]

                 [vwowrla.core "0.1.0-SNAPSHOT"]]

  :profiles {:repl {:source-paths ["repl"]}})
