(ns app.framework.model-callbacks
  (:require [app.util.core :as u]
            [app.framework.model-validations :refer [model-errors]]))

(defn composed-callbacks [callbacks options]
  (apply comp (map #(u/partial-right % options) (reverse callbacks))))

(defn invoke-callbacks [callbacks options doc]
  ((composed-callbacks callbacks options) doc))

(defn with-callbacks [model-fn action]
  (fn [app model-spec doc]
    (let [before-callbacks (get-in model-spec [:callbacks action :before] [])
          after-callbacks (get-in model-spec [:callbacks action :after] [])
          options {:app app :database (:database app) :action action :model-spec model-spec :schema (:schema model-spec)}]
        ; TODO: capture this chaining pattern with an abort condition in a function/macro?
        (let [doc (invoke-callbacks before-callbacks options doc)]
          (if-not (model-errors doc)
            (let [doc (model-fn app model-spec doc)]
              (if-not (model-errors doc)
                (invoke-callbacks after-callbacks options doc)
                doc))
            doc)))))
