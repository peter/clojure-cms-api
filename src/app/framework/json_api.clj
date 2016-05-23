(ns app.framework.json-api
  (:require [app.util.core :as u]
            [app.framework.model-validations :refer [model-errors model-not-updated]]))

; Inspired by http://jsonapi.org

(defn id [request]
  (get-in request [:params :id]))

; TODO: query parameter validation - use json schema?
(defn pagination [request]
  (->> (select-keys (get-in request [:params]) [:page :per-page])
       (u/map-values u/safe-parse-int)
       (u/compact)))

(defn list-opts [request]
  (pagination request))

(defn attributes [model-spec request]
  (get-in request [:params (:type model-spec)]))

(defn error-response [doc]
  {:body {:errors (model-errors doc)} :status 422})

(defn data-response
  ([data status] {:body {:data data} :status status})
  ([data] (data-response data 200)))

(defn no-update-response [data]
  {:status 204})

(defn missing-response []
  {:body {} :status 404})

(defn response [doc]
  (cond
    (= (model-errors doc) model-not-updated) (no-update-response doc)
    (model-errors doc) (error-response doc)
    :else (data-response doc)))
