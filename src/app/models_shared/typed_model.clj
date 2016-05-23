(ns app.models-shared.typed-model)

(defn type-callback [doc options]
  (let [type (get-in options [:model-spec :type])]
    (assoc doc :type (name type))))

(def typed-schema {
  :type {:type "string"}
})

(def typed-callbacks {
    :save {
      :before [type-callback]
    }
})
