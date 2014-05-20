(defproject appium-clj "0.1.0-SNAPSHOT"
  :description "Automation for iOS devices"
  :url "https://helpshift.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.7.7"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/data.json "0.2.3"]
                 [io.appium/java-client "1.4.0"]]
  :global-vars {*warn-on-reflection* true}
  :plugins [[codox "0.8.10"]])
