(ns app.routes
  (:require [app.framework.model-reflect :refer [model-spec]]
            [app.util.core :as u]
            [app.framework.crud-api :as crud-api]))

; TODO: a cleaner syntax for specifying API endpoints and controller handlers
(defn- parse-handler [handler]
  (drop 1 (re-matches #"(.+)\/(api:)?(.+)" handler)))

(defn- lookup-var-handler [controller action]
  (let [path (str "app.controllers." controller "/" action)]
    (u/load-var path)))

; TODO: the API implementation here is a bit of a hack. Would be nice to find a cleaner/simpler solution
(defn- lookup-api-handler [model action]
  (let [crud-fn (u/load-var (str "app.framework.crud-api/" action))
        api (crud-api/new-api :model-spec (model-spec model))]
    (partial crud-fn api)))

(defn lookup-handler-uncached [route]
  (let [[controller api action] (parse-handler (:handler route))]
    (if api
      (lookup-api-handler controller action)
      (lookup-var-handler controller action))))

(def lookup-handler (memoize lookup-handler-uncached))

(defn- crud-routes [model & {:keys [actions]
                             :or {actions [:list :get :create :update :delete]}}]
  (vals (select-keys {
    :list {:methods #{:get} :path (str "/v1/" (name model)) :handler (str (name model) "/api:list")}
    :get {:methods #{:get} :path (str "/v1/" (name model) "/:id") :handler (str (name model) "/api:get")}
    :create {:methods #{:post} :path (str "/v1/" (name model)) :handler (str (name model) "/api:create")}
    :update {:methods #{:patch :put} :path (str "/v1/" (name model) "/:id") :handler (str (name model) "/api:update")}
    :delete {:methods #{:delete} :path (str "/v1/" (name model) "/:id") :handler (str (name model) "/api:delete")}
  } actions)))

(defn routes []
  (flatten [
    {:methods #{:get} :path "/" :handler "home/index"}

    {:methods #{:post} :path "/v1/login" :handler "sessions/create"}

    {:methods #{:post} :path "/v1/bulk_import" :handler "bulk-import/create"}

    (crud-routes :sections)
    (crud-routes :pages)
    (crud-routes :widgets)
  ]))

(defn routes-with-handlers []
  (map #(assoc % :handler (lookup-handler %)) (routes)))
