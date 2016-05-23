(ns app.framework.model-spec-test
  (:use midje.sweet)
  (:require [app.framework.model-spec :as model-spec]))

(fact "save-callbacks: converts save callback to update/create callbacks"
  (model-spec/save-callbacks {:create {:before [:foo]} :update {:before [:bar]} :save {:before [:bla] :after [:baz]}}) =>
    {:create {:before [:bla] :after [:baz]} :update {:before [:bla] :after [:baz]}})

(fact "callbacks: does not touch callbacks without save"
  (model-spec/callbacks {:create {:before [:foo]} :update {:before [:bar]}}) =>
    {:create {:before [:foo]} :update {:before [:bar]}})

(fact "callbacks: merges save callbacks with update/create callbacks"
  (model-spec/callbacks {:save {:before [:foo]} :update {:before [:bar]}}) =>
    {:create {:before [:foo]} :update {:before [:foo :bar]}})

(fact "generate-spec: prepends save callbacks on update/create callbacks"
  (model-spec/generate-spec {:callbacks {:save {:before [:type :audit]} :create {:before [:id]}}}) =>
    {:callbacks {:update {:before [:type :audit]} :create {:before [:type :audit :id]}}})
