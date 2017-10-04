This is an implementation for the endpoints needed by the AngularJS "ngOauth2AuthCodeFlow" module based on a java webapp.

The endpoints will be as following :

- "/init" endpoint : http://youserver/oauth2-client/init
- "/tokens" endpoint : http://youserver/oauth2-client/tokens
- "/refresh" endpoint : http://youserver/oauth2-client/refresh

These values must be used when initializing the Oauth2AuthCodeFlowService.

The installation require multiple steps :

- Deploy an instance of the oauth2-client webapp per client application.
- Create a front application (on the same host) that will use the endpoints (and the NgOauth2AuthCodeFlow module).

# Deploy the oauth2-client webapp

This application implements all the needed endpoints.

## Get the war file

You can get the war file from the github releases, or generate it yourself from the source code (it is a simple maven project).

## Prepare the environment: The Spring Properties

Before deploying it, you will have to configure it. The code is using Spring Boot, so you will have to define a set of properties :

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

This values can be defined as simple Environment Variables at the OS level. Or, if deploying into tomcat, you can add context.xml file in the configuration. This is my prefered way :

- Create a file <webappname>.xml in the conf/Catalina/<engine name>/ folder of your tomcat installation folder.
- Declare the spring properties in JSON format :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>
	<Environment
			name="spring.application.json"
			type="java.lang.String"
			override="false"
			value='{
				"oauth2": {
					"client": {
						"endpoints": {
							"authorize": {
								"url" : "URL of your OAuth2 Authorization Server /authorize endpoint",
								"accessType" : "When using Google Clous OAUTH2, set this value to 'offline' if you want a refresh token"
							},
							"token": {
								"url" : "URL of your OAuth2 Authorization Server /token endpoint",
								"authMode" : "basic/queryString/none"
							}
						},
						"clientId": "Your oauth2 client application id",
						"secret": "Your oauth2 client application secret",
						"redirectUri": "URL used by the users to access your application. Must be coherent with what's configured in your OAUTH2 application.",
						"responseType: "The OAUTH2 authorize response type. Must be compatible with the authorization code flow (or openid hybrid flow). In doubt, set it to 'code+id_token'.",
						"scope": "The OAUTH2 scope value. You can leave it empty, or set it to 'openid' if you want to extract an id token."
					}
				}
			}'/>
</Context>
```

## Deploy the webapp

For tomcat, just copy the war file into the webapps folder, and let tomcat deploy it automatically.

# Create a client webapp

Once the oauth2-client webapp is deployed, you can generate your own application. 

Have a look at the front-sample project for more details.
