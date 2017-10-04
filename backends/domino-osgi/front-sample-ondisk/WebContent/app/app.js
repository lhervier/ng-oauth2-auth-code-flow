var sampleApp = angular.module('sampleApp', ['ngOauth2AuthCodeFlow', 'ngResource']);

sampleApp.controller(
    'SampleController', 
    ['$rootScope', '$resource', '$window', 'Oauth2AuthCodeFlowService', 
    function($rootScope, $resource, $window, Oauth2AuthCodeFlowService) {
  var ths = this;
  
  // A message to display to the user
  this.message = null;
  // To display as a link to the user if it needs to reconnect.
  this.reconnectUrl = null;
  // The json response of the Rest API.
  this.jsonResponse = null;
  // The access token, in case you want to play with it (note that you DON'T have to)
  this.accessToken = null;
  
  this.callRestApi = function() {
    // Interceptor will add the "Authorization Bearer" header for us !
    $resource('http://apis.acme.com/myRestApi').get(
        function(jsonResponse) {
          ths.jsonResponse = JSON.stringify(jsonResponse);
        },
        function(reason) {
          // User needs to reconnect. Refresh token is expired... 
          // This case can be handled application wide by a custom interceptor.
          if( reason.code == "oauth2.needs_reconnect" )
            ths.reconnectUrl = reason.reconnectUrl;
          else
            ths.message = "Error calling rest api : " + reason;
        }
    );
  };
  
  // Initialization of the OAUTH2 dance
  Oauth2AuthCodeFlowService.init('oauth2-client/init', 'oauth2-client/tokens', 'oauth2-client/refresh').then(
      function(result) {
        // This is just for fun. We don't need the access token. 
        // The interceptor handle it for you.
        ths.accessToken = result.access_token;  
      },
      function(reason) {
        // If reconnect is needed at startup, handle it the way you want 
        // (display a message, or - in our case - redirect the browser)
        if( reason.code == "oauth2.needs_reconnect" )
          $window.location = reason.reconnectUrl;
        else
          ths.message = "Error initializing OAUTH2 dance";
      }
  );
}]);
