(ns app.framework.model-attributes)

(defn attribute-type [schema attribute]
  (cond
    (= (:format schema) "date-time") "date"
    :else (:type schema)))

(defn api-writable? [attribute-schema]
  (get attribute-schema :api_writable true))

(defn api-writable-attribute-keys [schema]
  (let [properties (:properties schema)]
    (filter #(api-writable? (% properties)) (keys properties))))

(defn api-writable-attributes [attributes schema]
  (select-keys attributes (api-writable-attribute-keys schema)))
