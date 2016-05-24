(ns app.models.users
  (:require [app.framework.model-spec :refer [generate-spec]]
            [app.components.db :as db]
            [app.util.core :as u]
            [app.util.encrypt :as encrypt]
            [app.util.date :as date]))

(def spec (generate-spec {
  :type :users
  :schema {
    :type "object"
    :properties {
      :name {:type "string"}
      :email {:type "string"}
      :password {:type "string"}
      :access_token {:type "string"}
      :access_token_created_at {:type "string" :format "date-time"}
    }
    :additionalProperties false
    :required [:name :email :password]
  }
  :indexes [
    {:fields [:email] :unique true}
    {:fields [:access_token] :unique true}
  ]}))

(defn authenticate [user password]
  (and user (encrypt/check password (:password user))))

(defn token-expired? [user config]
  (date/before? (:access_token_created_at user) (date/seconds-ago (:session-expiry config))))

; --------------------------------------------------------
; Database API
; --------------------------------------------------------

(defn create [database user]
  (let [encrypted-password (encrypt/generate (:password user))
        secured-user (assoc user :password encrypted-password)]
    (db/create database (:type spec) secured-user)))

(defn find-one [database query]
  (db/find-one database (:type spec) query))

(defn store-token [database user access-token]
  (let [attributes {:access_token access-token
                    :access_token_created_at (date/now)}]
    (db/update database (:type spec) {:email (:email user)} {:$set attributes})))
