(ns app.framework.model-includes.content-base-model
  (:require [app.framework.model-includes.id-model :refer [id-spec]]
            [app.framework.model-includes.typed-model :refer [typed-spec]]
            [app.framework.model-includes.audited-model :refer [audited-spec]]
            [app.framework.model-includes.versioned-model :refer [versioned-spec]]
            [app.framework.model-includes.published-model :refer [published-spec]]
            [app.framework.model-includes.validated-model :refer [validated-spec]]))

(defn content-base-spec [type] [
  (id-spec)
  (typed-spec)
  (audited-spec)
  (versioned-spec :type type)
  (published-spec)
  (validated-spec)
])
