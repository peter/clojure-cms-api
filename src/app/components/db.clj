(ns app.components.db
  (:refer-clojure :exclude [find update delete count])
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [monger.joda-time]
            [com.stuartsierra.component :as component])
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId]))

; Mongo API doc: http://clojuremongodb.info/articles/getting_started.html

; --------------------------------------------------------
; Component
; --------------------------------------------------------

(defrecord Database [config]
  component/Lifecycle

  (start [component]
    (println "Starting Database")
    (let [uri (get-in config [:config :mongodb-url])
          {:keys [db conn]} (mg/connect-via-uri uri)]
      (assoc component :db db :conn conn)))

  (stop [component]
    (println "Stopping Database")
    (mg/disconnect (:conn component))
    (dissoc component :db :conn)))

(defn new-database [& args]
  (map->Database (apply hash-map args)))

; --------------------------------------------------------
; Utility functions
; --------------------------------------------------------

(defn- json-friendly [doc]
  (let [friendly-id (.toString (:_id doc))]
    (assoc doc :_id friendly-id)))

(defn- mongo-friendly [doc]
  (if (string? (:_id doc))
    (assoc doc :_id (ObjectId. (:_id doc)))
    doc))

(defn mongo-map [columns]
  (apply array-map (interleave columns (repeat 1))))

(defn valid-object-id? [object-id]
  (re-matches #"^[0-9a-fA-F]{24}$" object-id))

; --------------------------------------------------------
; Public Mongo API
; --------------------------------------------------------

(defn ensure-index [database coll columns options]
  (mc/ensure-index (:db database) coll (mongo-map columns) options))

(defn find
  ([database coll query opts]
    (let [default-opts {:page 1 :per-page 100 :sort {} :fields []}
          opts (merge default-opts opts)]
      (map json-friendly (mq/with-collection (:db database) (name coll)
                                             (mq/find (mongo-friendly query))
                                             (mq/fields (:fields opts))
                                             (mq/paginate :page (:page opts) :per-page (:per-page opts))
                                             (mq/sort (:sort opts))))))
  ([database coll query]
    (find database coll query {})))

(defn find-one [database coll query]
  (first (find database coll query)))

(defn count [database coll query]
  (mc/count (:db database) coll query))

(defn create [database coll doc]
  (let [created-doc (assoc doc :_id (ObjectId.))
        result (mc/insert (:db database) coll created-doc)]
    {:doc (json-friendly created-doc) :result result}))

; For partial update - use: {:$set {:foo "bar"}}
(defn update [database coll query doc]
  (let [result (mc/update (:db database) coll (mongo-friendly query) (mongo-friendly doc))
        updated-doc (find-one database coll query)]
    {:doc updated-doc :result result}))

(defn delete [database coll query]
  (mc/remove (:db database) coll (mongo-friendly query)))
