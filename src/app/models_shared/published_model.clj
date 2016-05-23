(ns app.models-shared.published-model
  (:require [app.models-shared.versioned-model :refer [latest-version]]))

(defn adjust-published-version
  "Make sure published version is not greater than latest version"
  [version published-version]
  (if (and published-version (> published-version version))
    version
    published-version))

(defn published-version-callback [doc options]
  (let [published-version (adjust-published-version (latest-version (:model-spec options) doc) (:published-version doc))]
    (if published-version
      (assoc doc :published_version published-version)
      doc)))

(def published-schema {
  :type "object"
  :properties {
    :published_version {:type "integer" :minimum 1 :versioned false}
    :publish_at {:type "string" :format "date-time" :versioned false}
    :unpublish_at {:type "string" :format "date-time" :versioned false}
  }
})

(def published-callbacks {
  :save {
    :before [published-version-callback]
    :after []
  }
})
