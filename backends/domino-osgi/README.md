This is an implementation for IBM Domino of the endpoints needed by the AngularJS "ngOauth2AuthCodeFlow" module.
Note that the endpoints are implemented as standard Spring controllers, thanks to the [domino-spring](https://github.com/lhervier/dom-spring) project.

The endpoints will be as following :

- "/init" endpoint : http://youserver/anydb.nsf/oauth2-client/init
- "/tokens" endpoint : http://youserver/anydb.nsf/oauth2-client/tokens
- "/refresh" endpoint : http://youserver/anydb.nsf/oauth2-client/refresh

These values must be used when initializing the Oauth2AuthCodeFlowService.

The installation require multiple steps :

- Install the domino-spring plugins on your server
- Install the ngOauth2AuthCodeFlow endpoints plugins on your server
- Declare the needed spring properties : Add a "Oauth2Params" view that contains a single document with a set of fields whose name correspond to the needed properties.
- Run...

# Installing the domino-spring osgi plugins

## Installing on the server

Download the latest release (1.2 as of writing this document) of the "Domino Spring" project from https://github.com/lhervier/dom-spring/releases. 
It is a simple zip file that contains osgi plugins.

Unzip the update site. Then, create an "update site" database :

- Name it the way you want. In this example, I will name it "SpringUpdateSite.nsf" and store it at the root of my Domino server.
- Use the "Eclipse Update Site" template (you will have to select a server, and click "show advanced templates" in the new database dialog box)
- Click on the "Import local update site" button
- Go find the "site.xml" that is present into your unzipped update site.
- It is recommended to disable the "Spring Sample Feature"
- Declare the name of the new database in your notes.ini, using the variable "OSGI_HTTP_DYNAMIC_BUNDLES". If it already exist, separate multiple values with a "," character.
- Restart the http task with a "restart task http" console command.

Once http has been restarted, you can check that the plugins have been loaded successfully by using the following console command :

	tell http osgi ss spring
	
If it answers something, you're good to go.

## Optional : Install plugins on your Domino Designer

Do this only if you plan to play with the code of the osgi plugin... Otherwise, skip this chapter.

- Check that your Designer allows you to add plugins :
	- Go to File / Preferences
	- Go to the "Domino Designer" section
	- Check that "Enable Eclipse Plugin install" is checked.
- Go to File / Application / Install
- Choose "Search for new feature to install"
- Add a "Zip/Jar Location", and go select the update site zip file
- Click "Finish" and accept the next steps.

Once Domino Designer has restarted, you can check that the plugins have been installed by going to Help / About Domino Designer. Click the "Plugin details" button, and
check that you can see the "com.github.lhervier.domino.oauth.client.*" plugins (sorting by plugin id make it easier to find).

# Install the domino backend plugins

## Get the update site

You can download it from the github releases. It is a simple zip file.

Otherwise, you can generate it yourself from IBM Domino Designer :

- Import the code into Designer
	- Clone or download the source code from github into a local folder.
	- Open the "package explorer" view.
	- Use the "File / Import" menu.
	- In the "General" section, select "Existing projets into workspace"
	- Click "Browse" and select the folder that contains this project's sources.
	- Select all projects and click "Import"
- Then, compile the code :
	- Open the file "site.xml" in the "com.github.lhervier.domino.oauth.client.update" project.
	- Click the "Build All" button.

The result is in the "com.github.lhervier.domino.oauth.client.update" folder. This is a "standard" update site composed of :

- the "site.xml" file
- the "plugins" folder
- and the "features" folder

Then, package the update site : Zip the site.xml, plugins and features folders, and you are ready !

## Install the plugins on the server

The procedure is the same as when we installed the domino-spring plugins.

Unzip the update site. Then, create an "update site" database :

- Name it the way you want. In this example, I will name it "OAuth2ClientUpdateSite.nsf" and store it at the root of my Domino server.
- Use the "Eclipse Update Site" template (you will have to select a server, and click "show advanced templates" in the new database dialog box)
- Click on the "Import local update site" button
- Go find the "site.xml" that is present into your unzipped update site.
- It is recommended to disable the "Spring Sample Feature"
- Declare the name of the new database in your notes.ini, using the variable "OSGI_HTTP_DYNAMIC_BUNDLES". If it already exist, separate multiple values with a "," character.
- Restart the http task with a "restart task http" console command.

Once http has been restarted, you can check that the plugins have been loaded successfully by using the following console command :

	tell http osgi ss oauth.client
	
If it answers something, you're good to go.

# Creating a database that is "ngOauth2AuthCodeFlow" ready

Such databases are not using XPages, or good old "Forms and Views" to generate the user interface. 
Here, we are talking about databases that contains :

- html/css/js files, preferably stored in the WebContent folder accessible from the package explorer.
- The client AngularJS code will use normal $http and $resource to access the services : 
	- XPages or Agents or whatever you want to publish rest like services.
	- And Rest services configured on other hosts that will wait for bearer tokens in the authorization header.

## Create the NSF

First option : You can download a sample database from the github releases. 

Second option : You can generate the sample database from the source code :

- Open the "package explorer" view
- Right click on the "front-sample-ondisk" project
- Select "Team Development" / "Associate with new NSF"
- Enter the server name and the name of your new database

And third option : You can create a brand new NSF.

### The configuration document :

Your database MUST have a view named "Oauth2Params". And this view MUST contain a single document. The list of fields present in the document will be described later (in the configuration part of this chapter).
If this is not the case, the endpoints will respond with a http 404 error.

For security reason, it is recommanded to protect this document with a Reader field. The only persons who will access this document are :

- The administrator that will create the document
- And the server itself (the osgi code that will read the values will use a notes session opened as the server itself).

In the sample, we are using a [Admin] role in the ACL and in a reader field.

The easiest way to create this document is to copy/paste the "Oauth2Params" view and the "Oauth2Param" form from the sample database.

### ACL warning

With OAuth2, the browser will load the web interface, detect that it does not have an access token, and then redirect to the authorize endpoint of your authorization server (ADFS for example). 
The user will log in, after which the authorization server will redirect back again the browser to the original page.

In conclusion : If your database is protected by an ACL, the user will have to authenticate twice :

- The first time when accessing the database : Because of Domino ACL, you will have the Domino login screen.
- And again when opening a session on the OAUTH2 authorization server (Your Microsoft ADFS for example) : You will have the ADFS login screen (except if you configured SSO).

For this reason, it is recommended to set Anonymous to reader in the ACL of the database that host the front end code. Then, write your Domino hosted Rest Services using domino-spring, and
protect them with the access token. The code of the openid userInfo endpoint implemented in the [dom-auth-server](https://github.com/lhervier/dom-auth-server) project is a good starting 
point (look at the BearerSession spring bean).

TODO: I will add later a github repository that contains example of Osgi/Domino and Java/Tomcat hosted sample rest services protected by an bearer token.

### Other design elements

Using the package explorer, you can create a set of HTML/JS/CSS files in the WebContent folder.

Don't forget to include the "NgOauth2AuthorizationCodeFlow" module. 

Have a look at the sample database. You will probably have to adapte the URL of the external Rest API.

## Configure your database

In the Oauth2Params view, you will have to create a single document with the following fields (which corresponds to spring properties) :

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

Again, the easiest way is to use the OAuth2Param form available in the sample database.

## Test

Try accessing 

	http://youserver/yourdb.nsf/index.html

You will be redirected to your OAUTH2 authorization server login page, and - after logging - redirected back to your application.
Clicking the button will call the external rest service and display its json response inthe HTML.

Note that the application also displays the access token. This is just for demonstration purpose.