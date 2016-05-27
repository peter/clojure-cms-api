(ns app.models.widgets
  (:require [app.framework.model-spec :refer [generate-spec]]
            [app.models-shared.content-base-model :refer [content-base-spec]]))

(def model-type :widgets)

(def spec (generate-spec
  (content-base-spec model-type)
  {
  :type model-type
  :schema {
    :type "object"
    :properties {
      :title {:type "string"}
      :widgets_type {:type "string"}
      :body {:type "string"}
    }
    :additionalProperties false
    :required [:title]
  }
  :indexes [
    {:fields [:title] :unique true}
  ]
}))