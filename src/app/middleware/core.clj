(ns app.middleware.core
  (:require [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [app.middleware.cors :refer [wrap-cors]]
            [app.middleware.auth :refer [wrap-auth]]))

(defn development-middleware [handler env]
  (if (= env "development")
    (-> handler
      (wrap-reload))
    handler))

; NOTE: middleware execute execute in reverse order - the last one listed here exeucutes first
(defn wrap [handler app]
  (-> handler
      (wrap-keyword-params)
      (wrap-params {})
      (wrap-json-params {})
      (wrap-json-response {:pretty true})
      (wrap-cors)
      (wrap-auth app)
      (development-middleware (:env app))
  ))
