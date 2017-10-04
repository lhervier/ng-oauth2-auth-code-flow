package com.github.lhervier.domino.oauth.client.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class TokensResponse {
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("id_token")
	private String idToken;
	public String getAccessToken() {return accessToken;}
	public void setAccessToken(String accessToken) {this.accessToken = accessToken;}
	public String getIdToken() {return idToken;}
	public void setIdToken(String idToken) {this.idToken = idToken;}
}
