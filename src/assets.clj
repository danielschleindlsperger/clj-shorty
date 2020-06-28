(ns assets
  (:require
   [clojure.java.io :as io]
   [mount.core :refer [defstate]]))

(defn- load-assets
  []
  (let [js (slurp (io/resource "js/app.js"))
        css (slurp (io/resource "css/app.out.css"))]
    {:js js
     :css css}))

(declare assets)
(defstate assets
  :start (load-assets))
