(ns clj-shorty.database
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defmethod ig/init-key :clj-shorty/database [_ {:keys [config]}]
  (let [db-spec {:jdbcUrl (:db-conn-string config)}]
    (connection/->pool HikariDataSource db-spec)))

(defmethod ig/halt-key! :clj-shorty/database [_ ds]
  (.close ds))