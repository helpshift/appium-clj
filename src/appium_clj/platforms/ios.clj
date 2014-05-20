(ns ^{:doc "Wrapper functions for iOS automation."
      :author "Mayur Jadhav <mayur@helpshift.com>"}
  appium-clj.platforms.ios
  (:require [clojure.java.io :as io]
            [appium-clj.util :only [run-sh]])
  (:import io.appium.java_client.AppiumDriver
           [org.openqa.selenium WebElement By]))


(def ^:dynamic *appium-driver* nil)


(defn reset-app
  "Reset the currently running app for this session."
  []
  (. ^AppiumDriver *appium-driver* resetApp))


(defn run-on-device
  "Executes test-fn with appium-driver."
  [reset? appium-driver test-fn]
  (binding [*appium-driver* appium-driver]
    (when reset?
      (reset-app))
    (test-fn)))


(defn execute-command
  "@TODO"
  ([command]
     (. ^AppiumDriver *appium-driver* execute command nil))
  ([command args]
     (. ^AppiumDriver *appium-driver* execute command args)))


(defn find-element-by-id
  "@TODO"
  [^String id]
  (. ^AppiumDriver *appium-driver* findElement (. By name id)))


(defn find-element-by-class
  "@TODO"
  [^String class-name]
  (. ^AppiumDriver *appium-driver* findElement (. By className class-name)))


(defn find-element-by-xpath
  "@TODO"
  [^String xpath-value]
  (. ^AppiumDriver *appium-driver* findElement (. By xpath xpath-value)))


(defn click
  "@TODO"
  [^String id]
  (. ^WebElement (find-element-by-id id) click))


(defn checked?
  "@TODO"
  [^String id]
  (. ^Boolean (find-element-by-id id) isSelected))


(defn click-by-class
  "@TODO"
  [^String class-name]
  (. ^WebElement (find-element-by-class class-name) click))


(defn click-by-xpath
  "@TODO"
  [^String xpath]
  (. ^WebElement (find-element-by-xpath xpath) click))


(defn clear-input
  "@TODO"
  [^String id]
  (. ^WebElement (find-element-by-id id) clear))


(defn input-text
  "@TODO"
  [^String id ^String text]
  (. ^WebElement (find-element-by-id id)
     sendKeys (into-array [text])))


(defn input-text-by-class
  "@TODO"
  [^String class-name ^String text]
  (. ^WebElement (find-element-by-class class-name)
     sendKeys (into-array [text])))


(defn send-key-event
  "Send a key event to the device.

  key-id - code for the key pressed on the device."
  [^Integer key-id]
  (. ^AppiumDriver *appium-driver* sendKeyEvent key-id))


(defn hide-keyboard
  "Hides the keyboard by pressing the button specified
by key-name if it is showing.

   key-name - The button pressed by the mobile driver to
attempt hiding the keyboard."
  ([]
     (. ^AppiumDriver *appium-driver* hideKeyboard))
  ([^String key-name]
     (. ^AppiumDriver *appium-driver* hideKeyboard key-name)))


(defn run-app-in-background
  "Runs the current app as a background app for the number
of seconds requested.

   This is a synchronous method, it returns after the back
has been returned to the foreground.
   timeout - Number of seconds to run App in background"
  [^Integer timeout]
  (. ^AppiumDriver *appium-driver* runAppInBackground timeout))


(defn perform-touch-action
  "Performs a chain of touch actions, which together can be
considered an entire gesture.

   See the Webriver 3 spec
https://dvcs.w3.org/hg/webdriver/raw-file/default/webdriver-spec.html

   It's more convenient to call the perform() method of the
TouchAction object itself.
   touch-action - A TouchAction object, which contains a list
of individual touch actions to perform.
   Returns the same touch-action object"
  [touch-action]
  (. ^AppiumDriver *appium-driver* performTouchAction touch-action))


(defn perform-mulit-touch-action
  "Performs multiple TouchAction gestures at the same time,
to simulate multiple fingers/touch inputs.

   See the Webriver 3 spec
https://dvcs.w3.org/hg/webdriver/raw-file/default/webdriver-spec.html

   It's more convenient to call the perform() method of the
MultiTouchAction object.

  multi-action - the MultiTouchAction object to perform."
  [multi-action]
  (. ^AppiumDriver *appium-driver* performMultiTouchAction multi-action))


