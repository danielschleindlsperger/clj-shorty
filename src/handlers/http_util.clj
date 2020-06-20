(ns handlers.http-util)

(defn ok
  "200 OK (Success)
  OK"
  ([] (ok nil))
  ([body]
   {:status 200
    :headers {}
    :body body}))

(defn moved-permanently
  "301 Moved Permanently (Redirection)
  This and all future requests should be directed to the given URI."
  ([url]
   {:status 301
    :headers {"Location" url}
    :body ""}))

(defn see-other
  "303 See Other ()Redirection)
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
