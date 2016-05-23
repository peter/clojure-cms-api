(ns app.system
  (:require [com.stuartsierra.component :as component]
            [app.components.config :refer [new-config]]
            [app.components.db :refer [new-database]]
            [app.components.app :refer [new-application]]
            [app.components.web :refer [new-webserver]]))

(defn new-system [& config]
  (component/system-map
    :config (apply new-config config)
    :database (component/using (new-database) [:config])
    :app (component/using (new-application) [:config :database])
    :web (component/using (new-webserver) [:app])))

(defn -main [& args]
  (component/start (apply new-system args)))
