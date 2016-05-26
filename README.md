# Clojure CMS API

This is a basic CMS REST API based on Mongodb/Clojure with features like user authentication,
JSON schema validation, versioning, and changelog.

## TODO

* Add more models with associations

* list endpoint
  * Default sort order id desc, support sort query parameter
  * query

* get endpoint
  * Include associated versions
  * version query parameter (id|latest|published)
   * which fields to include (cms needs more fields than www)

* Associations/Relationships
  * Include/embed associated docs on API get. Comply with jsonapi.org JSON structure.
  * Validate id references before save

* Use Swagger: https://github.com/metosin/ring-swagger

* validation
  * unique constraint
  * deal with mongo errors?

* scheduled publishing

* finish API tests (under api-test)

* comply more with jsonapi.org

## Starting the Server

```
lein run
```

## Running Tests

See the [midje tutorial](https://github.com/marick/Midje/wiki/A-tutorial-introduction) for setup instructions.

```
lein midje :autotest
```

## REPL

```
lein repl
```

## Working with MongoDB

```
(require '[app.components.db :as db])
(db/insert "pages" {:title "Start Page" :body "Welcome to Clojure CMS"})
(db/find-one "pages" {:title "Start Page"})
(pprint (db/find "pages" {}))
```

[Advanced queries](http://clojuremongodb.info/articles/getting_started.html#using_mongodb_query_operators):

```
(require '[monger.query :as query])
(def db (get-in system [:database :db]))
(query/with-collection db "pages" (query/find {}) (query/fields []) (query/paginate :page 1 :per-page 100) (query/sort {}))
```

## Working with users

Creating admin user:

```
(require '[app.system])
(def system (app.system/-main :start-web false))
(require '[app.models.users :as users])
(users/create (:database system) {:name "Admin User" :email "admin@example.com" :password "admin"})
```

Check token expiry:

```
(require '[app.system])
(def system (app.system/-main :start-web false))
(require '[app.models.users :as users])
(def user (users/find-one (:database system) {:email "admin@example.com"}))
(users/token-expired? user (get-in system [:app :config]))
```

## Invoking the API

```bash
# login
curl -i -X POST -H 'Content-Type: application/json' -d '{"email": "admin@example.com", "password": "admin"}' http://localhost:5000/v1/login

export TOKEN=<token in header response above>

# create
curl -i -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"pages": {"title": "foo", "body": "bar"}}' http://localhost:5000/v1/pages

# get
curl -i http://localhost:5000/v1/pages/1

# list
curl -i http://localhost:5000/v1/pages

# update
curl -i -X PUT -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"pages": {"title": "foo EDIT"}}' http://localhost:5000/v1/pages/1

# delete
curl -i -X DELETE -H "Authorization: Bearer $TOKEN" http://localhost:5000/v1/pages/1
```

## Using Components

```
(require '[app.components.db :as db :reload-all true])
(require '[com.stuartsierra.component :as component])
(def database (db/new-database))
(def database (component/start database))
(def database (component/stop database))
```

```
(require '[app.components.app :refer [new-application]])
(require '[com.stuartsierra.component :as component])
(def the-app (new-application))
(def the-app (component/start the-app))
(def the-app (component/stop the-app))
```

```
(require '[app.components.web :as web :reload-all true])
(require '[com.stuartsierra.component :as component])
(def webserver (web/new-webserver))
(def webserver (component/start webserver))
(def webserver (component/stop webserver))
```

## Password Encryption

```
(require '[crypto.password.scrypt :as password])
(def hash (password/encrypt "foobar"))
(password/check "foobar" hash)
```

## The CrudApi Protocol

```
(require '[app.system])
(def system (app.system/-main :start-web false))
(require '[app.api.crud-api :as crud-api :reload-all true])
(require '[app.models.pages :as pages])
(def api (crud-api/new-api :model-spec pages/spec))
(crud-api/list api (:app system) {})
```

## JSON Schema

```
(require '[scjsv.core :as v])
(def schema {
  :type "object"
  :properties {
    :title {:type "string"}
    :body {:type "string"}
    :publish_at {:type "string" :format "date-time" :optional true :versioned false}
  }
  :additionalProperties false
  :required ["title"]
})
((v/validator schema) {})
((v/validator schema) {:title "foobar"})
((v/validator schema) {:title "foobar" :publish_at "foobar"})
((v/validator schema) {:title "foobar" :publish_at "2012-04-23T18:25:43.511Z"})
(require '[app.util.date :as d])
((v/validator schema) {:title "foobar" :publish_at (d/now)})
```

```
(require '[scjsv.core :as v])
(require '[app.util.core :as u :reload-all true])
(require '[app.framework.model-spec :as model-spec])
(require '[app.models.pages :as pages])
((v/validator model-spec/spec-schema) (u/json-friendly-map pages/spec))
(pprint (u/json-friendly-map pages/spec))
(pprint model-spec/spec-schema)
```
