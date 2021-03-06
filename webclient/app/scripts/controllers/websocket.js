var addZero, toHours, toMinutes, toSeconds, utils;

utils = window.angular.module('utils', []);

utils.filter('timer', [
  'time', function(time) {
    return function(input) {
      var hours, minutes, seconds;
      if (input) {
        seconds = toSeconds(input);
        minutes = toMinutes(input);
        hours = toHours(input);
        return hours + ":" + minutes + ":" + seconds;
      } else {
        return "Press Start";
      }
    };
  }
]).service('time', function() {
  this.toHours = function(timeMillis) {
    return addZero(timeMillis / (1000 * 60 * 60));
  };
  this.toMinutes = function(timeMillis) {
    return addZero((timeMillis / (1000 * 60)) % 60);
  };
  this.toSeconds = function(timeMillis) {
    return addZero((timeMillis / 1000) % 60);
  };
  this.toTime = function(hours, minutes, seconds) {
    return ((hours * 60 * 60) + (minutes * 60) + seconds) * 1000;
  };
  return this.addZero = function(value) {
    value = Math.floor(value);
    if (value < 10) {
      return "0" + value;
    } else {
      return value;
    }
  };
}).controller('TimerController', function($scope, $http) {
  var startWS;
  startWS = function() {
    var wsUrl;
    wsUrl = jsRoutes.controllers.AppController.indexWS().webSocketURL();
    $scope.socket = new WebSocket(wsUrl);
    return $scope.socket.onmessage = function(msg) {
      return $scope.$apply(function() {
        console.log("received : " + msg);
        return $scope.time = JSON.parse(msg.data).data;
      });
    };
  };
  $scope.start = function() {
    return $http.get(jsRoutes.controllers.AppController.start().url).success(function() {});
  };
  $scope.stop = function() {
    return $http.get(jsRoutes.controllers.AppController.stop().url).success(function() {});
  };
  return startWS();
});

window.angular.module('app', ['utils']);

addZero = function(value) {
  value = Math.floor(value);
  if (value < 10) {
    return "0" + value;
  } else {
    return value;
  }
};

toHours = function(time) {
  return addZero(time / (1000 * 60 * 60));
};

toMinutes = function(time) {
  return addZero((time / (1000 * 60)) % 60);
};

toSeconds = function(time) {
  return addZero((time / 1000) % 60);
};