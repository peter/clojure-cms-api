(ns app.models.pages
  (:refer-clojure :exclude [type])
  (:require [app.framework.model-spec :refer [generate-spec]]
            [app.models-shared.content-base-model :refer [content-base-spec]]))

(def type :pages)

(def spec (generate-spec
  (content-base-spec type)
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
