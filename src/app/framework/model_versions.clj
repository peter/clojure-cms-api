(ns app.framework.model-versions
  (:require [app.util.core :as u]
            [app.framework.model-reflect :as model-reflect]
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

(defn select-version [doc version-param published?]
  (if published?
    (:published_version doc)
    (and version-param (u/safe-parse-int version-param))))

(defn apply-version [model-spec doc versioned-doc]
  (and versioned-doc (merge versioned-doc
                            (select-keys doc
                                         (unversioned-attributes (:schema model-spec))))))

(defn published-model? [model]
  (and model (get-in (model-reflect/model-spec model) [:schema :properties :published_version])))
