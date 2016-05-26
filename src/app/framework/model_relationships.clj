(ns app.framework.model-relationships
  (:require [app.framework.model-api :as model-api]
            [app.components.db :as db]))

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
      :to_coll to-coll
      :to_field to-field
    }))

(defn normalized-relationships [model-spec]
  (not-empty (reduce (fn [m [k v]] (assoc m k (relationship-options k model-spec))) {} (get model-spec :relationships {}))))

; Example: (i.e. ActiveRecord has_and_belongs_to_many or has_many :through)
; from_coll pages
; from_field widgets_ids
; to_coll widgets
; to_field id
(defn find-relationship-to-many [app model-spec doc relationship]
  (let [opts (get-in model-spec [:relationships (keyword relationship)])
        coll (:to_coll opts)
        field (:to_field opts)
        ids ((:from_field opts) doc)
        query {field {:$in ids}}
        find-opts {}
        docs (db/find (:database app) coll query find-opts)]
    docs))

; Example: (i.e. ActiveRecord belongs_to)
; from_coll pages
; from_field widgets_id
; to_coll widgets
; to_field id
(defn find-relationship-to-one [app model-spec doc relationship]
  (let [opts (get-in model-spec [:relationships (keyword relationship)])
        coll (:to_coll opts)
        field (:to_field opts)
        id ((:from_field opts) doc)
        query {field id}
        find-opts {}
        docs (db/find (:database app) coll query find-opts)]
    (first docs)))

; Example: (i.e. ActiveRecord has_many)
; from_coll pages_versions
; from_field id
; to_coll pages
; to_field id
(defn find-relationship-from-many [app model-spec doc relationship]
  (let [opts (get-in model-spec [:relationships (keyword relationship)])
        coll (:from_coll opts)
        field (:from_field opts)
        id ((:to_field opts) doc)
        query {field id}
        find-opts {}
        docs (db/find (:database app) coll query find-opts)]
    docs))

(defn find-relationship [app model-spec doc relationship]
  (let [opts (get-in model-spec [:relationships (keyword relationship)])
        coll (model-api/coll model-spec)
        from-field (:from_field opts)
        multiple? (= (get-in model-spec [:schema :properties from-field :type]) "array")
        find-fn (cond
                  (and (= coll (:from_coll opts)) multiple?) find-relationship-to-many
                  (and (= coll (:from_coll opts)) (not multiple?)) find-relationship-to-one
                  (= coll (:to_coll opts)) find-relationship-from-many)]
    (println "pm debug find-relationships" find-fn (:from_coll opts) coll from-field multiple? (:to_coll opts))
    (find-fn app model-spec doc relationship)))

; TODO: validate ids of has-one and has-many if from_coll = (coll model-spec)
(defn validate-references-callback [doc options])
