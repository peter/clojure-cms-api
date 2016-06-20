(ns app.framework.model-indexes
  (:require [app.framework.db-api :as db]
            [app.framework.model-support :as model-support]
            [app.framework.model-reflect :refer [model-spec all-models]]))

(defn ensure-indexes [database]
  (doseq [spec (filter #(:indexes %) (map model-spec (all-models)))]
    (doseq [options (:indexes spec)]
      (let [coll (or (:coll options) (model-support/coll spec))
            fields (:fields options)
            options (dissoc options :coll :fields)]
        (db/ensure-index database coll fields options)))))
