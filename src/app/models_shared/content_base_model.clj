(ns app.models-shared.content-base-model
  (:require [app.models-shared.id-model :refer [id-spec]]
            [app.models-shared.typed-model :refer [typed-spec]]
            [app.models-shared.audited-model :refer [audited-spec]]
            [app.models-shared.versioned-model :refer [versioned-spec]]
            [app.models-shared.published-model :refer [published-spec]]
            [app.models-shared.validated-model :refer [validated-spec]]))

(defn content-base-spec [type] [
  (id-spec)
  (typed-spec)
  (audited-spec)
  (versioned-spec :type type)
  (published-spec)
  (validated-spec)
])
