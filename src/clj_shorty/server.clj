(ns clj-shorty.server
  (:gen-class)
  (:require [integrant.core :as ig]
            [org.httpkit.server :refer [run-server]]))

(defmethod ig/init-key :clj-shorty/server [_ {:keys [config routes]}]
  (run-server routes {:port (:port config)}))

(defmethod ig/halt-key! :clj-shorty/server [_ server] (server))