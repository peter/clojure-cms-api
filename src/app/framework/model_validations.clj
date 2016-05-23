(ns app.framework.model-validations
  (:require [clojure.string :as str]
            [app.util.core :as u]))

(defn with-model-errors [doc errors]
  (let [new-meta (update-in (meta doc) [:errors] concat errors)]
    (with-meta doc new-meta)))

(defn model-errors [doc]
  (:errors (meta doc)))

(def model-not-updated [{:type "unchanged"}])

(defn blank?
  "returns true if value is nil or empty"
  [value]
  (cond (nil? value) true
        (and (string? value) (= (count (str/trim value)) 0)) true
        (and (coll? value) (= (count value) 0)) true
        :else false))

(defn present? [value]
  (not (blank? value)))

(defn required? [spec]
  (not (:optional spec)))

(defn validate-attribute [doc attribute spec]
  (cond (and (required? spec) (blank? (attribute doc))) {:attribute attribute :type :required}
        :else nil))

(defn validate-schema [doc schema]
  (->> (map #(apply validate-attribute doc %) schema)
       (u/compact)
       (not-empty)))
