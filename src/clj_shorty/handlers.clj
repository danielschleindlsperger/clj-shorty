(ns clj-shorty.handlers
  (:require [hiccup.page :refer [html5]]
            [clojure.string :refer [join]]
            [clojure.java.io :refer [as-url]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as result-set]
            [clj-shorty.database :refer [db]]
            [clj-shorty.config :refer [cfg]]
            [clj-shorty.assets :refer [assets]]
            [clj-shorty.util.http :refer [with-flash with-session html see-other moved-temporarily temporary-redirect]])
  (:import org.postgresql.util.PSQLException))

(defn- as-kebab-maps [rs opts]
  (let [kebab #(clojure.string/replace % #"_" "-")]
    (result-set/as-unqualified-modified-maps rs (assoc opts :qualifier-fn kebab :label-fn kebab))))

(defn- flash
  "Render a flash message (usually from the session).
  Takes a map with the key :type which can either be :success or :error and a key :message with a message text of type string."
  [f]
  (when f
    (let [base-classes "my-12 p-4 rounded"
          success-classes "bg-green-300"
          error-classes "bg-red-500"
          class [base-classes (if (= :success (:type f)) success-classes error-classes)]]
      [:div {:class class} (:message f)])))

(defn- copy-to-clipboard
  [url]
  [:button.ml-6.px-3.py-1.flex.items-center.border.border-gray-200.rounded {:data-clipboard url}
   [:svg {:width "1em" :height "1em" :viewBox "0 0 16 16" :fill "currentColor" :xmlns "http://www.w3.org/2000/svg"}
    [:path {:fill-rule "evenodd" :d "M4 1.5H3a2 2 0 0 0-2 2V14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V3.5a2 2 0 0 0-2-2h-1v1h1a1 1 0 0 1 1 1V14a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1V3.5a1 1 0 0 1 1-1h1v-1z"}]
    [:path {:fill-rule "evenodd" :d "M9.5 1h-3a.5.5 0 0 0-.5.5v1a.5.5 0 0 0 .5.5h3a.5.5 0 0 0 .5-.5v-1a.5.5 0 0 0-.5-.5zm-3-1A1.5 1.5 0 0 0 5 1.5v1A1.5 1.5 0 0 0 6.5 4h3A1.5 1.5 0 0 0 11 2.5v-1A1.5 1.5 0 0 0 9.5 0h-3z"}]]
   [:span.text-sm.ml-3 "Copy"]])

(defn- your-shorties
  [shorties]
  (when (peek shorties)
    [:div.mt-12.px-4.border.rounded
     (for [shorty (sort-by :created-at #(compare %2 %1) shorties)]
       (let [{:keys [id target-url]} shorty
             base-url (:base-url cfg)]
         [:div.flex.justify-between.items-baseline.p-4.border-t.first:border-t-0
          [:div.mr-4.truncate {:title target-url} target-url]
          [:div.ml-auto.font-mono.font-bold
           [:span base-url]
           [:span (str "/" id)]]
          (copy-to-clipboard (str base-url "/" id))]))]))

(defn render-homepage
  [req]
  (html5 {:lang "en"}
         [:head
          [:title "Shorty - The coolest URL shortener ever!"]
          [:meta {:charset "UTF-8"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]]
         [:meta {:name "description" :content "Shorty makes your life easier by replacing your long URLs with short, readable and memorizable ones."}]
         [:script {:async true :defer true :data-domain "shorty.lchthbr.xyz" :src "https://plausible.io/js/plausible.js"}]
         [:script (:js assets)]
         [:style (:css assets)]
         [:body
          [:main.mt-8.max-w-2xl.p-4.mx-auto
           [:h1.text-5xl.text-center.font-bold
            [:span.fat-underline.relative.inline-block.pb-8 "Hello Shorty"]]
           (flash (:flash req))
           [:h2.mt-12.text-3xl.font-bold "What's a shorty?"]
           [:p.mt-4 "Shorty is the simplest URL shortener imaginable. Paste in your long URL and we'll give you a short one! The shortened URL, called shorty, can be shared easily and is also fast and reliable to transcribe."]
           [:form.mt-12.flex {:method "POST" :action "/shorties"}
            (anti-forgery-field)
            [:label.flex-grow.flex
             [:span.sr-only "URL"]
             [:input.flex-grow.px-3.py-1.bg-gray-200.placeholder-gray-600.rounded-l
              {:type "text" :name "url" :placeholder "https://example.com"}]]
            [:button.px-4.py-2.bg-indigo.rounded-r.font-bold.text-white	{:type "submit"} "Shorten!"]]
           (your-shorties (-> req :session :shorties))]]))

(defn homepage
  [req]
  (html (render-homepage req)))

;; 404 page
(defn not-found-page
  [_]
  (html (html5 {:lang "en"}
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

(defn- return-with-error
  "Redirect to / with an error flash containing the supplied message."
  [message]
  (-> (moved-temporarily "/")
      (with-flash :error message)))

(defn store-shorty
  [req]
  (let [{:keys [error url]} (-> req :params (get "url") validate-url)]
    (if error
      (return-with-error "URL is not in a valid format. Please try again.")
      (let [{:keys [error id]} (insert-shorty url)]
        (if error
          (return-with-error "Error while shortening the URL. Please try again.")
          (let [shorty (sql/get-by-id db :urls id {:builder-fn as-kebab-maps})
                session-shorties (-> req :session :shorties vec)
                new-session-shorties (conj session-shorties (update shorty :created-at str))]
            (-> (see-other "/")
                (with-flash :success "Your shorty has been created successfully!")
                (with-session req [:shorties] new-session-shorties))))))))

;; REDIRECT shorty

(defn- get-shorty-by-id
  "Retrieve a shorty from the database using the supplied id (PK)."
  [id]
  (sql/get-by-id db :urls id {:builder-fn as-kebab-maps}))

(defn redirect-shorty
  "This is the meat of the app: It redirects the shortened URL to the underlying one."
  [req]
  (when-let [shorty (->> req :path-params :id get-shorty-by-id)]
    (temporary-redirect (:target-url shorty))))
