(ns clj-shorty.database
  (:require [integrant.core :as ig]
            [clojure.string :as str]
            [next.jdbc]
            [next.jdbc.connection :as connection])
  (:import (com.zaxxer.hikari HikariDataSource)))

;; Make sure the db url is a jdbc compliant one, i.e. it starts with "jdbc:"
;; Some cloud providers will use the "postgres://" format.
(defn- jdbc-url [db-url]
  (if (str/starts-with? db-url "jdbc:") db-url (str "jdbc:" db-url)))

(defmethod ig/init-key :clj-shorty/database [_ {:keys [config]}]
  (let [db-spec {:jdbcUrl (jdbc-url (:db-conn-string config))}]
    (connection/->pool HikariDataSource db-spec)))

(defmethod ig/halt-key! :clj-shorty/database [_ ds]
  (.close ds))