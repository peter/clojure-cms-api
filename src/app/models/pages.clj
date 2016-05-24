(ns app.models.pages
  (:require [app.framework.model-spec :refer [merge-callbacks generate-spec]]
            [app.models-shared.id-model :refer [id-callbacks id-schema id-indexes]]
            [app.models-shared.typed-model :refer [typed-callbacks typed-schema]]
            [app.models-shared.audited-model :refer [audited-callbacks audited-schema]]
            [app.models-shared.versioned-model :refer [versioned-callbacks versioned-schema versioned-indexes]]
            [app.models-shared.published-model :refer [published-callbacks published-schema]]
            [app.models-shared.validated-model :refer [validated-callbacks]]
            [app.framework.model-validations :refer [merge-schemas with-model-errors]]))

(def type :pages)

(def spec (generate-spec {
  :type type
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
  :indexes (merge id-indexes
                  (versioned-indexes type) {
    [:title] {:unique true}
  })}))
