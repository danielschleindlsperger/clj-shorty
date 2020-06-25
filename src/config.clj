(ns config
  (:require [mount.core :refer [defstate]]
            [aero.core :as aero]
            [clojure.java.io :as io]))

(defn- str->byte-arr
  "Converts a string to a Java byte array."
  [s]
  (into-array Byte/TYPE (map byte s)))

(defn- config
  []
  (-> (io/resource "config.edn")
      (aero/read-config)
      (update :session-secret str->byte-arr)))

(declare cfg)
(defstate cfg
  :start (config))
