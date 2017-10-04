This AngularJS module implements a set of interceptors that will add the needed OAUTH2 access token to the "Authorization Bearer" headers of every requests sent to an absolute URL (using $http).

It will extract the OAUTH2 access token from an OAUTH2 Authorization Server using the __Authorization Code Flow__. Of course, to do so, it needs a server backend... Read on.

FIXME: This module is not installable with bower... You have to copy the js file manually into your project.

# Background : OAUTH2

OAUTH2 allows you to write __client applications__ that are able to access a set of protected __resources__ hosted on a __resource server__.
In real life, most of the __client applications__ are web or mobile applications, and most __resources__ are Rest APIs.
Have a look at https://tools.ietf.org/html/rfc6749 for more details.

The important thing is that, with OAUTH2, the __resource server__ (the server that hosts the __resources__) will be able to CLEARLY identify the calling user. 

For this, the __client application__ will get an __access token__ from an OAUTH2 __Authorization Server__ (for example, a Microsoft ADFS), and will send this token 
with every requests made to the __resource server__. A common way of sending the __access token__ is to add it to the HTTP header named "Authorization", with the "Bearer" prefix.

Event though OAUTH2 RFC only says that the __access token__ is a string, technically speaking, it is often a JWT : Simply said, it is a JSON string associated with a digital signature. 
See https://jwt.io for more details. 

And because of this digital signature, the __resource server__ will be able to validate the token : 

- If the __access token__ is signed using a public/private key algorithm (RSA), you will have to configure your __resource server__ so it knows the public key. 
- And if the __access token__ is signed using a symetric algorithm (HMAC), you will have to configure it so it knows the secret that the __authorization server__ has used to generate the token.

# Background again : The OAUTH2 flows

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

The access token have a short life time. So, there is no problem to send it to the user agent. As usual, you will be able to add it to the "Authorization" header of your ajax requests.

But the refresh token have a long life time. It is then considered insecure to send it to the user agent. The OAUTH2 RFC even uses the "MUST NOT" keyword.
This is the reason why you will have to store it in a secured place : A server.

This backend server will receive the authorization code, and will send it to the OAUTH2 Authorization Server (to its "/token" endpoint) to obtain the two tokens (access and refresh). This is server to server communication.

As we already said, the access token can then be sent to the front end. It can also be used by backend code to send Rest queries "in the name of the user". Pretty usefull.

But the refresh token __MUST__ be kept server side.

When your client application sends a Rest request that contains the access token (again, in the "Authorization" http header), and the Rest server answers with a HTTP 403 error, then it's time for refresh.
With Implicit Flow, you will have to ask the user to enter its credentials again. But with the authorization Code Flow, you have a refresh token. 
Making your server backend code send the refresh token to the authorization server (to its "token" endpoint) will allow you to obtain two brand new tokens (a new refresh token and a new access token).
Of course, if the refresh token has itself expired, you will only get an error. This time, this will oblige you to ask the user to enter its credentials again.

So, in conclusion, with the Authorization Code Flow, you can have a refresh token that can live for a loooong time. 
And because it stays server side, you won't have to ask the user to enter its credentials again and again.

You can also have an access token that lives only for a few minutes. When it expires, it is almost free to obtain a new one. 
So, this is considered more secure because if the user agent is compromised, the token will expire in a short time.

Of course, the main difficulty with this flow is that you will have to implement a backend on an server.

# The server backend

To use this angularJS module, you will have to implement three endpoints :

- A endpoint that will initialize the oauth2 dance.
- A endpoint that will return the access token (and an id token if your oauth2 authorization server is openid compliant).
- And a endpoint that will refresh the current access token (and refresh token).

## The "/init" end point

This endpoint will initialize the OAUTH2 dance. Note that when registering your application your client application, you will have to define the URI of this endpoint as the redirectUri.
This is VERY important.

There is three ways of calling this endpoint.

### When called with a "redirect_url" parameter (note that it is "redirect_url" and not "redirect_uri" !!!)

The redirect_url parameter MUST contain the url that will be loaded by the browser once the tokens have been stored server side (once the OAUTH2 dance is over).

This endpoint MUST redirect to the OAUTH2 Authorization Server "/authorize" endpoint. 
To do so, it must define a "response_type" parameter compatible with the authorization code flow (like "code" or "code+idtoken"), along with other "/authorize" endpoint parameters, like the clientId, the scope, etc...

It MUST also send the value of the "redirect_url" parameter in the "state" parameter. 

### When called with a "code" parameter

Once the user is authorized, the OAUTH2 Authorization Server will redirect the user agent to the client application redirect URI. And remember that this URI MUST point to our "/init" endpoint.
As such, the authorization server will add the "code" parameter which will contain the authorization code.

In this case, the endpoint MUST process the authorization code, and send it to the authorization server "/token" endpoint (this is server to server communication) 
to obtain the access token and the refresh token (and an id token if the server is openid compliant). The tokens must then be stored server side (in the session object for example).

Once done, our endpoint MUST redirect to the url present in the "state" parameter. Remember ? It contains our initial redirect_url.

### When called with an "error" parameter

Such a parameter can be set by the oauth2 "/authorize" end point if the user cannot be authorized.

