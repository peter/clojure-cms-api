(ns app.bootstrap.indexes
  (:require [app.components.db :as db]
            [app.framework.model-reflect :refer [model-spec all-models]]))

(defn ensure-indexes [database]
  (doseq [spec (filter #(:indexes %) (map model-spec (all-models)))]
    (doseq [[columns options] (:indexes spec)]
      (db/ensure-index database (:type spec) columns options))))
