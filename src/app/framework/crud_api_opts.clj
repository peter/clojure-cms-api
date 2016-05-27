(ns app.framework.crud-api-opts
  (:require [app.util.core :as u]))

; TODO: query parameter validation - use json schema or custom query validation
(defn- pagination [request]
  (->> (select-keys (get-in request [:params]) [:page :per-page])
       (u/map-values u/safe-parse-int)
       (u/compact)))

(defn list-opts [request]
  (pagination request))

; TODO: query parameter validation - use json schema or custom query validation
; version is a string - it can be "published" or an id
(defn get-opts [request]
  (select-keys (:params request) [:relationships :version]))
