(ns app.models.pages
  (:require [app.framework.model-spec :refer [merge-callbacks generate-spec]]
            [app.models-shared.id-model :refer [id-callbacks id-schema]]
            [app.models-shared.typed-model :refer [typed-callbacks typed-schema]]
            [app.models-shared.audited-model :refer [audited-callbacks audited-schema]]
            [app.models-shared.versioned-model :refer [versioned-callbacks versioned-schema]]
            [app.models-shared.published-model :refer [published-callbacks published-schema]]
            [app.models-shared.validated-model :refer [validated-callbacks]]
            [app.framework.model-validations :refer [merge-schemas with-model-errors]]))

(def spec (generate-spec {
  :type :pages
  :callbacks (merge-callbacks id-callbacks
                              typed-callbacks
                              audited-callbacks
                              versioned-callbacks
                              published-callbacks
                              validated-callbacks)
  :schema (merge-schemas id-schema
                         typed-schema
                         audited-schema
                         versioned-schema
                         published-schema {
    :type "object"
    :properties {
      :title {:type "string"}
      :body {:type "string"}
    }
    :additionalProperties false
    :required [:title]
  })
  :indexes {
    [:title] {:unique true}
  }}))
