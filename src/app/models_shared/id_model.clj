(ns app.models-shared.id-model
  (:require [app.framework.model-api :as model-api]))

(defn- next-id [app model-spec]
  (let [docs (model-api/find app model-spec {} {:per-page 1 :fields [:id] :sort (array-map :id -1)})]
    (or (:id (first docs)) 1)))

(defn id-callback [doc options]
  (let [id (next-id (:app options) (:model-spec options))]
    (assoc doc :id id)))

(def id-schema {
  :id {:type "integer"}
})

(def id-callbacks {
  :create {
    :before [id-callback]
  }
})
