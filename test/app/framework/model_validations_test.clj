(ns app.framework.model-validations-test
  (:use midje.sweet)
  (:require [app.framework.model-validations :refer [validate-schema]]))

(fact "validate-schema: return nil if there are no schema errors"
  (validate-schema {:title "The title"} {:title {:type "string"}}) => nil)

(fact "validate-schema: can return the required schema error"
  (validate-schema {} {:title {:type "string"}}) => [{:attribute :title :type :required}])
