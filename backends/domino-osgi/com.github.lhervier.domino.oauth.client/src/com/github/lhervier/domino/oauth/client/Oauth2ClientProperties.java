package com.github.lhervier.domino.oauth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Oauth2ClientProperties {

	/**
	 * The authorize url
	 */
	@Value("${oauth2.client.endpoints.authorize.url:@null}")
	private String authorizeUrl;
	
	/**
	 * The authorize access type (specific to google cloud)
	 */
	@Value("${oauth2.client.endpoints.authorize.accessType:@null}")
	private String authorizeAccessType;
	
	/**
	 * The authorize prompt (sepcific to Google Cloud)
	 */
	@Value("${oauth2.client.endpoints.authorize.prompt:@null}")
	private String authorizePrompt;
	
	/**
	 * The token URL
	 */
	@Value("${oauth2.client.endpoints.token.url:@null}")
	private String tokenUrl;
	
	/**
	 * The token auth mode
	 */
	@Value("${oauth2.client.endpoints.token.authMode:@null}")
	private String tokenAuthMode;
	
	/**
	 * The client Id
	 */
	@Value("${oauth2.client.clientId:@null}")
	private String clientId;
	
	/**
	 * The secret
	 */
	@Value("${oauth2.client.secret:@null}")
	private String secret;
	
	/**
	 * The redirect uri
	 */
	@Value("${oauth2.client.redirectUri:@null}")
	private String redirectUri;
	
	/**
	 * The response type
	 */
	@Value("${oauth2.client.responseType:@null}")
	private String responseType;
	
	/**
	 * The scope
	 */
	@Value("${oauth2.client.scope:@null}")
	private String scope;
	
	/**
	 * Disable host verifier ?
	 */
	@Value("${oauth2.client.disableHostVerifier:false}")
	private boolean disableHostVerifier;

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @return the authorizeUrl
	 */
	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	/**
	 * @param authorizeUrl the authorizeUrl to set
	 */
	public void setAuthorizeUrl(String authorizeUrl) {
		this.authorizeUrl = authorizeUrl;
	}

	/**
	 * @return the tokenUrl
	 */
	public String getTokenUrl() {
		return tokenUrl;
	}

	/**
	 * @param tokenUrl the tokenUrl to set
	 */
	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}

	/**
	 * @return the tokenAuthMode
	 */
	public String getTokenAuthMode() {
		return tokenAuthMode;
	}

	/**
	 * @param tokenAuthMode the tokenAuthMode to set
	 */
	public void setTokenAuthMode(String tokenAuthMode) {
		this.tokenAuthMode = tokenAuthMode;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the secret
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * @param secret the secret to set
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * @return the redirectUri
	 */
	public String getRedirectUri() {
		return redirectUri;
	}

	/**
	 * @param redirectUri the redirectUri to set
	 */
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	/**
	 * @return the responseType
	 */
	public String getResponseType() {
		return responseType;
	}

	/**
	 * @param responseType the responseType to set
	 */
	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	/**
	 * @return the scope
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * @param scope the scope to set
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * @return the disableHostVerifier
	 */
	public boolean isDisableHostVerifier() {
		return disableHostVerifier;
	}

	/**
	 * @param disableHostVerifier the disableHostVerifier to set
	 */
	public void setDisableHostVerifier(boolean disableHostVerifier) {
		this.disableHostVerifier = disableHostVerifier;
	}

	/**
	 * @return the authorizeAccessType
	 */
	public String getAuthorizeAccessType() {
		return authorizeAccessType;
	}

	/**
	 * @param authorizeAccessType the authorizeAccessType to set
	 */
	public void setAuthorizeAccessType(String accessType) {
		this.authorizeAccessType = accessType;
	}

	/**
	 * @return the authorizePrompt
	 */
	public String getAuthorizePrompt() {
		return authorizePrompt;
	}

	/**
	 * @param authorizePrompt the authorizePrompt to set
	 */
	public void setAuthorizePrompt(String authorizePrompt) {
		this.authorizePrompt = authorizePrompt;
	}
	
}
