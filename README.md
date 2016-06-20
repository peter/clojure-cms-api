# Clojure CMS API

This is a starter kit (template) that forms the foundation for a CMS REST API based on
Mongodb/Clojure. Features include token based user authentication,
JSON schema validation, versioning, publishing, relationships, changelog, and a model API with
before/after callbacks on create/update/delete operations.

The history of this app is that it is a re-implementation and simplification of the
Node.js/Mongodb CMS that we built to power the Swedish recipe website [köket.se](http://www.koket.se)
in 2015.

## Getting Started Tutorial

First make sure you have [Leiningen/Clojure](http://leiningen.org) and Mongodb installed.

Get the source:

```bash
git clone git@github.com:peter/clojure-cms-api.git
cd clojure-cms-api
```

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

Now, let's look at versioning, associations, and publishing. Create two widgets and a page:

```bash
curl -i -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"widgets": {"title": "Latest Movies", "published_version": 1}}' http://localhost:5000/v1/widgets

curl -i -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"widgets": {"title": "Latest Series"}}' http://localhost:5000/v1/widgets

curl -i -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"pages": {"title": "Start Page", "widgets_ids": [1, 2], "published_version": 1}}' http://localhost:5000/v1/pages
```

The first widget and the page are published since the `published_version` is set but the second widget is not. Now we can fetch the page with its associations:

```bash
curl -i http://localhost:5000/v1/pages/1?relationships=1
```

The response looks something like:

```json
{
  "data" : {
    "id" : "1",
    "type" : "pages",
    "attributes" : {
      "version" : 1,
      "created_at" : "2016-05-30T10:31:38.870+02:00",
      "type" : "pages",
      "id" : 1,
      "created_by" : "admin@example.com",
      "widgets_ids" : [ 1, 2 ],
      "title" : "Start Page",
      "published_version" : 1,
      "_id" : "574bfa6a64fd1debd081d5b0"
    },
    "relationships" : {
      "versions" : {
        "data" : [ {
          "id" : "1",
          "type" : "pages",
          "attributes" : {
            "created_at" : "2016-05-30T10:31:38.880+02:00",
            "version" : 1,
            "widgets_ids" : [ 1, 2 ],
            "id" : 1,
            "title" : "Start Page",
            "type" : "pages",
            "_id" : "574bfa6a64fd1debd081d5b1"
          }
        } ]
      },
      "widgets" : {
        "data" : [ {
          "id" : "1",
          "type" : "widgets",
          "attributes" : {
            "version" : 1,
            "created_at" : "2016-05-30T10:30:42.230+02:00",
            "type" : "widgets",
            "id" : 1,
            "created_by" : "admin@example.com",
            "title" : "Latest Movies",
            "published_version" : 1,
            "_id" : "574bfa3264fd1debd081d5aa"
          }
        }, {
          "id" : "2",
          "type" : "widgets",
          "attributes" : {
            "version" : 1,
            "created_at" : "2016-05-30T10:30:48.941+02:00",
            "type" : "widgets",
            "id" : 2,
            "created_by" : "admin@example.com",
            "title" : "Latest Series",
            "_id" : "574bfa3864fd1debd081d5ad"
          }
        } ]
      }
    }
  }
}
```

Notice how the page has a single version and how it is associated with two widgets, only the first of which has a published version.
Now, if we ask for the published version of the page (relevant to the end-user/public facing website) we don't get the version history
and we only get the first widget:

```bash
curl -i 'http://localhost:5000/v1/pages/1?relationships=1&published=1'
```

If the page hadn't been published we would have gotten a 404.

In addition to the version history there is a `changelog` collection in Mongodb with a log of all write operations performed via the API.
Here is an example entry from the update above:

```json
{
  "_id": "574bf1d564fd1debd081d5a8",
  "action": "update",
  "errors": null,
  "doc": {
    "_id": "574bf1bd64fd1debd081d5a4",
    "type": "pages",
    "title": "foo EDIT",
    "updated_at": "2016-05-30T07:55:01.728Z",
    "id": 1,
    "updated_by": "admin@example.com",
    "version": 2,
    "body": "bar",
    "created_by": "admin@example.com",
    "created_at": "2016-05-30T07:54:37.087Z"
  },
  "changes": {
    "title": {
      "from": "foo",
      "to": "foo EDIT"
    },
    "updated_at": {
      "from": null,
      "to": "2016-05-30T07:55:01.728Z"
    },
    "updated_by": {
      "from": null,
      "to": "admin@example.com"
    },
    "version": {
      "from": 1,
      "to": 2
    }
  },
  "created_by": "admin@example.com",
  "created_at": "2016-05-30T07:55:01.748Z"
}
```

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

## Bulk Import

There is a bulk import API that you can use if you need to load larger amounts of data (i.e. migrate from another CMS):

```
curl -i -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"model": "widgets", "data": [{"title": "Latest Movies", "published_version": 1}, {"title": "Latest Series"}]}' http://localhost:5000/v1/bulk_import
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

* Add first_published_at to published-model

* finish API tests (under api-test)

* Put all model specs in the app object. Memoize model-spec lookup

* get endpoint
  * which fields to include (cms needs more fields than www, compare ommit/disabled in contentful CMS)

* validation
  * unique constraint
  * deal with mongo errors?

* Scheduler that publishes and unpulishes documents based on publish_at/unpublish_at

* comply more with jsonapi.org
