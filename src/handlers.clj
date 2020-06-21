(ns handlers
  (:require [hiccup.page :refer [html5]]
            [clojure.string :refer [join]]
            [next.jdbc.sql :as sql]
            [database :refer [db]]
            [config :refer [cfg]]
            [util.http :refer [ok see-other temporary-redirect]]
            [util.url :refer [resource-url shareable-url]])
  (:import org.postgresql.util.PSQLException))

(defn render-homepage []
  (html5 {:lang "en"}
         [:head
          [:title "Shorty - The coolest URL shortener ever!"]
          [:meta {:charset "UTF-8"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
          [:link {:href "https://unpkg.com/tailwindcss@^1.0/dist/tailwind.min.css" :rel "stylesheet"}]]
         [:body
          [:main.mt-8.max-w-2xl.p-4.mx-auto
           [:h1.text-3xl.text-center.font-bold "Hello Shorty"]
           [:form.mt-16.flex.max-w-xl {:method "POST" :action "/shorties"}
            [:input.flex-grow.px-3.py-1.bg-gray-200.placeholder-gray-600.rounded-l
             {:type "text" :name "url" :value "https://example.com":placeholder "https://example.com"}]
            [:button.px-3.py-1.bg-green-300.rounded-r	{:type "submit"} "Shorten"]]]]))

(defn homepage
  [_]
  (ok (render-homepage)))

;; STORE shorty

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

(defn store-shorty
  [req]
  ;; TODO: validate
  (let [target-url (-> req :params (get "url"))
        result (insert-shorty target-url)]
    (if (:error result)
      ;; TODO: handle better with flash message or something
      {:status 500 :body (:error result)}
      (see-other (resource-url (:id result))))))

;; SHOW shorty

(defn- render-shorty
  [shorty]
  (html5 {:lang "en"}
         [:head
          [:title "Shorty - The coolest URL shortener ever!"]
          [:meta {:charset "UTF-8"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
          [:link {:href "https://unpkg.com/tailwindcss@^1.0/dist/tailwind.min.css" :rel "stylesheet"}]]
         [:body
          [:main.max-w-2xl.p-4.mx-auto
           [:h1.text-3xl.text-center.font-bold "Your Shorty"]
           [:h2 "Your url has been shortened successfully!"]
           ;; TODO: make url copy-able
           [:div (shareable-url (:urls/id shorty))]
           [:div (:urls/target_url shorty)]
           [:div (:urls/created_at shorty)]]]))

(defn show-shorty
  [req]
  (let [id (-> req :path-params :id)
        shorty (sql/get-by-id db :urls id)]
    (ok (render-shorty shorty))))

;; REDIRECT shorty

(defn redirect-shorty
  [req]
  (let [id (-> req :path-params :id)
        shorty (sql/get-by-id db :urls id)]
    (temporary-redirect (:urls/target_url shorty))))
