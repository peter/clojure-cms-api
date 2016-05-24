(ns app.framework.model-indexes
  (:require [app.components.db :as db]
            [app.framework.model-api :as model-api]
            [app.framework.model-reflect :refer [model-spec all-models]]))

(defn ensure-indexes [database]
  (doseq [spec (filter #(:indexes %) (map model-spec (all-models)))]
    (doseq [[columns options] (:indexes spec)]
      (let [coll (or (:coll options) (model-api/coll spec))
            options (dissoc options :coll)]
        (db/ensure-index database coll columns options)))))
