(ns app.models.pages
  (:require [app.framework.model-spec :refer [generate-spec]]
            [app.framework.model-includes.content-base-model :refer [content-base-spec]]))

(def model-type :pages)

(def spec (generate-spec
  (content-base-spec model-type)
  {
  :type model-type
  :schema {
    :type "object"
    :properties {
      :title {:type "string"}
      :description {:type "string"}
      :slug {:type "string"}
      :widgets_ids {
        :type "array"
        :items {
          :type "integer"
        }
      }
      :legacy {:type "object"}
    }
    :additionalProperties false
    ;:required [:title]
  }
  :relationships {
    :widgets {}
  }
  :indexes [
    ;{:fields [:title] :unique true}
  ]
}))
