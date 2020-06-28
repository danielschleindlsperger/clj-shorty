(ns server
  (:gen-class)
  (:require [mount.core :refer [defstate] :as mount]
            [org.httpkit.server :refer [run-server]]
            [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :refer [parameters-middleware]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.x-headers :refer [wrap-content-type-options wrap-frame-options wrap-xss-protection]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [config :refer [cfg]]
            [handlers :refer [homepage not-found-page redirect-shorty store-shorty]]))

(def session [wrap-session {:store (cookie-store {:key (:session-secret cfg)})
                            :cookie-name "shorty-session"
                            :cookie-attrs {:max-age (* 3600 24)
                                           :http-only true
                                           :same-site :strict}}])

(def security [wrap-anti-forgery
               [wrap-content-type-options :nosniff]
               [wrap-frame-options :deny]
               [wrap-xss-protection :block]])

(def routes ["/"
             ["" {:get homepage
                  :middleware (concat [session wrap-flash] security)}]
             ["shorties" {:post store-shorty
                          :middleware (concat [parameters-middleware session wrap-flash] security)
                          :conflicting true}]
             ;; TODO: add actual favicon
             ["favicon.ico" {:get (constantly {:status 404})
                             :conflicting true}]
             [":id" {:get redirect-shorty :conflicting true}]])

(def app (ring/ring-handler (ring/router routes) not-found-page))

(declare web-server)
(defstate web-server
  :start (run-server app)
  :stop (web-server))
