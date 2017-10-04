package com.github.lhervier.oauth.client.backend.servlet.ex;

import com.github.lhervier.oauth.client.backend.servlet.model.AuthorizeError;

public class AuthorizeException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The error
	 */
	private AuthorizeError error;
	
	/**
	 * Constructor
	 */
	public AuthorizeException(AuthorizeError error) {
		this.error = error;
	}

	public AuthorizeError getError() {
		return error;
	}
}