(defn tap
  "Convenience method for tapping the center of an element or
a position on the screen.

   fingers - number of fingers/appendages to tap with
   web-element - element to tap
   timeout -  how long between pressing down, and lifting
fingers/appendages
   x - x coordinate
   y - y coordinate"
  ([^Integer fingers ^WebElement web-element ^Integer timeout]
     (. ^AppiumDriver *appium-driver* tap fingers web-element timeout))
  ([^Integer fingers ^Integer x ^Integer y ^Integer timeout]
     (. ^AppiumDriver *appium-driver* tap fingers x y timeout)))


(defn swipe
  "Convenience method for swiping across the screen.

   startx - starting x coordinate
   starty - starting y coordinate
   endx - ending x coordinate
   endy - ending y coordinate
   timeout - amount of time in milliseconds for the entire
swipe action to take."
  [^Integer startx ^Integer starty ^Integer endx ^Integer endy ^Integer timeout]
  (. ^AppiumDriver *appium-driver* swipe startx starty endx endy timeout))


(defn pinch
  "Convenience method for pinching an element or position on the screen.
   \"pinching\" refers to the action of two appendages pressing
the screen and sliding towards each other.

   NOTE: This convenience method places the initial touches around
the element, if this would happen to place one of them off the screen,
appium with return an outOfBounds error. In this case, revert to
using the MultiTouchAction api instead of this method.

  web-element - The element to pinch
  x - x coordinate to terminate the pinch on
  y - y coordinate to terminate the pinch on"
  ([^WebElement web-element]
     (. ^AppiumDriver *appium-driver* pinch web-element))
  ([^Integer x ^Integer y]
     (. ^AppiumDriver *appium-driver* pinch x y)))


(defn zoom
  "Convenience method for \"zooming in\" on an element or position
on the screen.

   \"zooming in\" refers to the action of two appendages pressing
the screen and sliding away from each other.

   NOTE: This convenience method slides touches away from the
element, if this would happen to place one of them off the screen,
appium will return an outOfBounds error. In this case, revert to using
the MultiTouchAction api instead of this method.

   web-element - The element to pinch
   x -  x coordinate to start zoom on
   y -  y coordinate to start zoom on"
  ([^WebElement web-element]
     (. ^AppiumDriver *appium-driver* zoom web-element))
  ([^Integer x ^Integer y]
     (. ^AppiumDriver *appium-driver* zoom x y)))


(defn get-name-text-field
  "In iOS apps, named TextFields have the same accessibility Id as
their containing TableElement.

   This is a convenience method for getting the named TextField,
rather than its containing element.

   text-field-id - accessiblity id of TextField.
   Returns The textfield with the given accessibility id."
  [^String text-field-id]
  (. ^AppiumDriver *appium-driver* getNamedTextField text-field-id))


(defn is-app-installed
  "Checks if an app is installed on the device.

   bundle-id - bundleId of the app.
   Returns True if app is installed, false otherwise."
  [^String bundle-id]
  (. ^AppiumDriver *appium-driver* isAppInstalled bundle-id))


(defn install-app
  "Install an app on the mobile device.

  app-path - path to app to install."
  [^String app-path]
  (. ^AppiumDriver *appium-driver* installApp app-path))


(defn remove-app
  "Remove the specified app from the device (uninstall).

   bundle-id the bunble identifier (or app id) of the app to remove."
  [^String bundle-id]
  (. ^AppiumDriver *appium-driver* removeApp bundle-id))


(defn launch-app
  "Launch the app which was provided in the capabilities at
session creation."
  []
  (. ^AppiumDriver *appium-driver* launchApp))


(defn close-app
  "Close the app which was provided in the capabilities at session
creation."
  []
  (. ^AppiumDriver *appium-driver* closeApp))


(defn lock-screen
  "Lock the device (bring it to the lock screen) for a given number
of seconds."
  [^Integer timeout]
  (. ^AppiumDriver *appium-driver* lockScreen timeout))


(defn shake
  "Simulate shaking the device."
  []
  (. ^AppiumDriver *appium-driver* shake))


(defn complex-find
  "@TODO"
  [^String complex-query]
  (. ^AppiumDriver *appium-driver* complexFind complex-query))


(defn scroll-to
  "@TODO"
  [^String text]
  (. ^AppiumDriver *appium-driver* scrollTo ^String text))


(defn scroll-to-exact
  "@TODO"
  [^String text]
  (. ^AppiumDriver *appium-driver* scrollToExact text))


(defn rotate
  "@TODO"
  [orientation]
  (. ^AppiumDriver *appium-driver* rotate orientation))


(defn get-orientation
  "@TODO"
  []
  (. ^AppiumDriver *appium-driver* getOrientation))
