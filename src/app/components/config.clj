(ns app.components.config
  (:require [clojure.string :as str]
            [app.util.core :as u]
            [com.stuartsierra.component :as component]))

(defn- get-env []
  (or (System/getenv "ENV") "development"))

(defn- default-config [env] {
  :session-expiry (* 60 60 24 14)
  :log-level (if (= "production" env) "info" "debug")
  :env env
  :port 5000
  :mongodb-url (str "mongodb://127.0.0.1/clojure-cms-api-" env)
})

(defn- env-key [config-key]
  (-> (name config-key)
      (str/upper-case)
      (str/replace "-" "_")))

(defn- env-value [config-key defaults]
  (let [value (System/getenv (env-key config-key))
        default-value (config-key defaults)]
    (if (and value (integer? default-value))
      (u/parse-int value)
      value)))

(defn- env-config [defaults]
  (u/compact (into {} (map #(vector % (env-value % defaults)) (keys defaults)))))

(defn- get-config [config]
  (let [defaults (default-config (get-env))]
    (merge defaults (env-config defaults) config)))

; --------------------------------------------------------
; Component
; --------------------------------------------------------

(defrecord Config [config]
  component/Lifecycle

  (start [component]
    (println "Starting Config")
    (let [config (get-config config)]
      (assoc component :config config)))

  (stop [component]
    (println "Stopping Config")
    (dissoc component :config)))

(defn new-config [& args]
  (map->Config (apply hash-map args)))
