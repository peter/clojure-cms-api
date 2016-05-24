(ns app.framework.model-api
  (:refer-clojure :exclude [find update delete count])
  (:require [app.components.db :as db]
            [app.util.core :as u]
            [app.framework.model-schema :refer [schema-attributes]]
            [app.framework.model-validations :refer [with-model-errors model-not-updated]]
            [app.framework.model-changes :refer [model-changed?]]
            [app.framework.model-callbacks :refer [with-callbacks]]))

(defn coll [spec]
  (:type spec))

(defn id-attribute [model-spec]
  (let [attribute-keys (keys (schema-attributes (:schema model-spec)))]
    (or (some #{:id} attribute-keys)
        :_id)))

(defn id-value [id]
  (or (u/safe-parse-int id) id))

(defn id-query [model-spec id]
  (hash-map (id-attribute model-spec)
            (id-value id)))

(defn valid-id? [model-spec id]
  (let [attribute (id-attribute model-spec)]
    (cond
      (= attribute :id) (u/valid-int? id)
      (= attribute :_id) (db/valid-object-id? id)
      :else true)))

(defn find
  ([app model-spec query opts]
    (db/find (:database app) (coll model-spec) query opts))
  ([app model-spec query]
    (find app model-spec query {})))

(defn find-one [app model-spec id]
  (if (valid-id? model-spec id)
    (db/find-one (:database app) (coll model-spec) (id-query model-spec id))
    nil))

(defn count [app model-spec query]
  (db/count (:database app) (coll model-spec) query))

(defn- exec-create-without-callbacks [app model-spec doc]
  (let [result (db/create (:database app) (coll model-spec) doc)]
    (with-meta (:doc result)
               (merge (meta doc) {:result result}))))

(def exec-create (with-callbacks exec-create-without-callbacks :create))

(defn create [app model-spec doc]
  (let [create-doc (u/compact doc)]
    (exec-create app model-spec create-doc)))

(defn- exec-update-without-callbacks [app model-spec doc]
  (let [id ((id-attribute model-spec) doc)
        result (db/update (:database app) (coll model-spec) (id-query model-spec id) doc)]
      (with-meta (:doc result)
                 (merge (meta doc) {:result result}))))

(def exec-update (with-callbacks exec-update-without-callbacks :update))

(defn update [app model-spec doc]
  (let [id ((id-attribute model-spec) doc)
        existing-doc (find-one app model-spec id)
        merged-doc (with-meta (u/compact (merge existing-doc doc)) {:existing-doc existing-doc})]
    (if (model-changed? model-spec merged-doc)
      (exec-update app model-spec merged-doc)
      (with-model-errors doc model-not-updated))))

(defn- delete-without-callbacks [app model-spec doc]
  (let [id ((id-attribute model-spec) doc)
        result (db/delete (:database app) (coll model-spec) (id-query model-spec id))]
    (with-meta doc {:result result})))

(def delete (with-callbacks delete-without-callbacks :delete))
