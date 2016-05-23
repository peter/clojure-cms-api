(ns app.controllers.home)

(defn index [app request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Welcome Clojure CMS API"})
