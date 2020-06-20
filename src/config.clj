(ns config
  (:require [mount.core :refer [defstate]]
            [aero.core :as aero]
            [clojure.java.io :as io]))

(declare cfg)
(defstate cfg
  :start (aero/read-config (io/resource "config.edn")))
