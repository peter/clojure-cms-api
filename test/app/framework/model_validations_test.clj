(ns app.framework.model-validations-test
  (:use midje.sweet)
  (:require [app.framework.model-validations :refer [validate-schema]]))

(fact "validate-schema: return nil if there are no schema errors"
  (validate-schema {:title "The title"} {:type "object" :properties {:title {:type "string"}} :required [:title]})
    => nil)

(fact "validate-schema: can return the required schema error"
  (map :keyword (validate-schema {} {:type "object" :properties {:title {:type "string"}} :required [:title]}))
    => ["required"])
