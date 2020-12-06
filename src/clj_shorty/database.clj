(ns clj-shorty.database
  (:require [mount.core :refer [defstate]]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [clj-shorty.config :refer [cfg]])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defn- mk-db
  []
  (let [db-spec {:jdbcUrl (:db-conn-string cfg)}]
    (connection/->pool HikariDataSource db-spec)))

(declare db)
(defstate db
  :start (mk-db))
