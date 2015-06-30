////////
// This sample is published as part of the blog article at www.toptal.com/blog
// Visit www.toptal.com/blog and subscribe to our newsletter to read great posts
////////

'use strict';

/**
 * @ngdoc function
 * @name webclientApp.controller:DashboardCtrl
 * @description
 * # DashboardCtrl
 * Controller of the webclientApp
 */
angular.module('webclientApp')
    .controller('DashboardCtrl', function ($scope, $log, $http, alertService, $location) {

      $scope.loadPosts = function() {
        $http.get('/app/userposts')
            .error(function(data, status) {
              if(status === 401) {
                $location.path('/login');
              } else {
                alertService.add('danger', data.error.message);
              }
            })
            .success(function(data) {
              $scope.posts = data;
            });
      };

      $scope.loadPosts();
    });
