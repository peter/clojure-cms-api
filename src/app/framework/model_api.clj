(ns app.framework.model-api
  (:refer-clojure :exclude [find update delete count])
  (:require [app.components.db :as db]
            [app.util.core :as u]
            [app.framework.model-validations :refer [with-model-errors model-not-updated]]
            [app.framework.model-changes :refer [model-changed?]]
            [app.framework.model-callbacks :refer [with-callbacks]]))

(defn coll [spec]
  (:type spec))

(def id-attribute :_id)

(defn id-query [id]
  (hash-map id-attribute id))

(defn doc-with-id
  ([id] (doc-with-id id {}))
  ([id doc] (merge doc (id-query id))))

(defn find
  ([app model-spec query opts]
    (db/find (:database app) (coll model-spec) query opts))
  ([app model-spec query]
    (find app model-spec query {})))

(defn find-one [app model-spec id]
  (if (db/valid-object-id? id)
    (db/find-one (:database app) (coll model-spec) (id-query id))
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
  (let [result (db/update (:database app) (coll model-spec) (id-query (id-attribute doc)) doc)]
      (with-meta (:doc result)
                 (merge (meta doc) {:result result}))))

(def exec-update (with-callbacks exec-update-without-callbacks :update))

(defn update [app model-spec doc]
  (let [existing-doc (find-one app model-spec (id-attribute doc))
        merged-doc (with-meta (u/compact (merge existing-doc doc)) {:existing-doc existing-doc})]
    (if (model-changed? model-spec merged-doc)
      (exec-update app model-spec merged-doc)
      (with-model-errors doc model-not-updated))))

(defn- delete-without-callbacks [app model-spec doc]
  (let [result (db/delete (:database app) (coll model-spec) (id-query (id-attribute doc)))]
    (with-meta doc {:result result})))

(def delete (with-callbacks delete-without-callbacks :delete))
