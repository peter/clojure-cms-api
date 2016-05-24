(ns app.framework.model-validations
  (:require [clojure.string :as str]
            [app.util.core :as u]
            [scjsv.core :as v]))

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

(defn validate-schema [doc schema]
  ((v/validator (without-custom-keys schema)) doc))

; TODO: use "allOf" to combine JSON schemas instead?
(defn merge-schemas [& schemas]
  (let [properties (apply merge (map :properties schemas))
        required (apply concat (map :required schemas))]
    (assoc (apply merge schemas) :properties properties :required required)))
