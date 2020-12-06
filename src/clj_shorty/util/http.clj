(ns clj-shorty.util.http)

(defn with-flash
  "Add a flash message to the next request. Useful for redirects and showing a message.
  See: https://github.com/ring-clojure/ring/blob/master/ring-core/src/ring/middleware/flash.clj"
  [res type message]
  (assoc res :flash {:type type :message message}))

(defn with-session
  "Add a value v to key k in the session.
  k can also be a vector to set a nested value.
  Needs req to be able to use the old session as a base."
  [res req k v]
  (let [old-session (:session req)
        new-session (assoc-in old-session k v)]
    (assoc res :session new-session)))

(defn ok
  "200 OK (Success)
  OK"
  ([] (ok nil))
  ([body]
   {:status 200
    :headers {}
    :body body}))

(defn html
  "200 OK (Success) with HTML body"
  ([body]
   {:status 200
    :headers {"Content-Type" "text/html"}
    :body body}))

(defn moved-permanently
  "301 Moved Permanently (Redirection)
  This and all future requests should be directed to the given URI."
  ([url]
   {:status 301
    :headers {"Location" url}
    :body ""}))

(defn moved-temporarily
  "302 Found - Moved Temporarily (Redirection)
  This and all future requests should be directed to the given URI."
  ([url]
   {:status 302 :headers {"Location" url} :body ""}))

(defn see-other
  "303 See Other (Redirection)
  Redirect to the actual resource."
  ([url]
   {:status 303
    :headers {"Location" url}
    :body ""}))

(defn temporary-redirect
  "307 Temporary Redirect
  The request should be repeated with another URI."
  ([url]
   {:status 307
    :headers {"Location" url}
    :body ""}))
