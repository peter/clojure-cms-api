(ns app.framework.crud-api-attributes
  (:require [app.framework.model-support :as model-support]
            [app.framework.json-api :as json-api]
            [app.framework.model-attributes :refer [api-writable-attributes]]
            [app.framework.crud-api-audit :refer [updated-by created-by save-changelog]]
            [app.framework.crud-api-types :refer [coerce-attribute-types]]))

(defn- write-attributes [model-spec attributes]
  (-> attributes
      (api-writable-attributes (:schema model-spec))
      (coerce-attribute-types (:schema model-spec))))

(defn create-attributes [model-spec request attributes]
  (merge (write-attributes model-spec attributes)
         (created-by request)))

(defn update-attributes [model-spec request attributes]
  (merge (write-attributes model-spec attributes)
         (model-support/id-query model-spec (json-api/id request))
         (updated-by request)))

(defn invalid-attributes [model-spec request]
  (not-empty (clojure.set/difference (set (keys (json-api/attributes model-spec request)))
                                     (set (keys (get-in model-spec [:schema :properties]))))))
