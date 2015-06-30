////////
// This sample is published as part of the blog article at www.toptal.com/blog
// Visit www.toptal.com/blog and subscribe to our newsletter to read great posts
////////

'use strict';

/**
 * @ngdoc function
 * @name webclientApp.controller:SignupCtrl
 * @description
 * # SignupCtrl
 * Controller of the webclientApp
 */
angular.module('webclientApp')
  .controller('SignupCtrl', function ($scope, $http, $log, alertService, $location, userService) {

    $scope.signup = function() {
      var payload = {
        username : $scope.username,
        email : $scope.email,
        password : $scope.password
      };

      $http.post('app/signup', payload)
        .error(function(data, status) {
          if(status === 400) {
            angular.forEach(data, function(value, key) {
              if(key === 'username' || key === 'email' || key === 'password') {
                alertService.add('danger', key + ' : ' + value);
              } else {
                alertService.add('danger', value.message);
              }
            });
          }
          if(status === 500) {
            alertService.add('danger', 'Internal server error!');
          }
        })
        .success(function(data) {
          if(data.hasOwnProperty('success')) {
            userService.username = $scope.username;
            $location.path('/dashboard');
          }
        });
    };
  });
