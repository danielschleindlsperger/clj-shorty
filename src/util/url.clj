(ns util.url
  "Utilities related to the generation of URLs."
  (:require [config :refer [cfg]]))

(defn resource-url
  "Generate the URL to the resource page of the shorty using the supplied id."
  [id]
  (str (:base-url cfg) "/shorties/" id))

(defn shareable-url
  "Generate the URL to the shareable version of the shorty. The one that resolves to the target URL."
  [id]
  (str (:base-url cfg) "/" id))
