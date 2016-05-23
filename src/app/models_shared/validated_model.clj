(ns app.models-shared.validated-model
  (:require [app.framework.model-validations :refer [validate-schema with-model-errors]]))

(defn validate-schema-callback [doc options]
  (if-let [errors (validate-schema doc (:schema options))]
    (with-model-errors doc errors)
    doc))

(def validated-schema {
  :id {:type "integer"}
  :type {:type "string"}
  :created_at {:type "datetime" :api_writable false :versioned false}
  :created_by {:type "string" :api_writable false :versioned false}
  :updated_at {:type "datetime" :api_writable false :optional true :versioned false}
  :updated_by {:type "string" :api_writable false :optional true :versioned false}
})

; NOTE: we usually want validation to happen last of the before callbacks. The create/update
;       callbacks execute after the save callbacks.
(def validated-callbacks {
  :create {
    :before [validate-schema-callback]
  }
  :update {
    :before [validate-schema-callback]
  }
})
