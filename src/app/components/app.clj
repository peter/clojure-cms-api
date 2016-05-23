(ns app.components.app
  (:require [com.stuartsierra.component :as component]
            [clojure.string :as str]
            [app.middleware.core :as middleware]
            [app.bootstrap.indexes :refer [ensure-indexes]]
            [app.router.core :as router]
            [app.router.routes :refer [routes]]))

(defrecord Application [database config]
  component/Lifecycle

  (start [component]
    (let [config (:config config)
          app {:config config :database database}
          handler (-> (router/create-handler app (routes))
                      (middleware/wrap app))]
      (println "Starting Application config:" config)
      (ensure-indexes database)
      (assoc component :config config :routes routes :handler handler)))

  (stop [component]
    (println "Stopping Application config:" config)
    (dissoc component :handler)))

(defn new-application [& args]
  (map->Application (apply hash-map args)))
