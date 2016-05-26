(ns app.util.schema
  (:require [app.util.core :as u]
            [scjsv.core :as v]))

(defn json-type? [value]
  (boolean (some #(% value) [string? keyword? number? u/boolean? nil? map? vector?])))

; Test with: {:foo [(fn []) :foobar] :bar {:baz (fn []) :bla :bla}}
(defn json-friendly-map [m]
  (u/deep-map-values #(if (json-type? %) % (.toString %)) m))

(defn validate-schema [schema doc]
  ((v/validator schema) (json-friendly-map doc)))
