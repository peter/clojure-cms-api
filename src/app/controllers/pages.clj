(ns app.controllers.pages
  (:require [app.models.pages :as pages]
            [app.framework.crud-api :as crud-api]))

(def api (crud-api/new-api :model-spec pages/spec))
