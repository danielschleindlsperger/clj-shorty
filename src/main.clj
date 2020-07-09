(ns main
  (:gen-class)
  (:require [mount.core :as mount]
            [server]))

(defn -main
  "The main entry point when the app is not running in REPL-mode."
  [& args]
  (println "\nCreating your server...")
  (mount/start)
  ;; TODO: shutdown mount components on shutdown signal
  (println "Done.."))
