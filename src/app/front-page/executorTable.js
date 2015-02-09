(function () {
  'use strict';

  var m = angular.module('nflowVisApp.frontPage.executorTable', []);

  m.directive('executorTable', function () {
    return {
      restrict: 'E',
      scope: {
        executors: '='
      },
      bindToController: true,
      controller: 'ExecutorTableCtrl as ctrl',
      templateUrl: 'app/front-page/executorTable.html'
    };
  });

  m.controller('ExecutorTableCtrl', function () {
    var vm = this;
    vm.executorClass = executorClass;

    function executorClass(executor) {
      var expires = moment(executor.expires);
      var active = moment(executor.active);
      if (active.add(1, 'days').isBefore(new Date())) {
        return;
      }
      if (expires.isBefore(new Date())) {
        return 'warning';
      }
      return 'success';
    }
  });

})();
