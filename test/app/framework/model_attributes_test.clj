(ns app.framework.model-attributes-test
  (:use midje.sweet)
  (:require [app.framework.model-attributes :refer [api-writable-attributes]]))

(fact "api-writable-attributes: selects attributes that are api_writable according to the schema"
  (let [attributes {
          :title "The title"
          :version_number 100}
        schema {
          :type "object"
          :properties {
            :title {:type "string"}
            :version_number {:type "integer" :api_writable false}
          }}]
    (api-writable-attributes attributes schema) => {:title "The title"}))
