(ns app.framework.crud-api
  (:require [app.framework.model-api :as model-api]
            [app.framework.json-api :as json-api]
            [app.framework.model-attributes :refer [api-writable-attributes]]
            [app.framework.crud-api-audit :refer [updated-by created-by save-changelog]]
            [app.framework.crud-api-types :refer [coerce-attribute-types]]
            [app.logger :as logger]))

(defn- write-attributes [model-spec request]
  (-> (json-api/attributes model-spec request)
      (api-writable-attributes (:schema model-spec))
      (coerce-attribute-types (:schema model-spec))))

(defn- create-attributes [model-spec request]
  (merge (write-attributes model-spec request)
         (created-by request)))

(defn- update-attributes [model-spec request]
  (merge (write-attributes model-spec request)
         (model-api/id-query (json-api/id request))
         (updated-by request)))

(defprotocol CrudApi
  (list [api app request])
  (get [api app request])
  (create [api app request])
  (update [api app request])
  (delete [api app request]))

(defrecord BaseApi [model-spec]
  CrudApi

  (list [this app request]
    (let [opts (json-api/list-opts request)
          docs (model-api/find app model-spec {} opts)]
      (json-api/response docs)))

  (get [this app request]
    (let [doc (model-api/find-one app model-spec (json-api/id request))]
      (if doc
        (json-api/response doc)
        (json-api/missing-response))))

  (create [this app request]
    (let [attributes (create-attributes model-spec request)
          doc (model-api/create app model-spec attributes)]
      (logger/debug app "crud-api create " (:type model-spec) " doc:" doc " meta:" (meta doc))
      (save-changelog (:database app) request model-spec :create doc)
      (json-api/response doc)))

  (update [this app request]
    (let [existing-doc (model-api/find-one app model-spec (json-api/id request))]
      (if existing-doc
        (let [attributes (update-attributes model-spec request)
              doc (model-api/update app model-spec attributes)]
          (logger/debug app "crud-api update " (:type model-spec) " doc:" doc " meta:" (meta doc))
          (save-changelog (:database app) request model-spec :update doc)
          (json-api/response doc))
        (json-api/missing-response))))

  (delete [this app request]
    (let [existing-doc (model-api/find-one app model-spec (json-api/id request))]
      (if existing-doc
        (let [doc (model-api/delete app model-spec existing-doc)]
          (logger/debug app "crud-api delete " (:type model-spec) " doc:" doc " meta:" (meta doc))
          (save-changelog (:database app) request model-spec :delete doc)
          (json-api/response {}))
        (json-api/missing-response)))))

(defn new-api [& args]
  (map->BaseApi (apply hash-map args)))
