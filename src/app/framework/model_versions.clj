(ns app.framework.model-versions
  (:require [app.util.core :as u]
            [app.framework.model-support :as model-support]))

(defn versioned-attribute? [attribute-schema]
  (get attribute-schema :versioned true))

(defn versioned-attributes [schema]
  (filter #(versioned-attribute? (% (:properties schema)))
          (keys (:properties schema))))

(defn unversioned-attributes [schema]
  (clojure.set/difference (set (keys (:properties schema)))
                          (set (versioned-attributes schema))))

(defn versioned-coll [model-spec]
  (keyword (str (name (model-support/coll model-spec)) "_versions")))

(defn versioned-id-query [model-spec id version]
  (merge (model-support/id-query model-spec id)
         {:version version}))

(defn parse-version-id [doc version-param]
  (if (= version-param "published")
      (:published_version doc)
      (and version-param (u/safe-parse-int version-param))))