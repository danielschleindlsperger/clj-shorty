(ns clj-shorty.server
  (:gen-class)
  (:require [integrant.core :as ig]
            [taoensso.timbre :as log]
            [org.httpkit.server :refer [run-server]]))

(defmethod ig/init-key :clj-shorty/server [_ {:keys [config routes]}]
  (let [port (:port config)
        server (run-server routes {:port port})]
    (log/info (format "Server running @ http://localhost:%s" port))
    server))

(defmethod ig/halt-key! :clj-shorty/server [_ server]
  (when server (server)))