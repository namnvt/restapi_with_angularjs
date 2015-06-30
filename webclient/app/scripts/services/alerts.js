////////
// This sample is published as part of the blog article at www.toptal.com/blog
// Visit www.toptal.com/blog and subscribe to our newsletter to read great posts
////////

'use strict';

/**
 * @ngdoc service
 * @name webclientApp.alerts
 * @description
 * # alerts
 * Service in the webclientApp.
 */
angular.module('webclientApp')
    .factory('alertService', function($timeout) {

      var ALERT_TIMEOUT = 5000;

      function add(type, msg, timeout) {

        if (timeout) {
          $timeout(function(){
            closeAlert(this);
          }, timeout);
        } else {
          $timeout(function(){
            closeAlert(this);
          }, ALERT_TIMEOUT);
        }

        return alerts.push({
          type: type,
          msg: msg,
          close: function() {
            return closeAlert(this);
          }
        });
      }

      function closeAlert(alert) {
        return closeAlertIdx(alerts.indexOf(alert));
      }

      function closeAlertIdx(index) {
        return alerts.splice(index, 1);
      }

      function clear(){
        alerts = [];
      }

      function get() {
        return alerts;
      }

      var service = {
            add: add,
            closeAlert: closeAlert,
            closeAlertIdx: closeAlertIdx,
            clear: clear,
            get: get
          },
          alerts = [];

      return service;
    }
);

