# Clojure CMS API

This is a starter kit (template) that forms the foundation for a CMS REST API based on
Mongodb/Clojure. Features include token based user authentication,
JSON schema validation, versioning, relationships, changelog, and a model API with
before/after callbacks on create/update/delete operations.

The history of this app is that it is a re-implementation and simplification of the
Node.js/Mongodb CMS that we built to power the Swedish recipe website [köket.se](http://www.koket.se)
in 2015.

## Getting Started Tutorial

First make sure you have [Leiningen/Clojure](http://leiningen.org) and Mongodb installed.

Create an admin user:

```
lein repl
(require '[app.system])
(def system (app.system/-main :start-web false))
(require '[app.models.users :as users])
(users/create (:database system) {:name "Admin User" :email "admin@example.com" :password "admin"})
exit
```

Check out the [pages](src/app/models/pages.clj) and [widgets](src/app/models/widgets.clj)
models that we will be working with. Here is the essence of the pages model:

```clojure
(def model-type :pages)

(def spec (generate-spec
  (content-base-spec model-type)
  {
  :type model-type
  :schema {
    :type "object"
    :properties {
      :title {:type "string"}
      :body {:type "string"}
      :widgets_ids {
        :type "array"
        :items {
          :type "integer"
        }
      }
    }
    :additionalProperties false
    :required [:title]
  }
  :relationships {
    :widgets {}
  }
  :indexes [
    {:fields [:title] :unique true}
  ]
}))
```

The `type` property corresponds to a Mongodb collection name. The schema is a JSON schema
that is used to validate documents before they are saved to the database. Any relationships
are configured separately and the `widgets` relationship corresponds to the `widgets_ids`
property with is an array of id references to the `widgets` collection. The
`indexes` section is a list of indexes that should be created in Mongodb.

The `pages` model inherits from the `content-base-spec` that provides the following
features:

```clojure
(defn content-base-spec [type] [
  (id-spec)
  (typed-spec)
  (audited-spec)
  (versioned-spec :type type)
  (published-spec)
  (validated-spec)
])
```

Model specifications may also contain a `callbacks` property with before/after
callbacks for create/update/delete operations. As an illusatration, here are
the callbacks used by `audited-spec`:

```clojure
(defn audit-create-callback [doc options]
  (assoc doc :created_at (d/now)))

(defn audit-update-callback [doc options]
  (assoc doc :updated_at (d/now)))

(def audited-callbacks {
  :create {
    :before [audit-create-callback]
  }
  :update {
    :before [audit-update-callback]
  }
})
```

What the `id-spec` does is add a numeric sequential `id` that
we can use instead of the default Mongodb `_id` field (which is a 24 character hexadecimal).

Now, if you haven't already done so, start up the API in a different terminal with `lein run`.

To be able to create pages and widgets via the API (perform write operations)
we first need to log in:

```bash
curl -i -X POST -H 'Content-Type: application/json' -d '{"email": "admin@example.com", "password": "admin"}' http://localhost:5000/v1/login

export TOKEN=<token in header response above>
```

Here is a simple CRUD flow for pages:

```bash
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

TODO: continue with examples for versioning and relationships.

## Starting the Server

```
lein run
```

## Running Tests

See the [midje tutorial](https://github.com/marick/Midje/wiki/A-tutorial-introduction) for setup instructions.

```
lein midje
```

## The REPL

```
lein repl
```

## TODO

* Validation
  * Validate association id references before save
  * Validate published_version reference before save

* list endpoint
  * Default sort order id desc
  * support sort query parameter
  * support query?

* Use Swagger: https://github.com/metosin/ring-swagger

* finish API tests (under api-test)

* Put all model specs in the app object. Memoize model-spec lookup

* get endpoint
  * which fields to include (cms needs more fields than www, compare ommit/disabled in contentful CMS)

* validation
  * unique constraint
  * deal with mongo errors?

* Scheduler that publishes and unpulishes documents based on publish_at/unpublish_at

* comply more with jsonapi.org
