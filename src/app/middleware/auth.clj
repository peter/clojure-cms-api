(ns app.middleware.auth
  (:require [app.util.auth :refer [parse-token]]
            [app.models.users :as users]))

(def unauthorized-response {
  :status 401
  :body "Unauthorized"})

(def write-methods #{:post :put :patch})

(defn auth-required? [request]
  (and (write-methods (:request-method request))
       (not= "/v1/login" (:uri request))))

(defn require-auth [request handler app]
  (let [access-token (parse-token (:headers request))
        user (users/find-one (:database app) {:access_token access-token})]
    (if (and access-token user (not (users/token-expired? user (:config app))))
      (handler (assoc request :user user))
      unauthorized-response)))

(defn wrap-auth [handler app]
  (fn [request]
    (if (auth-required? request)
      (require-auth request handler app)
      (handler request))))
