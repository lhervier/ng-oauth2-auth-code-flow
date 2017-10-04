This AngularJS module implements a set of interceptors that will add the needed OAUTH2 access token to the "Authorization Bearer" headers to every requests sent to an absolute URL (using $http).

It will extract the OAUTH2 access token from an OAUTH2 Authorization server using the __Authorization Code Flow__. Of course, to do so, it needs a server backend... Read on.

# OAUTH2

OAUTH2 allows you to write __client applications__ that are able to access a protected set of __resources__ hosted on a __resource server__.
In real life, most of the __client applications__ are web or mobile applications, and most __resources__ are Rest APIs.
Have a look at https://tools.ietf.org/html/rfc6749 for more details.

The important thing is that, with OAUTH2, the __resource server__ (the server that hosts the __resources__) will be able to CLEARLY identify the calling user. 

For this, the __client application__ will get an __access token__ from an OAUTH2 __authorization server__ (for example, a Microsoft ADFS), and will send this token 
with every requests made to the __resource server__. A common way of sending the __access token__ is to add it to the HTTP header named "Authorization", with the "Bearer" prefix.

Event though OAUTH2 RFC only says that the __access token__ is a string, technically speaking, it is often a JWT : Simply said, it is a JSON string associated with a digital signature. 
See https://jwt.io for more details. 

If the __access token__ is signed using a public/private key algorithm (RSA), you will have to configure your __resource server__ so it knows the public key. And if the __access token__ is signed
using a symetric algorithm (HMAC), you will have to configure it so it knows the secret that the __authorization server__ has used to generate the token.

# The flows

OAUTH2 gives us multiple methods (multiple __flows__) to send the access token to the client application. 

## Implicit flow 

You can find a lot of different implementations of angular modules that works with the "Implicit Flow". 
It is widely used for mobile apps, and does not oblige you to implement a server backend for your application.

The problem with this flow is that you will get an access token that will be valid only for a (short or not) period of time. 
If it expires, your application will have to ask the user to enter their credentials again. And - of course - if you don't want to ask for the credentials often, 
it will have to expire as late as possible. 

This is why this flow is not considered as the most secure.

## Authorization Code flow

With the "Authorization Code flow", your application will only receive an __Authorization Code__ after the user enter its credential. You will have to exchange this __authorization code__
with an access token (short life time), and a refresh token (long life time). 

The access token have a short life time. So, there is no problem to send it to the user agent. You will be able to add it to the "Authorization" header of your ajax requests.

But the refresh token have a long life time. It is then considered insecure to send it to the user agent. The OAUTH2 RFC even uses the "MUST NOT" keyword.
This is the reason why you will have to implement a server backend.

This backend will receive the authorization code, and will send it to the OAUTH2 Authorization Server (to its "/token" endpoint) to obtain the two tokens (access and refresh). This is server to server communication.

As we already said, the access token can then be sent to the front end. It can also be used by backend code to send Rest queries "in the name of the user". Pretty usefull.

But the refresh token __MUST__ be kept server side.

When your client application sends a Rest request that contains the access token (again, in the "Authorization" http header), and the Rest server answers with a HTTP 403 error, then it's time for refresh.
With Implicit Flow, you will have to ask the user to enter its credentials again. But with the authorization Code Flow, you will have a refresh token. 
Making your server backend code send the refresh token to the authorization server (to its "token" endpoint) will allow you to obtain two brand new tokens (a new refresh token and a new access token).
Of course, if the refresh token itself has expired, you will only get an error. This time, this will oblige you to ask the user to enter its credentials again.

So, in conclusion, with the Authorization Code Flow, you can have a refresh token that can live for multiple days. 
And because it stays server side, you won't have to ask the user to enter its credentials again and again.

You can also have an access token that lives only for a few minutes. When it expires, it is almost free to obtain a new one. 
So, this is considered more secure because if the user agent is compromised, the token will expire in a short time.

Of course, the main difficulty with this flow is that you will have to implement a backend on an server.

# The server backend

To use this angularJS module, you will have to implement three end points :

- A endpoint that will initialize the oauth2 dance.
- A endpoint that will return the access token (and an id token if your oauth2 authorization server is openid compliant).
- And a endpoint that will refresh the current access token (and refresh token).

## The "/init" end point

This endpoint MUST be defined as your oauth2 client application redirectURI.

### With a "redirect_url" parameter

The redirect_url parameter MUST contain the url that will be loaded by the browser once the tokens have been stored server side (once the OAUTH2 dance is over).

The endpoint MUST redirect to the OAUTH2 Authorization Server "/authorize" endpoint. 
To do so, it must define a "response_type" parameter compatible with the authorization code flow (like "code" or "code+idtoken"), along with other oauth2 information, like the clientId.

It MUST also send the value of the "redirect_url" parameter in the "state" parameter. 

### With a "code" parameter

Once the user is authorized, the OAUTH2 Authorization Server will redirect the user agent to the client application redirect URI. And remember that this URI MUST point to our "/init" endpoint.
As such, the authorization server will add the "code" parameter which will contain the authorization code.

In this case, the endpoint MUST process the authorization code, and send it to the authorization server "/token" end point (this is server to server communication) 
to obtain the access token and the refresh token (and an id token if the server is openid compliant). The tokens must then be stored server side (in the session object for example).

Once done, our endpoint MUST redirect to the url present in the "state" parameter. Remember ? It contains our initial redirect_url.

### With an "error" parameter

Such a parameter can be set by the oauth2 "/authorize" end point if the user cannot be authorized.

Do whatever you want in this case. The samples simply displays the error. 

## The "/tokens" end point

This endpoint does not accept any parameter, and must return a JSON object that contains the tokens (access token, and id token). If no access token is available, it must return an empty JSON object.

Here is an example response :

	{
		"access_token": "<the access token>",
		"token_type": "bearer",
		"id_token": "<an JWT that contains all the openid id token claims>"
	}

Note that the refresh token is NOT present !

## The "/refresh" end point

This endpoint does not accept any parameter, and must use the refresh token (stored server side) to ask the oauth2 authorization server "/token" endpoint for a new set of tokens.
This is server to server communication.

Once done, its answer must be the same as the answer of the "/tokens" endpoint.

## Sample implementations

Two sample implementations of those endpoints are provided with this project :

- A Java webapp that can be ran on any Tomcat server.
- A IBM Domino Osgi plugin that exposes the endpoints as servlets.

Each have their own README.md file, so, just have a look.

# How to use the angular module

Once you have your three endpoints, you are ready to go.

Include the javascript file with the other modules inyour HTML template. 

Here is a sample Controller example :

- A Rest API, configured to validate the access token, is available at "http://apis.acme.com/myRestApi"
- Note that the "callRestApi" method uses a normal $resource call. The only specific code is to make it react when the refresh token has expired. And it could have been handled by another interceptor.
- Note the use of the "init" method to which you will have to send the url of your three endpoints.
	- "/init" endpoint is located at the relative url "oauth2-client/init"
	- "/tokens" endpoint is located at the relative url "oauth2-client/tokens"
	- and "/refresh" endpoint is located at the relative url "oauth2-client/refresh"


```javascript
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
          ths.jsonResponse = jsonResponse;
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
```
