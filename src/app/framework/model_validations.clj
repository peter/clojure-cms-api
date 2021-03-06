(ns app.framework.model-validations
  (:require [clojure.string :as str]
            [app.util.core :as u]
            [app.util.schema :refer [validate-schema]]))

(defn with-model-errors [doc errors]
  (let [new-meta (update-in (meta doc) [:errors] concat errors)]
    (with-meta doc new-meta)))

(defn model-errors [doc]
  (:errors (meta doc)))

(def model-not-updated [{:type "unchanged"}])

(def custom-property-keys #{:api_writable :versioned})

(defn without-custom-keys
  "Drop custom property keys when validating to avoid validator warnings"
  [schema]
  (assoc schema :properties
                (u/map-values #(apply dissoc % custom-property-keys) (:properties schema))))

(defn validate-model-schema [schema doc]
  (validate-schema (without-custom-keys schema) doc))
