"use strict";

var assert = require('assert');

module.exports = {
  suite: {
    name: "Pages CRUD",
    tests: [
      {
        name: "login_create_get_list_update_delete",
        description: "You can create a page object, get and list it, update its name, and then delete it",
        api_calls: [
          {
            it: "can log in",
            request: "POST /login",
            params: "{{users.admin}}",
            assert: function(body, headers) {
              this.users.admin.auth_header = headers['authorization'];
            }
          },

          {
            it: "can create a page object",
            request: "POST /pages",
            params: "{{pages.start}}",
            headers: {"Authorization": "{{users.admin.auth_header}}"},
            // status: 201,
            assert: function(body) {
              // assert(body.createdAt);
              // assert(new Date(body.createdAt) > new Date());
              console.log("pm debug create body", body);
              this.pages.start._id = body.data._id; // save the ID
              console.log("pm debug create id", this.pages.start._id);
            }
          },

          // {
          //   it: "can get the created GameScore object",
          //   request: "GET /classes/GameScore/{{users.joe.objectId}}",
          //   assert: function(body) {
          //     assert.equal(body.playerName, this.users.joe.playerName);
          //   }
          // },
          // {
          //   it: "can list GameScore objects",
          //   request: "GET /classes/GameScore",
          //   params: {
          //     order: "-created_at",
          //     limit: 3
          //   },
          //   assert: function(body) {
          //     assert.equal(body.results.length, 3);
          //     assert.equal(body.results[0].objectId, this.users.joe.objectId);
          //   }
          // },
          // {
          //   it: "can update name of GameScore object",
          //   request: "PUT /classes/GameScore/{{users.joe.objectId}}",
          //   params: {
          //     playerName: "New name"
          //   }
          // },
          // {
          //   it: "can get new name of GameScore object",
          //   request: "GET /classes/GameScore/{{users.joe.objectId}}",
          //   assert: function(body) {
          //     assert.equal(body.playerName, "New name");
          //   }
          // },
          // {
          //   it: "can delete GameScore object",
          //   request: "DELETE /classes/GameScore/{{users.joe.objectId}}"
          // },
          // {
          //   it: "can verify that GameScore object was deleted",
          //   request: "GET /classes/GameScore/{{users.joe.objectId}}",
          //   status: 404
          // }
        ]
      }
    ]
  }
};
