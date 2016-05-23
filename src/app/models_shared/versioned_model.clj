(ns app.models-shared.versioned-model
  (:require [app.framework.model-api :as model-api]
            [app.framework.model-changes :refer [model-changes]]
            [app.util.date :as d]
            [app.components.db :as db]))

(defn versioned-attribute? [attribute-schema]
  (get attribute-schema :versioned true))

(defn versioned-attributes [schema]
  (filter #(versioned-attribute? (% schema)) (keys (:properties schema))))

(defn increment-version? [model-spec doc]
  (not-empty (select-keys (model-changes model-spec doc)
                          (versioned-attributes (:schema model-spec)))))

(defn latest-version [model-spec doc]
  (let [old-version (get-in (meta doc) [:existing-doc :version])]
    (if old-version
      (if (increment-version? model-spec doc) (inc old-version) old-version)
      1)))

(defn versioned-doc [model-spec doc]
  (let [model-attributes (select-keys doc (versioned-attributes (:schema model-spec)))
        version-attributes {:created_at (d/now)}]
    (merge model-attributes version-attributes)))

(defn versioned-coll [model-spec]
  (str (name (model-api/coll model-spec)) "_versions"))

(defn set-version-callback [doc options]
  (assoc doc :version (latest-version (:model-spec options) doc)))

(defn create-version-callback [doc options]
  (if (increment-version? (:model-spec options) doc)
    (let [result (db/create (:database options) (versioned-coll doc) (versioned-doc (:model-spec options) doc))]))
  doc)

(def versioned-schema {
  :type "object"
  :properties {
    :version {:type "integer" :minimum 1 :api_writable false}
  }
  :required [:version]
})

(def versioned-callbacks {
  :save {
    :before [set-version-callback]
    :after [create-version-callback]
  }
})
