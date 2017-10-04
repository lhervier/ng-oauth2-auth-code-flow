package com.github.lhervier.oauth.client.backend.servlet.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GrantError {

	private String error;
	
	@JsonProperty("error_description")
	private String errorDescription;
	
	@JsonProperty("error_uri")
	private String errorUri;

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public String getErrorUri() {
		return errorUri;
	}

	public void setErrorUri(String errorUri) {
		this.errorUri = errorUri;
	}
	
}
