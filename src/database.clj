(ns database
  (:require [mount.core :refer [defstate]]
            [next.jdbc :as jdbc]
            [config :refer [cfg]]))

(declare db)
(defstate db
  :start (jdbc/get-datasource (:db-conn-string cfg)))
