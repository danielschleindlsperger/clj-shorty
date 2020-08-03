(ns main
  (:gen-class)
  (:require [mount.core :as mount]
            [taoensso.timbre :as timbre]
            [server]))

(defn -main
  "The main entry point when the app is not running in REPL-mode."
  [& args]
  (timbre/info "Bootstrapping the application ...")
  (mount/start)
  ;; TODO: shutdown mount components on shutdown signal
  (timbre/info "Application is ready."))
