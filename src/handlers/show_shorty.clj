(ns handlers.show-shorty
  (:require [hiccup.page :refer [html5]]
            [next.jdbc.sql :as sql]
            [database :refer [db]]
            [config :refer [cfg]]
            [handlers.http-util :refer [ok]]))

(defn- shareable-url
  [id]
  (str (:base-url cfg) "/" id))

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
    (println shorty)
    (ok (render-shorty shorty))))
