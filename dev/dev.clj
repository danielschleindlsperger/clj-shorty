(ns dev
  (:require [integrant.repl :refer [clear go halt prep init reset reset-all]]
            [clj-shorty.system :refer [config]]))

(integrant.repl/set-prep! (constantly config))
