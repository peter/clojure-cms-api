(ns app.models.sections
  (:require [app.framework.model-spec :refer [generate-spec]]
            [app.framework.model-includes.content-base-model :refer [content-base-spec]]))

(def model-type :sections)

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
      :public {:type "boolean"}
      :sites {
        :type "array"
        :items {
          :enum ["se" "no" "dk" "fi"]
        }
      }
      :pages_ids {
        :type "array"
        :items {
          :type "integer"
        }
      }
      :legacy {:type "object"}
    }
    :additionalProperties false
    :required [:title :slug]
  }
  :relationships {
    :pages {}
  }
}))
