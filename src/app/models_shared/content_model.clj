(ns app.models-shared.content-model
  (:require [app.models-shared.model-id :refer [next-id]]
            [app.util.date :as d]
            [app.framework.model-validations :refer [validate-schema]]
            [app.framework.model-validations :refer [with-model-errors]]))

; TODO: Refactor this module into audited-model, id-model, typed-model

(defn id-callback [doc options]
  (let [id (next-id (:app options) (:model-spec options))]
    (assoc doc :id id)))

(defn type-callback [doc options]
  (let [type (get-in options [:model-spec :type])]
    (assoc doc :type (name type))))

(defn audit-create-callback [doc options]
  (assoc doc :created_at (d/now)))

(defn audit-update-callback [doc options]
  (assoc doc :updated_at (d/now)))

(defn validate-schema-callback [doc options]
  (if-let [errors (validate-schema doc (:schema options))]
    (with-model-errors doc errors)
    doc))

(def content-schema {
  :id {:type "integer"}
  :type {:type "string"}
  :created_at {:type "datetime" :api_writable false :versioned false}
  :created_by {:type "string" :api_writable false :versioned false}
  :updated_at {:type "datetime" :api_writable false :optional true :versioned false}
  :updated_by {:type "string" :api_writable false :optional true :versioned false}})

(def content-callbacks {
    :save {
      :before [type-callback]
    }
    :create {
      :before [id-callback audit-create-callback validate-schema-callback]
    }
    :update {
      :before [audit-update-callback validate-schema-callback]
    }})
