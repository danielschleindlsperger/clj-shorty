(ns handlers.homepage
  (:require [handlers.http-util :refer [ok]]
            [hiccup.page :refer [html5]]))

(defn- render-homepage []
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
  [req]
  (ok (render-homepage)))
