(ns handlers
  (:require [hiccup.page :refer [html5]]
            [clojure.string :refer [join]]
            [clojure.java.io :refer [as-url]]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as result-set]
            [database :refer [db]]
            [config :refer [cfg]]
            [util.http :refer [with-flash with-session ok see-other temporary-redirect]])
  (:import org.postgresql.util.PSQLException))

(defn as-kebab-maps [rs opts]
  (let [kebab #(clojure.string/replace % #"_" "-")]
    (result-set/as-unqualified-modified-maps rs (assoc opts :qualifier-fn kebab :label-fn kebab))))

(defn flash
  [f]
  (when f [:div.my-8.bg-green-300.p-4.rounded f]))

(defn your-shorties
  [shorties]
  (when (peek shorties)
    [:div.mt-8
     [:h2.my-4.text-3xl.font-bold "Your shorties"]
     (for [shorty (sort-by :created-at #(compare %2 %1) shorties)]
       (let [{:keys [id target-url]} shorty]
         [:div.flex.justify-between
          [:div.truncate {:title target-url} target-url]
          [:div.ml-4.font-mono.font-bold
           [:span (:base-url cfg)]
           [:span (str "/" id)]]]))]))

(defn render-homepage
  [req]
  (html5 {:lang "en"}
         [:head
          [:title "Shorty - The coolest URL shortener ever!"]
          [:meta {:charset "UTF-8"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
          [:link {:href "https://unpkg.com/tailwindcss@^1.0/dist/tailwind.min.css" :rel "stylesheet"}]]
         [:body
          [:main.mt-8.max-w-2xl.p-4.mx-auto
           [:h1.text-5xl.text-center.font-bold "Hello Shorty"]
           (flash (:flash req))
           [:h2.mt-8.text-3xl.font-bold "What's that?"]
           [:p.mt-4 "Shorty is the simplest URL shortener imaginable. Paste in your long URL and we'll give you a short one! The short URL can be shared easily and is also fast and reliable to transcribe."]
           [:form.mt-12.flex.max-w-xl {:method "POST" :action "/shorties"}
            [:input.flex-grow.px-3.py-1.bg-gray-200.placeholder-gray-600.rounded-l
             {:type "text" :name "url" :value "https://example.com" :placeholder "https://example.com"}]

            [:button.px-3.py-1.bg-green-300.rounded-r	{:type "submit"} "Shorten!"]]
           (your-shorties (-> req :session :shorties))]]))

(defn homepage
  [req]
  (ok (render-homepage req)))

;; 404 page
(defn not-found-page
  [_]
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
               [:p "Check if you correctly entered the URL and re-enter."]]])))

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
          (let [shorty (sql/get-by-id db :urls id {:builder-fn as-kebab-maps})
                session-shorties (-> req :session :shorties vec)
                new-session-shorties (conj session-shorties (update shorty :created-at str))]
            (-> (see-other "/")
                (with-flash "Your shorty has been created successfully!")
                (with-session req [:shorties] new-session-shorties))))))))

;; REDIRECT shorty

(defn redirect-shorty
  "This is the meat of the app: It redirects the shortened URL to the underlying one."
  [req]
  (when-let [shorty (->> req :path-params :id (sql/get-by-id db :urls {:builder-fn as-kebab-maps}))]
    (temporary-redirect (:urls/target_url shorty))))
