(ns app.models.pages
  (:refer-clojure :exclude [type])
  (:require [app.framework.model-spec :refer [generate-spec]]
            [app.models-shared.id-model :refer [id-spec]]
            [app.models-shared.typed-model :refer [typed-spec]]
            [app.models-shared.audited-model :refer [audited-spec]]
            [app.models-shared.versioned-model :refer [versioned-spec]]
            [app.models-shared.published-model :refer [published-spec]]
            [app.models-shared.validated-model :refer [validated-spec]]))

(def type :pages)

(def spec (generate-spec
  (id-spec)
  (typed-spec)
  (audited-spec)
  (versioned-spec :type type)
  (published-spec)
  (validated-spec)
  {
  :type type
  :schema {
    :type "object"
    :properties {
      :title {:type "string"}
      :body {:type "string"}
    }
    :additionalProperties false
    :required [:title]
  }
  :indexes [
    {:fields [:title] :unique true}
  ]
}))
