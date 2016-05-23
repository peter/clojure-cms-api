(ns app.models-shared.model-id
  (:require [app.framework.model-api :as model-api]))

(defn next-id [app model-spec]
  (let [docs (model-api/find app model-spec {} {:per-page 1 :fields [:id] :sort (array-map :id -1)})]
    (or (:id (first docs)) 1)))
