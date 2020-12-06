(ns clj-shorty.system
  (:require [integrant.core :as ig]))

(def config
  {:clj-shorty/server {:config (ig/ref :clj-shorty/config)
                       :routes (ig/ref :clj-shorty/routes)}
   :clj-shorty/config {}
   :clj-shorty/routes {:config (ig/ref :clj-shorty/config)
                       :ds (ig/ref :clj-shorty/database)
                       :assets (ig/ref :clj-shorty/assets)}
   :clj-shorty/database {:config (ig/ref :clj-shorty/config)}
   :clj-shorty/assets {}})

(ig/load-namespaces config)