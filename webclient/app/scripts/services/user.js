////////
// This sample is published as part of the blog article at www.toptal.com/blog
// Visit www.toptal.com/blog and subscribe to our newsletter to read great posts
////////

'use strict';

/**
 * @ngdoc service
 * @name webclientApp.user
 * @description
 * # user
 * Service in the webclientApp.
 */
angular.module('webclientApp')
    .factory('userService', function() {
      var username = '';

      return {
        username : username
      };
    });
