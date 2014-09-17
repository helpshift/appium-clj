(ns ^{:doc "Builds iOS app"
      :author "Mayur Jadhav <mayur@helpshift.com>"}
  appium-clj.build.ios
  (:require [clojure.string :as cs]
            [clojure.java.io :as io]
            [clojure.tools.logging :refer [info error]]
            [appium-clj.util :refer [run-sh run-with-dir some-truthy]]
            [appium-clj.platforms.ios :as ios])
  (:import java.io.IOException
           io.appium.java_client.AppiumDriver
           [org.openqa.selenium WebElement By]
           [org.openqa.selenium.remote DesiredCapabilities CapabilityType]
           [java.net Socket InetSocketAddress SocketTimeoutException UnknownHostException]))


(def appium-server-port 4723)

(def command-timeout 3600)

(defonce appium-server nil)


(defn host-up?
  "Returns true if given hostname port is open."
  [hostname timeout port]
  (let [sock-addr (InetSocketAddress. hostname port)]
    (try
      (with-open [sock (Socket.)]
        (. sock connect sock-addr timeout)
        hostname)
      (catch IOException e false))))


(defn wait-for-server-up
  "Waits for port to open with given timeout and interval."
  [port timeout interval]
  (let [interval-counter (/ timeout interval)]
    (try
      (loop [counter 0]
        (cond
         (host-up? "127.0.0.1" 60000 port) true
         (> counter interval-counter) (throw (java.net.SocketTimeoutException.))
         :else (do
                 (Thread/sleep interval)
                 (recur (inc counter)))))
      (catch SocketTimeoutException e false))))


(defn build-output->app-name
  "Returns app-name from iOS project build-output."
  [build-output]
  (cs/trim (second (re-find #"CodeSign (.*)\n"
                                        build-output))))


(defn get-project-target
  "Returns project-target from 'xcodebuild -list' command output."
  [command-output]
  (cs/trim (second (re-find #"Targets:\n (.*)\n"
                                        command-output))))


(defn setup-project
  "Builds iOS project given a path and provision profile and returns
path for .ipa file.

   Finds targets for iOS project and creates .ipa for iOS project.
.ipa file is compressed format of .app file for iOS project.

   config - \"Release\" or \"Debug\" build"
  [project-path provisioning-profile config]
  (info "Building project")
  (let [project-target-list (run-with-dir project-path
                                          "xcodebuild -list")
        build-output (if (seq (:err project-target-list))
                       (throw (Exception. (:err project-target-list)))
                       (run-with-dir project-path
                                     (format "xcodebuild -target %s -sdk iphoneos -configuration %s"
                                             (get-project-target (:out project-target-list))
                                             config)))
        app-path (if (and (seq (:err build-output))
                          ;; This exception occurs while creating .app of iOS
                          ;; project. Don't have definite solution to solve the
                          ;; problem, so right now just ignoring the exception.
                          (not (second (re-find #"Using pre-existing (.*)\n"
                                                (:err build-output)))))
                   (throw (Exception. (:err build-output)))
                   (build-output->app-name (:out build-output)))
        build-ipa (run-with-dir project-path
                                (format "xcrun -sdk iphoneos PackageApplication -v %s -o %s -embed %s"
                                        (str project-path app-path)
                                        (str project-path "DemoApp.ipa")
                                        (str project-path provisioning-profile)))]
    (if (seq (:err build-ipa))
      (throw (Exception. (:err build-ipa)))
      (str project-path "DemoApp.ipa"))))


(defn start-appium-server
  "Starts appium server and listens to requests on given port."
  [port]
  (let [appium-server (future (run-sh (format "appium -p %s" port)))]
    (alter-var-root #'appium-server (constantly appium-server))
    (try
      (wait-for-server-up port 60000 1000)
      (catch UnknownHostException e false))))


(defn stop-appium-server
  "Stops all sessions of appium server."
  []
  (run-sh "ps aux | grep appium | awk '{print $2}' | xargs kill -9"))


(defn get-device-uuid
  "Returns UUID of connected iOS device."
  []
  (let [device-uuid (run-sh "idevice_id -l")]
    (first (cs/split (:out device-uuid) #"\n"))))


(defn get-device-version
  "Returns version of connected iOS device."
  [device-info]
  (cs/trim (second (re-find #"ProductVersion:(.*)\n"
                                        (:out device-info)))))


(defn get-device-type
  "Returns type of connected iOS device.
For ex. iphone, ipad, etc."
  [device-info]
  (clojure.string/trim (second (re-find #"DeviceClass:(.*)\n"
                                        (:out device-info)))))


(defn install-ios-app
  "Installs app and returns appium driver.

   Sets desired capabilities for install ios app.
Starts appium server and installs app on device."
  [ipa-path port]
  (let [capabilities (DesiredCapabilities.)
        app-path (io/file ipa-path)
        device-uuid (get-device-uuid)
        device-info (run-sh (format "ideviceinfo -u %s"
                                    device-uuid))]
    (info "Installing app on device.")
    (doto capabilities
      (.setCapability "app" app-path)
      (.setCapability "noReset" true)
      (.setCapability "udid" device-uuid)
      (.setCapability "platformName" "iOS")
      (.setCapability "newCommandTimeout" command-timeout)
      (.setCapability "device" (get-device-type device-info))
      (.setCapability (. CapabilityType VERSION)
                      (get-device-version device-info)))
    (start-appium-server port)
    (AppiumDriver.
         (io/as-url (format "http://127.0.0.1:%s/wd/hub"
                            port))
         capabilities)))


(defn run-on-connected-device
  "Installs app and returns fn to run test body on connected
iOS device.

   opts: {:project-path - path to ios project.
          :ipa-path - path to ipa file
          :provision-profile - Name of mobile provision profile.
                        Must be in same project path.
                        (Optional if ipa-path is specified)
          :config - Debug or Release (Optional if ipa-path is specified)}
   test-fn: appium queries

   For Ex.:
   (run-on-connected-device {:ipa-path \"path/to/ipa-file\"}
                            (fn []
                              ;; Write appium queries
                              (ios/click \"button1\"))

  OR
  (run-on-connected-device {:ipa-path \"path/to/ios-project/\"
                            :provision-profile \"provision-profile.mobileprovision\"
                            :config \"Release OR Debug\"}
                            (fn []
                              ;; Write appium queries
                              (ios/click \"button1\"))"
  [{:keys [project-path ipa-path]
    :as opts} test-fn]
  {:pre [(some-truthy opts :project-path :ipa-path)]}
  (let [{:keys [project-path ipa-path provision-profile config]} opts
         appium-driver (install-ios-app (if ipa-path
                                          ipa-path
                                          (setup-project project-path
                                                         provision-profile
                                                         config))
                                        appium-server-port)
         run-ios-tests (fn [tests & {:keys [reset?]
                                    :or {reset? true}}]
                         (info "Running appium tests")
                         (ios/run-on-device reset?
                                            appium-driver
                                            tests))]
    (run-ios-tests test-fn :reset? false)
    run-ios-tests))
