This is an implementation for IBM Domino of the endpoints needed by the AngularJS "ngOauth2AuthCodeFlow" module.
Note that the endpoints are implemented as standard Spring controllers, thanks to the [domino-spring](https://github.com/lhervier/dom-spring) project.

Once installed, the endpoints will be made available on EVERY databases referenced in the "oauth2.client.databases" notes.ini variable :

- "/init" endpoint : http://youserver/db.nsf/oauth2-client/init
- "/tokens" endpoint : http://youserver/db.nsf/oauth2-client/tokens
- "/refresh" endpoint : http://youserver/db.nsf/oauth2-client/refresh

The installation required multiple steps :

- Install the domino-spring plugins on your server
- Install the ngOauth2AuthCodeFlow endpoints plugins on your server
- Create a database with a mandatory view and a mandatory form
- Configure your database
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

Do this only if you plan to play with the code... Otherwise, skip this chapter.

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

Such databases are not using XPages, or good old "Forms and Views" to generate the interface. 
Here, we are talking about databases that contains html/css/js files preferably stored in the WebContent folder (accessible from package explorer), 
and XPages or Agents or whatever you want to publish rest like services.

And of course, you client js code will be using AngularJS and the ngOauth2AuthCodeFlow.

## Generate the NSF

You can download a sample template from the github releases, and create a new database from this template.

### The parameter view :

- Create a view named "Oauth2Params"
- This view must contain only one document that contains a set of fields.

The easier way to create the view and the form is to copy/paste the view names "Oauth2Params" and the form named "Oauth2Param" from the sample database.

### notes.ini declaration :

When the database is created, declare it in the notes.ini of your server :

	oauth2.client.databases=<name of your nsf>

If you have multiple databases, separate the values with a ","

### ACL warning

Also note that the ACL will only connect the user as Anonymous. Otherwise, and because of the oauth2 redirections, the user will have to authenticate twice :

- A first time to access the database (ACL protected)
- And again when opening a session on the OAUTH2 authorization server (Your Microsoft ADFS for example).

### Generate the sample from the code

You can also generate the sample database from the source code using Domino Designer :

- Open the "package explorer" view
- Right click on the "front-sample-ondisk" project
- Select "Team Development" / "Associate with new NSF"
- Enter the server name and the name of your new database

# Configure your database

In the Oauth2Params view, you will have to create a single document with the following fields :

- oauth2.client.clientId = <your oauth2 client application id>
- oauth2.client.secret = <your oauth2 client application secret>
- oauth2.client.baseURI = <URL used by the users to access your NSF. This is parameterized in case of reverse proxies>
- oauth2.client.endpoints.authorize = <URL of your OAuth2 Authorization Server /authorize endpoint>
- oauth2.client.endpoints.token = <URL of your OAuth2 Authorization Server /token endpoint>
- oauth2.client.disableHostVerifier = <Set it to "1" if you have problems with your SSL certificates>

Again, the easiest way is to use the OAuth2Param form available in the sample database.

The sample application needs another configuration document. Go to the "Params" view, and create it using the action button.
You will have to enter the openid userInfo endpoint.

# Test

Try accessing 

	http://youserver/db.nsf/index.html

You will be redirected to your OAUTH2 authorization server login page, and - after logging - back to your application.
Clicking the button will use the openid userInfo endpoint to extract an id token from which we will find the current user name.

Note that the application also displays the access token. This is just for demonstration purpose.