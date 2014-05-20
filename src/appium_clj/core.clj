(ns ^{:doc "Core functionality"
      :author "Mayur Jadhav <mayur@helpshift.com>"}
  appium-clj.core
  (:require [appium-clj.build.ios :as build-ios]
            [appium-clj.platforms.ios :as ios]))


(defn -main
  "Build an iOS project and run appium queries on it"
  [project-path provision-profile config]
  (build-ios/run-on-connected-device project-path
                                     provision-profile
                                     config
                                     (fn []
                                       (ios/click "hs-btn-decomp-report-issue")
                                       (ios/click "Send"))))
