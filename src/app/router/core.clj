(ns app.router.core
  (:require [app.logger :as logger]
            [app.util.resource :as resource]
            [app.router.match :as m]))

(defn missing-handler [app request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (resource/get "404.html")})

(defn log-route-match [app request match]
  (logger/debug app (str
    "route match "
    (:remote-addr request) " "
    (:request-method request) " " (:uri request) " "
    (pr-str (:route match)) " "
    (pr-str (:params request)) " "
  )))

(defn create-handler [app routes]
  (fn [request]
    (let [match (m/find-match routes request)
          handler (if match (get-in match [:route :handler]) missing-handler)
          request-with-params (update-in request [:params] merge (:params match))]
    (when match (log-route-match app request-with-params match))
    (handler app request-with-params))))
