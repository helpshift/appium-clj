# appium-clj

A Clojure library designed to write iOS Automation.

## Requirements

- Mac OSX 10.7+
- XCode 4.5+ w/ Command Line Tools
- Install libimobiledevice library. https://github.com/benvium/libimobiledevice-macosx

## Usage

The following code block will do these things.
- Build iOS project
- Start appium server
- Start running appium tests

```clj
(require '[appium-clj.build.ios :as ios-build]
         '[appium-clj.platforms.ios :as ios])

(ios-build/run-on-connected-device {:project-path "/path/to/ios-project/"
                                    :provision-profile "provision-profile.mobileprovision"
                                    :config "Release OR Debug"}
                                   (fn []
                                      ;; Write appium queries
                                      (ios/click "button1")))
```

OR

```clj
(ios-build/run-on-connected-device {:ipa-path "/path/to/ipa-file/"}
                                   (fn []
                                      ;; Write appium queries
                                      (ios/click "button1")))
```

## License

Copyright © 2014 Helpshift, Inc.

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
