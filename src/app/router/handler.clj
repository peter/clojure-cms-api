(ns app.router.handler
  (:require [app.util.core :as u]))

(defn- parse-handler [handler]
  (drop 1 (re-matches #"(.+)\/(api:)?(.+)" handler)))

(defn- lookup-var-handler [controller action]
  (let [path (str "app.controllers." controller "/" action)]
    (u/load-var path)))

; TODO: the API implementation here is a bit of a hack. Would be nice to find a cleaner/simpler solution
(defn- lookup-api-handler [controller action]
  (let [crud-fn (u/load-var (str "app.framework.crud-api/" action))
        api @(u/load-var (str "app.controllers." controller "/api"))]
    (partial crud-fn api)))

(defn lookup-handler-uncached [route]
  (let [[controller api action] (parse-handler (:handler route))]
    (if api
      (lookup-api-handler controller action)
      (lookup-var-handler controller action))))

(def lookup-handler (memoize lookup-handler-uncached))
