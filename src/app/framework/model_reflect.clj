(ns app.framework.model-reflect
  (:require [app.util.core :as u]))

(def models-dir "src/app/models")
(def clj-file-pattern #"(.+)\.clj")
(def spec-var-name "spec")

(defn model-name [filename]
  (second (re-matches clj-file-pattern filename)))

(defn all-models []
  (let [files (file-seq (clojure.java.io/file models-dir))]
    (->> files
        (map #(.getName %))
        (map #(model-name %))
        (u/compact))))

(defn model-spec [model-name]
  (let [spec-path (str "app.models." (name model-name) "/" spec-var-name)
        spec-var (u/load-var spec-path)]
    (and spec-var @spec-var)))
