(ns server
  (:gen-class)
  (:require [mount.core :refer [defstate] :as mount]
            [org.httpkit.server :refer [run-server]]
            [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :refer [parameters-middleware]]
            [handlers :refer [homepage show-shorty redirect-shorty store-shorty]]))

(def routes ["/"
             ["" {:get homepage}]
             ["shorties"
              ["" {:post store-shorty :middleware [parameters-middleware] :conflicting true}]
              ["/:id" {:get show-shorty}]]
             [":id" {:get redirect-shorty :conflicting true}]])

(def app (ring/ring-handler (ring/router routes)))

(declare web-server)
(defstate web-server
  :start (run-server app)
  :stop (web-server))
