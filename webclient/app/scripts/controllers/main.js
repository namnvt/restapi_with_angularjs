'use strict';

/**
 * @ngdoc function
 * @name webclientApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the webclientApp
 */
angular.module('webclientApp')
  .controller('MainCtrl', function ($scope, $http) {
    $scope.getPosts = function() {
      $http.get('app/posts')
        .success(function(data) {
          $scope.posts = data;
        });
    };

    $scope.getPosts();

  });
