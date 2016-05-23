(ns app.framework.model-changes-test
  (:use midje.sweet)
  (:require [app.framework.model-changes :refer [changed-value? model-changes model-changed?]]))

(fact "changed-value?: returns true if from and to are equal"
  (changed-value? {:foo 1} {:foo 1}) => false)

(fact "changed-value?: returns false if from and to are not equal"
  (changed-value? {:foo 1} {:foo 2}) => true)

(fact "model-changes: returns from/to values for all changed attributes based on :existing-doc meta"
  (let [existing-doc {:changed "changed" :removed "removed"}
        new-doc {:changed "changed EDIT" :added "added"}
        doc (with-meta new-doc {:existing-doc existing-doc})
        expected {
          :changed {:from "changed" :to "changed EDIT"}
          :removed {:from "removed" :to nil}
          :added {:from nil :to "added"}}]
    (model-changes doc) => expected))

(fact "model-changed?: with one argument returns true if there is any change in doc"
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})]
      (model-changed? doc) => truthy))

(fact "model-changed?: with one argument returns false if there is no change in doc"
  (let [existing-doc {:title "title"}
        new-doc {:title "title"}
        doc (with-meta new-doc {:existing-doc existing-doc})]
      (model-changed? doc) => falsey))

(fact "model-changed?: with two arguments returns true if certain argument has changed"
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})]
      (model-changed? doc :title) => truthy))

(fact "model-changed?: with two arguments returns false if certain argument has not changed"
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})]
      (model-changed? doc :body) => falsey))

(fact "model-changed?: with three arguments returns true if certain argument has changed from one value to another"
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})]
      (model-changed? doc :title "title" "title changed") => truthy))

(fact "model-changed?: with three arguments returns false if certain argument has not changed from one value to another"
  (let [existing-doc {:title "title"}
        new-doc {:title "title changed"}
        doc (with-meta new-doc {:existing-doc existing-doc})]
      (model-changed? doc :title "title" "title changed different") => falsey))
