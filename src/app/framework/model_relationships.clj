(ns app.framework.model-relationships
  (:require [app.framework.model-api :as model-api]))

(defn- id-field [relationship]
  (keyword (str (name relationship) "_id")))

(defn- ids-field [relationship]
  (keyword (str (name relationship) "_ids")))

(defn relationship-options [relationship model-spec]
  (let [options (get-in model-spec [:relationships relationship])
        from-coll (get options :from_coll (model-api/coll model-spec))
        attribute-keys (set (keys (get-in model-spec [:schema :properties])))
        default-from-field (some attribute-keys [(id-field relationship) (ids-field relationship)])
        from-field (get options :from_field default-from-field)
        to-coll (get options :to_coll relationship)
        to-field (get options :to_field :id)]
    {
      :from_coll from-coll
      :from_field from-field
      :to_field to-field
      :to_coll to-coll
    }))

(defn normalized-relationships [model-spec]
  (not-empty (reduce (fn [m [k v]] (assoc m k (relationship-options k model-spec))) {} (get model-spec :relationships {}))))

; TODO: Find related docs.
; TODO: sort/limit/fields options for relationships?
(defn find-relationship [model-spec doc relationship])

; TODO: validate ids of has-one and has-many if from_coll = (coll model-spec)
(defn validate-references-callback [doc options])
