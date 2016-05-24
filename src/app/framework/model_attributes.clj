(ns app.framework.model-attributes
  (:require [app.framework.model-schema :refer [schema-attributes]]))

(defn api-writable? [attribute-schema]
  (get attribute-schema :api_writable true))

(defn api-writable-attribute-keys [schema]
  (let [schema-attrs (schema-attributes schema)]
    (filter #(api-writable? (% schema-attrs)) (keys schema-attrs))))

(defn api-writable-attributes [attributes schema]
  (select-keys attributes (api-writable-attribute-keys schema)))