Do whatever you want in this case. The actual implementations simply displays the error. 

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
Again, this is server to server communication.

Once done, its answer must be the same as the answer of the "/tokens" endpoint.

# Implementations

A set of implementations of those endpoints are provided with this project :

- A Java webapp that can be ran on any Tomcat server [README](backends/java-servlet/README.md)
- A IBM Domino Osgi plugin that exposes the endpoints as servlets [README](backends/domino-osgi/README.md)

Each have their own README.md file, so, just have a look.

## The awaited properties

Each implementation needs to be configured with the following properties :

- oauth2.client.endpoints.authorize.url = *URL of your OAuth2 Authorization Server /authorize endpoint*
- oauth2.client.endpoints.authorize.accessType = *When using Google Clous OAUTH2, set this value to 'offline' if you want a refresh token*
- oauth2.client.endpoints.token.url = *URL of your OAuth2 Authorization Server /token endpoint*
- oauth2.client.endpoints.token.authMode = *One of "basic"/"queryString"/"none". This is the way the secret will be passed to the token endpoint.*
	- "basic" : It will besent to the "Authorization Basic" header. The client_id will NOT be passed in the query string.
	- "queryString" : It will be passed along the "client_id" in the query string, in the "client_secret" parameter.
	- "none" : Only the "client_id" will be passed in the query string.
- oauth2.client.responseType = *The OAUTH2 authorize response type. Must be compatible with the authorization code flow (or openid hybrid flow). In doubt, set it to "code+id_token".*
- oauth2.client.scope = *The OAUTH2 scope value. You can leave it empty, or set it to "openid" if you want to extract an id token.*
- oauth2.client.clientId = *Your oauth2 client application id*
- oauth2.client.secret = *Your oauth2 client application secret*
- oauth2.client.redirectURI = *URL used by the users to access your application. Must be coherent with what's configured in your OAUTH2 application.*

## Values of the properties when using Google Cloud 

To plug the sample apps into a google cloud environment, it's pretty easy.

The endpoints are available at this URL :

	https://accounts.google.com/.well-known/openid-configuration

You will find the authorize and the token endpoint in the JSON.

The access type must be set to "offline" if you wan a refresh token. This is not present in OAUTH2 RFC...

The token authMode must be set to "queryString", as described in this document (Go to Step 2.0) :

	https://developers.google.com/identity/protocols/OAuth2WebServer

The responseType must be set to "code", and the scope must be set to "openid" if you want to be able to validate the provided access token.
	
Then, to declare your application, go to the google cloud console : 

	https://console.developers.google.com
	
In the "credentials" part, click the "create credentials" button, and select "OAuth client Id". Choose "Web application", enter a name and your __redirect uri__ (the url of your local "/init" endpoint). 
Click "create", and Google will display your __client id__ and __client secret__.

Then, you can update the sample app (app.js file) to make it access the openid userInfo endpoint (which is a good example of an API that works with an bearer access token). 
You will find it in the JSON, alongside the other endpoints urls.

## Values of the properties when using Facebook 

TBD

## Values of the properties when using LinkedIn

TBD

## Values of the properties when using GitHub

TBD

## Values of the properties when using Microsoft Azure AD

TBD

## Values of the properties when using a local Microsoft ADFS

TBD

## Values of the properties when using Domino OAUTH2 Authorization Server

TBD

# How to use the angular module

Once you have your three endpoints, and your backends are configured properly, you are ready to go.

Include the javascript file with the other modules in your HTML template. In a near future, it should be available with bower.

Here is a sample Controller example that uses the following (imaginary) environment :

- A Rest API, configured to validate the access token, is available at "http://apis.acme.com/myRestApi"
- Note that the "callRestApi" method uses a normal $resource call. The only specific code is to make it react when the refresh token has expired. And it could have been handled by another interceptor, generic to the application.
- Note the use of the "init" method to which you will have to send the url of your three endpoints.
	- "/init" endpoint is located at the relative url "oauth2-client/init"
	- "/tokens" endpoint is located at the relative url "oauth2-client/tokens"
	- and "/refresh" endpoint is located at the relative url "oauth2-client/refresh"

	
```html
<!DOCTYPE html>
<html ng-app="sampleApp">
<head>
  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.6.4/angular.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.6.4/angular-resource.js"></script>
  <script src="app/ngOauth2AuthCodeFlow.js"></script>
  <script src="app/app.js"></script>
</head>
<body ng-controller="SampleController as ctrl">
  <div ng-show="ctrl.message != null">{{ctrl.message}}</div>
  <div ng-show="ctrl.reconnectUrl != null">
    <a ng-href="{{ctrl.reconnectUrl}}">Click here to reconnect</a>
  </div>
  <hr/>
  <div ng-show="ctrl.accessToken != null">
    <div>Access Token</div>
    <div>{{ctrl.accessToken}}</div>
  </div>
  <hr/>

  <div ng-show="ctrl.jsonResponse != null">
    <div>Json Response</div>
    <div>{{ctrl.jsonResponse}}</div>
  </div>
  <hr/>

  <button ng-click="ctrl.callRestApi()">
    Call protected Rest API
  </button>
</body>
</html>
```

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
```
