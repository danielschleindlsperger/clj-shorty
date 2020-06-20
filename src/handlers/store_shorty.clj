(ns handlers.store-shorty
  (:require [clojure.string :refer [join]]
            [next.jdbc.sql :as sql]
            [database :refer [db]]
            [config :refer [cfg]]
            [handlers.http-util :refer [see-other]])
  (:import org.postgresql.util.PSQLException))

(defn- gen-readable-id
  "Generate a human readable, 8 character long, random string.
  Does not contain easily confusable characters like O/0 or l/I"
  []
  ;; TODO: we might filter out duplicates so that we make it impossible to have the same character multiple times in a row
  (join (repeatedly 8 #(rand-nth "abcdefghikmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVW23456789"))))

(defn- unique-violation?
  "Returns true if the cause of the supplied PSQLException is a unique constrained violation.
  See: https://www.postgresql.org/docs/12/errcodes-appendix.html"
  [^PSQLException e]
  (= "23505" (.getSQLState e)))

(defn- insert-shorty
  "Takes a target url and tries to insert it into the database with a randomly generated id.
  If the id already exists it will retry the operatoin up to 5 times and then abort with a
  return value of {:error error}.
  If it succeeds, it returns a map with the keys :id and :url"
  ([url] (insert-shorty url 0))
  ([url n]
   (if (< 5 n)
     {:error "Could not find unique id after 5 tries."}
     (let [id (gen-readable-id)]
       (try (sql/insert! db :urls {:id id :target_url url})
            {:id id :url url}
            (catch PSQLException e (if (unique-violation? e)
                                     (insert-shorty url (inc n))
                                     (throw e))))))))

(defn- shorty-url
  "Generate the url to the supplied id of a shorty."
  [id]
  (str (:base-url cfg) "/shorties/" id))

(defn store-shorty
  [req]
  ;; TODO: validate
  (let [target-url (-> req :params (get "url"))
        result (insert-shorty target-url)]
    (if (:error result)
      ;; TODO: handle better with flash message or something
      {:status 500 :body (:error result)}
      (see-other (shorty-url (:id result))))))
