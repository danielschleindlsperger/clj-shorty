(ns handlers
  (:require [hiccup.page :refer [html5]]
            [clojure.string :refer [join]]
            [clojure.java.io :refer [as-url]]
            [next.jdbc.sql :as sql]
            [database :refer [db]]
            [util.http :refer [with-flash ok see-other temporary-redirect]]
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
             {:type "text" :name "url" :value "https://example.com" :placeholder "https://example.com"}]
            [:button.px-3.py-1.bg-green-300.rounded-r	{:type "submit"} "Shorten"]]]]))

(defn homepage
  [_]
  (ok (render-homepage)))

;; 404 page
(defn not-found-page
  [req]
  (ok (html5 {:lang "en"}
             [:head
              [:title "Not Found.."]
              [:meta {:charset "UTF-8"}]
              [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
              [:link {:href "https://unpkg.com/tailwindcss@^1.0/dist/tailwind.min.css" :rel "stylesheet"}]]
             [:body
              [:main.mt-8.max-w-2xl.p-4.mx-auto
               [:h1.text-3xl.text-center.font-bold "Not Found"]
               [:h2 "The requested page could not be found."]
               [:p "Check if you correctly entered the URL and re-submit."]]])))

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

(defn- validate-url
  "Check if the given url is a valid HTTP URL.
  Returns a map with the :url if is valid, or an :error message if it is invalid."
  [url]
  (try (let [prot (-> url as-url .getProtocol)]
         (if-not (#{"https" "http"} prot)
           {:error "URL invalid: Protocol must be either HTTP or HTTPS"}
           {:url url}))
       (catch Exception e {:error (str "URL invalid: " (.getMessage e))})))

(defn store-shorty
  [req]
  (let [{:keys [error url]} (-> req :params (get "url") validate-url)]
    (if error
      ;; TODO: handle better with flash message or something
      {:status 500 :body error}
      (let [{:keys [error id]} (insert-shorty url)]
        (if error
          ;; TODO: handle better with flash message or something
          {:status 500 :body error}
          (with-flash "Your shorty has been successfully created!" (see-other (resource-url id))))))))

;; SHOW shorty

(defn- render-shorty
  [shorty req]
  (html5 {:lang "en"}
         [:head
          [:title "Shorty - The coolest URL shortener ever!"]
          [:meta {:charset "UTF-8"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
          [:link {:href "https://unpkg.com/tailwindcss@^1.0/dist/tailwind.min.css" :rel "stylesheet"}]]
         [:body
          [:main.max-w-2xl.p-4.mx-auto
           [:h1.text-3xl.text-center.font-bold "Your Shorty"]
           [:div.my-4.bg-green-300.p-4.rounded (:flash req)]
           [:div.mt-8 (shareable-url (:urls/id shorty))]
           [:div (:urls/target_url shorty)]
           [:div (:urls/created_at shorty)]]]))

(defn show-shorty
  "Show the resource page of the shortened URL."
  [req]
  ;; TODO: validate id so far as is a string of length 8?
  (when-let [shorty (->> req :path-params :id (sql/get-by-id db :urls))]
    (ok (render-shorty shorty req))))

;; REDIRECT shorty


(defn redirect-shorty
  "This is the meat of the app: It redirects the shortened URL to the underlying one."
  [req]
  (when-let [shorty (->> req :path-params :id (sql/get-by-id db :urls))]
    (temporary-redirect (:urls/target_url shorty))))
