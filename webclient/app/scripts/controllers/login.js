////////
// This sample is published as part of the blog article at www.toptal.com/blog
// Visit www.toptal.com/blog and subscribe to our newsletter to read great posts
////////

'use strict';

/**
 * @ngdoc function
 * @name webclientApp.controller:LoginCtrl
 * @description
 * # LoginCtrl
 * Controller of the webclientApp
 */
angular.module('webclientApp')
    .controller('LoginCtrl', function ($scope, userService, $location, $log, $http, alertService) {

      $scope.isAuthenticated = function() {
        if(userService.username) {
          $log.debug(userService.username);
          $location.path('/dashboard');
        } else {
          $http.get('/app/isauthenticated')
              .error(function() {
                $location.path('/login');
              })
              .success(function(data) {
                if(data.hasOwnProperty('success')) {
                  userService.username = data.success.user;
                  $location.path('/dashboard');
                }
              });
        }
      };

      $scope.isAuthenticated();

      $scope.login = function() {

        var payload = {
          username : this.username,
          password : this.password
        };

        $http.post('/app/login', payload)
            .error(function(data, status){
              if(status === 400) {
                angular.forEach(data, function(value, key) {
                  if(key === 'username' || key === 'password') {
                    alertService.add('danger', key + ' : ' + value);
                  } else {
                    alertService.add('danger', value.message);
                  }
                });
              } else if(status === 401) {
                alertService.add('danger', 'Invalid login or password!');
              } else if(status === 500) {
                alertService.add('danger', 'Internal server error!');
              } else {
                alertService.add('danger', data);
              }
            })
            .success(function(data){
              $log.debug(data);
              if(data.hasOwnProperty('success')) {
                userService.username = data.success.user;
                $location.path('/dashboard');
              }
            });
      };
    });
