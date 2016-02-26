(ns vwowrla.cli.core
  (:gen-class)
  (:import
    (java.util Date TimeZone))
  (:require
    [clojure.string :as string]
    [clojure.tools.cli :as cli]))

(defn- get-current-year []
  (+ 1900 (.getYear (Date.))))

(defn- get-timezone [^String tz]
  (let [timezone (TimeZone/getTimeZone tz)]
    (if (= (.getID timezone) tz)
      timezone)))

(defn- ->usage-string [options-summary]
  (->> ["Vanilla World of Warcraft Raid Log Analyzer"
        ""
        "Usage: vwowrla.cli -n CHARNAME -y YEAR [options] LOGFILE"
        ""
        "LOGFILE is the path/filename of the combat log file to parse."
        ""
        options-summary
        ""]
       (string/join \newline)))

(defn- ->error-string [errors]
  (str "Invalid options/arguments.\n"
       (string/join \newline errors)))

(defn- exit [status msg]
  (println msg)
  (System/exit status))

(def ^:private cli-options
  [["-n" "--charname CHARNAME"
    "Required. Character name of the player who recorded the combat log file."
    :parse-fn #(-> (str %) (string/trim))
    :validate [#(not (empty? %)) "Character name is required."]]
   ["-y" "--year YEAR"
    "The year that the combat log was taken in. Current year if not specified."
    :parse-fn #(Integer/parseInt %)]
   ["-z" "--timezone TIMEZONE"
    "Timezone that the timestamps within the combat log file are in. Current timezone if not specified."
    :parse-fn #(-> (str %) (string/trim))
    :validate [#(or (empty? %)
                    (not (nil? (get-timezone %))))
               "Valid timezone name/abbreviation is required."]]
   ["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    ; option pre-validations
    (cond
      (:help options)            (exit 0 (->usage-string summary))
      (not= (count arguments) 1) (exit 1 (->usage-string summary))
      errors                     (exit 1 (->error-string errors)))

    (let [filename (first arguments)
          {:keys [year charname timezone]
           :or   {year     (get-current-year)
                  charname nil
                  timezone (.getID (TimeZone/getDefault))}}
          options]
      (cond
        (nil? charname)
        (exit 1 (->error-string ["Character name is required."])))

      ; TODO: run parse
      (println
        "charname:" charname \newline
        "year:" year \newline
        "timezone:" timezone \newline
        "logfile:" filename))))