(ns app.models.pages
  (:require [app.framework.model-spec :refer [merge-callbacks generate-spec]]
            [app.models-shared.content-model :refer [content-callbacks content-schema]]
            [app.models-shared.versioned-model :refer [versioned-callbacks versioned-schema]]
            [app.models-shared.published-model :refer [published-callbacks published-schema]]
            [app.framework.model-validations :refer [with-model-errors]]))

(def spec (generate-spec {
  :type :pages
  :callbacks (merge-callbacks content-callbacks
                              versioned-callbacks
                              published-callbacks {})
  :schema (merge content-schema
                 versioned-schema
                 published-schema {
    :title {:type "string"}
    :body {:type "string" :optional true}
  })
  :indexes {
    [:title] {:unique true}
  }}))
