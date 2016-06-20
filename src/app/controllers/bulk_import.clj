(ns app.controllers.bulk-import
  (:require [app.framework.model-reflect :as model-reflect]
            [app.framework.db-api :as db]
            [app.util.core :as u]
            [app.framework.model-api :as model-api]
            [app.framework.model-support :refer [coll]]
            [app.framework.model-versions :refer [versioned-coll]]
            [app.framework.crud-api-attributes :refer [create-attributes]]
            [app.framework.model-validations :refer [model-errors]]))

(defn- clear [app model-spec]
  (db/delete (:database app) (coll model-spec) {})
  (db/delete (:database app) (versioned-coll model-spec) {}))

(defn- insert-one [app model-spec request doc]
  (let [attributes (create-attributes model-spec request doc)]
    (model-api/create app model-spec attributes)))

(defn- insert [app model-spec request data]
  (map (partial insert-one app model-spec request) data))

(defn- one-result [doc]
  (u/compact {:errors (model-errors doc) :doc doc}))

(defn create [app request]
  (let [model-name (get-in request [:params :model])
        model-spec (model-reflect/model-spec model-name)
        data (get-in request [:params :data])
        _ (clear app model-spec)
        result (map one-result (insert app model-spec request data))]
     {:body {:result result} :status 200}))
