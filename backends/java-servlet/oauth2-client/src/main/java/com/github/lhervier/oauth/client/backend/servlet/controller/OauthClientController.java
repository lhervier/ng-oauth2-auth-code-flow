package com.github.lhervier.oauth.client.backend.servlet.controller;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.lhervier.oauth.client.backend.servlet.ex.AuthorizeException;
import com.github.lhervier.oauth.client.backend.servlet.ex.GrantException;
import com.github.lhervier.oauth.client.backend.servlet.model.AuthorizeError;
import com.github.lhervier.oauth.client.backend.servlet.model.GrantError;
import com.github.lhervier.oauth.client.backend.servlet.model.GrantResponse;
import com.github.lhervier.oauth.client.backend.servlet.service.TokenService;
import com.github.lhervier.oauth.client.backend.servlet.utils.Utils;

@RestController
@RequestMapping(value = "/")
public class OauthClientController {

	@Value("${oauth2.client.endpoints.authorize.url}")
	private String authorizeUrl;

	@Value("${oauth2.client.endpoints.authorize.accessType}")
	private String authorizeAccessType;
	
	@Value("${oauth2.client.endpoints.token.url}")
	private String tokenUrl;
	
	@Value("${oauth2.client.endpoints.token.authMode}")
	private String tokenAuthMode;
	
	@Value("${oauth2.client.clientId}")
	private String clientId;

	@Value("${oauth2.client.secret}")
	private String secret;

	@Value("${oauth2.client.redirectUri}")
	private String redirectUri;
	
	@Value("${oauth2.client.responseType}")
	private String responseType;
	
	@Value("${oauth2.client.scope}")
	private String scope;

	@Autowired
	private TokenService tokenSvc;

	private RestTemplate template = new RestTemplate();

	// =========================================================================
	
	/**
	 * "/init" endpoint
	 * @throws MalformedURLException 
	 */
	@RequestMapping(value = "/init", method = RequestMethod.GET, produces = "application/json")
	public ModelAndView init(
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "error_description", required = false) String errorDescription,
			@RequestParam(value = "error_uri", required = false) String errorUri,
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "redirect_url", required = false) String redirectUrl) throws AuthorizeException, GrantException {

		// Error parameter => Throw error
		if (!StringUtils.isEmpty(error)) {
			AuthorizeError authError = new AuthorizeError();
			authError.setError(error);
			authError.setErrorDescription(errorDescription);
			authError.setErrorUri(errorUri);
			authError.setState(state);
			throw new AuthorizeException(authError);
		}

		// Authorization code => Get the tokens
		if (!StringUtils.isEmpty(code)) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			if( "basic".equals(this.tokenAuthMode) )
				headers.add("Authorization", "Basic " + this.secret);
			
			MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
			map.add("grant_type", "authorization_code");
			if( "none".equals(this.tokenAuthMode) || "queryString".equals(this.tokenAuthMode) )
				map.add("client_id", this.clientId);
			if( "queryString".equals(this.tokenAuthMode) )
				map.add("client_secret", this.secret);
			map.add("redirect_uri", this.redirectUri);
			map.add("code", code);
			
			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
			
			ResponseEntity<GrantResponse> entity = this.template.postForEntity(this.tokenUrl, request, GrantResponse.class);
			GrantResponse resp = entity.getBody();
			if( entity.getStatusCode() == HttpStatus.OK ) {
				if( !Utils.checkIdTokenNonce(resp.getIdToken(), this.tokenSvc.getSessionId()) ) {
					GrantError e = new GrantError();
					e.setError("invalid_id_token");
					throw new GrantException(e);
				}
				this.tokenSvc.setAccessToken(resp.getAccessToken());
				this.tokenSvc.setRefreshToken(resp.getRefreshToken());
				this.tokenSvc.setIdToken(resp.getIdToken());
				this.tokenSvc.setTokenType(resp.getTokenType());
				
				return new ModelAndView("redirect:" + state);
			} else
				throw new GrantException(resp);
		}
		
		// Otherwise, redirect to authorize endpoint
		try {
			URL authorize = new URL(this.authorizeUrl);
			UriComponentsBuilder builder = UriComponentsBuilder
	                .newInstance()
	                .scheme(authorize.getProtocol())
	                .host(authorize.getHost())
	                .port(Integer.toString(authorize.getPort()))
	                .path(authorize.getPath())
	                .queryParam("response_type", this.responseType)
	                .queryParam("client_id", Utils.urlEncode(this.clientId))
	                .queryParam("redirect_uri", Utils.urlEncode(this.redirectUri))
	                .queryParam("scope", this.scope)
	                .queryParam("state", Utils.urlEncode(redirectUrl))
	                .queryParam("nonce", this.tokenSvc.getSessionId());
			if( !StringUtils.isEmpty(this.authorizeAccessType) )
				builder.queryParam("access_type", this.authorizeAccessType);
	        
	        return new ModelAndView("redirect:" + builder.build().toString());
		} catch(MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	// =========================================================================
	
	public static class TokensEndpointResponse {
		@JsonProperty("access_token")
		private String accessToken;
		@JsonProperty("token_type")
		private String tokenType;
		@JsonProperty("id_token")
		private String idToken;
		public String getAccessToken() { return accessToken; }
		public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
		public String getTokenType() { return tokenType; }
		public void setTokenType(String tokenType) { this.tokenType = tokenType; }
		public String getIdToken() { return idToken; }
		public void setIdToken(String idToken) { this.idToken = idToken; }
	}
	
	/**
	 * "/tokens" end point
	 */
	@RequestMapping(value = "/tokens", method = RequestMethod.GET, produces = "application/json")
	public TokensEndpointResponse tokens() {
		TokensEndpointResponse resp = new TokensEndpointResponse();
		resp.setAccessToken(this.tokenSvc.getAccessToken());
		resp.setTokenType(this.tokenSvc.getTokenType());
		resp.setIdToken(this.tokenSvc.getIdToken());
		return resp;
	}
	
	// =========================================================================
	
	/**
	 * "/refresh" endpoint
	 */
	@RequestMapping(value = "/refresh", method = RequestMethod.GET, produces = "application/json")
	public TokensEndpointResponse refresh() throws GrantException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		if( !StringUtils.isEmpty(this.secret) )
			headers.add("Authorization", "Basic " + this.secret);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("grant_type", "refresh_token");
		map.add("refresh_token", this.tokenSvc.getRefreshToken());
		
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		
		ResponseEntity<GrantResponse> entity = this.template.postForEntity(this.tokenUrl, request, GrantResponse.class);
		GrantResponse resp = entity.getBody();
		if( entity.getStatusCode() == HttpStatus.OK ) {
			this.tokenSvc.setAccessToken(resp.getAccessToken());
			this.tokenSvc.setRefreshToken(resp.getRefreshToken());
			this.tokenSvc.setIdToken(resp.getIdToken());
			return tokens();
		} else
			throw new GrantException(resp);
	}
}
