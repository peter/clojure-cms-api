(ns app.framework.model-spec
  (:require [app.util.core :as u]
            [app.framework.model-relationships :refer [normalized-relationships]]
            [app.util.schema :refer [validate-schema]]))

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

(def spec-schema {
  :type "object"
  :definitions {
    :callback {
      :type "object"
      :properties {
        :before {:type "array"}
        :after {:type "array"}
      }
      :additionalProperties false
    }
  }
  :properties {
    :type {
      :type "string"
    }
    :schema {
      :type "object"
    }
    :callbacks {
      :type "object"
      :properties {
        :create {"$ref" "#/definitions/callback"}
        :update {"$ref" "#/definitions/callback"}
        :delete {"$ref" "#/definitions/callback"}
      }
      :additionalProperties false
    }
    :relationships {
      :type "object"
      :patternProperties {
        "^[a-z_]+$" {
          :type "object"
          :properties {
            :from_coll {:type "string"}
            :from_field {:type "string"}
            :to_field {:type "string"}
            :to_coll {:type "string"}
            :find_opts {
              :type "object"
              :properties {
                :sort {:type "object"}
                :per-page {:type "integer"}
                :fields {:type "array"}
              }
              :additionalProperties false
            }
          }
          :required [:from_coll :from_field :to_field :to_coll]
          :additionalProperties false
        }
      }
      :additionalProperties false
    }
    :indexes {
      :type "array"
      :items {
        :type "object"
        :properties {
          :fields {:type "array"}
          :unique {:type "boolean"}
          :coll {:type "string"}
        }
        :required [:fields]
      }
    }
  }
  :required [:type :schema]
  :additionalProperties false
})

(defn generate-spec [& specs]
  (let [specs (flatten specs)
        schema (apply merge-schemas (u/compact (map :schema specs)))
        callbacks (apply merge-callbacks (map normalize-callbacks (u/compact (map :callbacks specs))))
        relationships (apply u/deep-merge (u/compact (map normalized-relationships specs)))
        indexes (flatten (u/compact (map :indexes specs)))
        merged-spec (apply merge specs)
        result (u/compact (assoc merged-spec
                            :schema schema
                            :callbacks callbacks
                            :relationships relationships
                            :indexes indexes))
        errors (validate-schema spec-schema result)]
    (if errors
      (throw (Exception. (str "Model spec " (:type result) " has an invalid structure: " (pr-str errors) " spec: " (pr-str result))))
      result)))
