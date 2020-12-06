(ns clj-shorty.main
  (:gen-class)
  (:require [integrant.core :as ig]
            [taoensso.timbre :as timbre]
            [clj-shorty.system :refer [config]]))

(defn -main
  "The main entry point when the app is not running in REPL-mode."
  [& args]
  (timbre/info "Bootstrapping the application ...")
  (let [system (ig/init config)]
    (-> (Runtime/getRuntime)
        (.addShutdownHook (Thread. ^Runnable (fn []
                                               (timbre/info "Shutting down system")
                                               (ig/halt! system)))))
    (timbre/info "Application is ready.")))
