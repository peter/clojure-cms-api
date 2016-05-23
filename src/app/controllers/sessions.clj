(ns app.controllers.sessions
  (:require [app.models.users :as users]
            [app.util.auth :as auth]))

(defn create [app request]
  (let [email (get-in request [:params :email])
        password (get-in request [:params :password])
        user (users/find-one (:database app) {:email email})]
      (if (users/authenticate user password)
        (let [access-token (auth/generate-token)]
          (users/store-token (:database app) user access-token)
          {:status 200 :headers (auth/header access-token)})
        {:status 401})))
