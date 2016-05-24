(ns app.framework.model-schema)

(defn child-schema [schema attribute]
  (cond
    (= (:type schema) "object") (get-in schema [:properties attribute])
    (= (:type schema) "array") (:items schema)
    :else schema))
