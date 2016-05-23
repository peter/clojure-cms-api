(ns app.router.routes)

(defn routes []
  [
    {:methods #{:get} :path "/" :handler "home/index"}

    {:methods #{:post} :path "/v1/login" :handler "sessions/create"}

    {:methods #{:get} :path "/v1/pages" :handler "pages/api:list"}
    {:methods #{:get} :path "/v1/pages/:id" :handler "pages/api:get"}
    {:methods #{:post} :path "/v1/pages" :handler "pages/api:create"}
    {:methods #{:patch :put} :path "/v1/pages/:id" :handler "pages/api:update"}
    {:methods #{:delete} :path "/v1/pages/:id" :handler "pages/api:delete"}
  ])
