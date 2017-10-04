package com.github.lhervier.oauth.client.backend.servlet.ex;

import com.github.lhervier.oauth.client.backend.servlet.model.GrantError;

public class GrantException extends Exception {

	private static final long serialVersionUID = 7418669090093143504L;

	private GrantError error;
	
	public GrantException(GrantError e) {
		this.error = e;
	}

	public GrantError getError() {
		return error;
	}
}
