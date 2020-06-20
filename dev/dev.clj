(ns dev
  (:require [mount.core :as mount]
            [clojure.tools.namespace.repl :as ctnr]))

(defn start []
  (mount/start))

(defn stop []
  (mount/stop))

(defn refresh []
  (stop)
  (ctnr/refresh))

(defn refresh-all []
  (stop)
  (ctnr/refresh-all))

(defn go
  "starts all states defined by defstate"
  []
  (start)
  :ready)

(defn reset
  "stops all states defined by defstate, reloads modified source files, and restarts the states"
  []
  (stop)
  (ctnr/refresh :after 'dev/go))
