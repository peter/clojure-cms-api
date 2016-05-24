(ns app.framework.model-spec
  (:require [app.util.core :as u]))

(defn merge-callbacks [& callbacks]
  (apply u/deep-merge-with concat callbacks))

; TODO: can we use something like "allOf" to combine JSON schemas instead?
(defn merge-schemas [& schemas]
  (let [properties (apply merge (map :properties schemas))
        required (apply concat (map :required schemas))]
    (assoc (apply merge schemas)
      :properties properties
      :required required)))

(def empty-callback {:before [] :after []})

(defn save-callbacks [callbacks]
  {:update (:save callbacks) :create (:save callbacks)})

(defn normalize-callbacks [callbacks]
  (if (:save callbacks)
    (merge-callbacks (save-callbacks callbacks) (dissoc callbacks :save))
    callbacks))

(defn generate-spec [& specs]
  (let [schema (apply merge-schemas (u/compact (map :schema specs)))
        callbacks (apply merge-callbacks (map normalize-callbacks (u/compact (map :callbacks specs))))
        indexes (flatten (u/compact (map :indexes specs)))
        merged-spec (apply merge specs)]
    (assoc merged-spec
      :schema schema
      :callbacks callbacks
      :indexes indexes)))
