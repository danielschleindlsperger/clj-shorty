(ns assets
  (:require
   [clojure.java.io :as io]
   [mount.core :refer [defstate]]))

(defn- load-assets
  []
  (let [js (slurp (io/resource "js/app.js"))]
    {:js js}))

(declare assets)
(defstate assets
  :start (load-assets))
