(ns user)

(set! *warn-on-reflection* true)

(println "Loaded namespace `user`, welcome to clj-shorty!")
(println "Run (dev) and (go) to start the application.")
(println "Run (integrant.repl/reset) to reset after code changes")

(defn dev []
  (require 'dev)
  (in-ns 'dev))