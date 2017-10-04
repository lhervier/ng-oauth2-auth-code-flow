var sampleApp = angular.module('sampleApp', ['ngOauth2AuthCodeFlow', 'ngResource']);

sampleApp.controller('SampleController', ['$rootScope', '$resource', '$window', 'Oauth2AuthCodeFlowService', function($rootScope, $resource, $window, Oauth2AuthCodeFlowService) {
	var ths = this;
	
	this.alerte = null;
	this.reconnectUrl = null;
	this.userInfo = null;
	this.accessToken = null;
	this.param = null;
	
	this.loadUserInfo = function() {
		$resource(ths.param.userInfoEndPoint).get(
				function(result) {
					ths.userInfo = result;
				},
				function(reason) {
					if( reason.code == "oauth2.needs_reconnect" )
						ths.reconnectUrl = reason.reconnectUrl;
					else
						ths.alerte = "Erreur à la récupération des infos utilisateur : " + reason;
				});
	};
	
	// Charge le paramétrage
	$resource('params').get(
			function(param) {
				ths.param = param;
			},
			function(reason) {
				ths.alerte = "Erreur au chargement des paramètres de l'application : " + reason;
			}
	);
	
	// Initialise la danse oauth2. On force la reconnexion s'il n'est pas en session.
	Oauth2AuthCodeFlowService.init('oauth2-client/init', 'oauth2-client/tokens', 'oauth2-client/refresh').then(
			function(result) {
				ths.accessToken = result.access_token;
			},
			function(reason) {
				if( reason.code == "oauth2.needs_reconnect" )
					$window.location = reason.reconnectUrl;
				else
					ths.alerte = "Erreur à l'initialisation de la danse oauth2";
			}
	);
}]);


