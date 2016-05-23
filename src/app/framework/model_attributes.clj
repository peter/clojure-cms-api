(ns app.framework.model-attributes)

(defn api-writable? [attribute-schema]
  (get attribute-schema :api_writable true))

(defn api-writable-attribute-keys [schema]
  (filter #(api-writable? (% schema)) (keys schema)))

(defn api-writable-attributes [attributes schema]
  (select-keys attributes (api-writable-attribute-keys schema)))
