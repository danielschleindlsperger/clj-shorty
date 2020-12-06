(ns clj-shorty.assets
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]))

(defn- load-assets!
  []
  (let [js (slurp (io/resource "js/app.js"))
        css (slurp (io/resource "css/app.out.css"))]
    {:js js
     :css css}))

(defmethod ig/init-key :clj-shorty/assets [_ _opts]
  (load-assets!))