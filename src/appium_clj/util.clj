(ns ^{:doc "iOS Utility functions
            Reference: https://github.com/kapilreddy/calabash-clj"}
  appium-clj.util
  (:require [clojure.java.shell :as shell]))

(defn run-sh
  "Passes the given command to shell/sh to exexute in sub-process."
  [& commands]
  (let [op (apply shell/sh (clojure.string/split (first commands) #"\s"))]
    (if (empty? (rest commands))
      op
      (recur (rest commands)))))


(defn run-with-dir
  "Passes the given command to shell/sh to exexute command
with given directory."
  [dir & commands]
  (binding [shell/*sh-dir* dir]
    (apply run-sh commands)))
