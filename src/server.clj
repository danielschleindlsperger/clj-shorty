(ns server
  (:gen-class)
  (:require [mount.core :refer [defstate] :as mount]
            [org.httpkit.server :refer [run-server]]
            [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :refer [parameters-middleware]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.flash :refer [wrap-flash]]
            [config :refer [cfg]]
            [handlers :refer [homepage not-found-page redirect-shorty store-shorty]]))

(def session [wrap-session {:store (cookie-store {:key (:session-secret cfg)})
                            :cookie-attrs {:max-age 3600}}])

(def routes ["/"
             ["" {:get homepage :middleware [session wrap-flash]}]
             ["shorties" {:post store-shorty :middleware [parameters-middleware session wrap-flash] :conflicting true}]
             [":id" {:get redirect-shorty :conflicting true}]])

(def app (ring/ring-handler (ring/router routes) not-found-page))

(declare web-server)
(defstate web-server
  :start (run-server app)
  :stop (web-server))
