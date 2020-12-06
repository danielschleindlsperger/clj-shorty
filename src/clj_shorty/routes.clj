(ns clj-shorty.routes
  (:require [integrant.core :as ig]
            [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :refer [parameters-middleware]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.x-headers :refer [wrap-content-type-options wrap-frame-options wrap-xss-protection]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [clj-shorty.handlers :refer [homepage not-found-page redirect-shorty store-shorty]]))

(defn session [cfg]
  [wrap-session {:store (cookie-store {:key (:session-secret cfg)})
                 :cookie-name "shorty-session"
                 :cookie-attrs {:max-age (* 3600 24)
                                :http-only true
                                :same-site :strict}}])

(def security [wrap-anti-forgery
               [wrap-content-type-options :nosniff]
               [wrap-frame-options :deny]
               [wrap-xss-protection :block]])

(defn routes [{:keys [cfg ds assets]}]
  ["/"
   ["" {:get (homepage cfg assets)
        :middleware (concat [(session cfg) wrap-flash] security)}]
   ["shorties" {:post (store-shorty ds)
                :middleware (concat [parameters-middleware session wrap-flash] security)
                :conflicting true}]
             ;; TODO: add actual favicon
   ["favicon.ico" {:get (constantly {:status 404})
                   :conflicting true}]
   ["robots.txt" {:get (constantly {:status 200
                                    :body "User-agent: *\nDisallow:"
                                    :headers {"content-type" "text/plain; charset=UTF-8"}})
                  :conflicting true}]
   [":id" {:get redirect-shorty :conflicting true}]])

(defn app [opts]
  (ring/ring-handler (ring/router (routes opts)) not-found-page))

(defmethod ig/init-key :clj-shorty/routes [_ {:keys [config ds assets]}]
  (app {:cfg config :ds ds :assets assets}))