(ns database
  (:require [mount.core :refer [defstate]]
            [next.jdbc :as jdbc]))

(declare db)
(defstate db
  ;; TODO: use config
  :start (jdbc/get-datasource "jdbc:postgresql:shorty?currentSchema=public&user=root&password=root"))
