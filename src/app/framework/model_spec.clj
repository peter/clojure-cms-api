(ns app.framework.model-spec
  (:require [app.util.core :as u]))

(defn merge-callbacks [& callbacks]
  (apply u/deep-merge-with concat callbacks))

(def empty-callback {:before [] :after []})

(defn save-callbacks [callbacks]
  {:update (:save callbacks) :create (:save callbacks)})

(defn callbacks [callbacks]
  (if (:save callbacks)
    (merge-callbacks (save-callbacks callbacks) (dissoc callbacks :save))
    callbacks))

(defn generate-spec [model-spec]
  (let [model-callbacks (callbacks (:callbacks model-spec))]
    (assoc model-spec :callbacks model-callbacks)))
