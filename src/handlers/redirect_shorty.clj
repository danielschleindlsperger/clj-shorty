(ns handlers.redirect-shorty
  (:require [next.jdbc.sql :as sql]
            [database :refer [db]]
            [config :refer [cfg]]
            [handlers.http-util :refer [temporary-redirect]]))

(defn redirect-shorty
  [req]
  (let [id (-> req :path-params :id)
        shorty (sql/get-by-id db :urls id)]
    (temporary-redirect (:urls/target_url shorty))))
