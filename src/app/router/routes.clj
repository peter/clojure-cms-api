(ns app.router.routes)

(defn- crud-routes [model]
  [
    {:methods #{:get} :path (str "/v1/" model) :handler (str model "/api:list")}
    {:methods #{:get} :path (str "/v1/" model "/:id") :handler (str model "/api:get")}
    {:methods #{:post} :path (str "/v1/" model) :handler (str model "/api:create")}
    {:methods #{:patch :put} :path (str "/v1/" model "/:id") :handler (str model "/api:update")}
    {:methods #{:delete} :path (str "/v1/" model "/:id") :handler (str model "/api:delete")}
  ])

(defn routes []
  (flatten [
    {:methods #{:get} :path "/" :handler "home/index"}

    {:methods #{:post} :path "/v1/login" :handler "sessions/create"}

    (crud-routes "pages")
  ]))
