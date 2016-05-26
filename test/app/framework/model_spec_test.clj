(ns app.framework.model-spec-test
  (:use midje.sweet)
  (:require [app.framework.model-spec :as model-spec]))

(fact "save-callbacks: converts save callback to update/create callbacks"
  (model-spec/save-callbacks {:create {:before [:foo]} :update {:before [:bar]} :save {:before [:bla] :after [:baz]}}) =>
    {:create {:before [:bla] :after [:baz]} :update {:before [:bla] :after [:baz]}})

(fact "normalize-callbacks: does not touch callbacks without save"
  (model-spec/normalize-callbacks {:create {:before [:foo]} :update {:before [:bar]}}) =>
    {:create {:before [:foo]} :update {:before [:bar]}})

(fact "normalize-callbacks: merges save callbacks with update/create callbacks"
  (model-spec/normalize-callbacks {:save {:before [:foo]} :update {:before [:bar]}}) =>
    {:create {:before [:foo]} :update {:before [:foo :bar]}})

(fact "generate-spec: deep merges schema/callbacks/indexes for specs"
  (let [spec1 {
          :type "spec1"
          :schema {
            :properties {
              :spec1 {:type "string"}
            }
            :required [:spec1]
          }
          :callbacks {
            :create {
              :before [:spec1]
            }
          }
          :indexes [{:fields [:spec1]}]
        }
        spec2 {
          :type "spec2"
          :schema {
            :properties {
              :spec2 {:type "integer"}
            }
            :required [:spec2]
          }
          :callbacks {
            :create {
              :before [:spec2]
            }
          }
          :indexes [{:fields [:spec2]}]
        }
        expect {
          :type "spec2"
          :schema {
            :properties {
              :spec1 {:type "string"}
              :spec2 {:type "integer"}
            }
            :required [:spec1 :spec2]
          }
          :callbacks {
            :create {
              :before [:spec1 :spec2]
            }
          }
          :indexes [{:fields [:spec1]} {:fields [:spec2]}]
        }]
  (model-spec/generate-spec spec1 spec2) => expect))
