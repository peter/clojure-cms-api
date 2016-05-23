(ns app.models-shared.audited-model
  (:require [app.util.date :as d]))

(defn audit-create-callback [doc options]
  (assoc doc :created_at (d/now)))

(defn audit-update-callback [doc options]
  (assoc doc :updated_at (d/now)))

(def audited-schema {
  :created_at {:type "datetime" :api_writable false :versioned false}
  :created_by {:type "string" :api_writable false :versioned false}
  :updated_at {:type "datetime" :api_writable false :optional true :versioned false}
  :updated_by {:type "string" :api_writable false :optional true :versioned false}
})

(def audited-callbacks {
  :create {
    :before [audit-create-callback]
  }
  :update {
    :before [audit-update-callback]
  }
})
