package com.github.lhervier.oauth.client.backend.servlet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;

@Component
public class TokenService {
	private static final String ATTR_ACCESS_TOKEN = TokenService.class.getName() + ".ACCESS_TOKEN";
	private static final String ATTR_REFRESH_TOKEN = TokenService.class.getName() + ".REFRESH_TOKEN";
	private static final String ATTR_ID_TOKEN = TokenService.class.getName() + ".ID_TOKEN";
	private static final String ATTR_TOKEN_TYPE = TokenService.class.getName() + ".TOKEN_TYPE";

	@Autowired
	private HttpSession session;

	public String getAccessToken() {
		return (String) session.getAttribute(ATTR_ACCESS_TOKEN);
	}

	public void setAccessToken(String token) {
		this.session.setAttribute(ATTR_ACCESS_TOKEN, token);
	}

	public String getRefreshToken() {
		return (String) session.getAttribute(ATTR_REFRESH_TOKEN);
	}

	public void setRefreshToken(String token) {
		this.session.setAttribute(ATTR_REFRESH_TOKEN, token);
	}

	public String getIdToken() {
		return (String) session.getAttribute(ATTR_ID_TOKEN);
	}

	public void setIdToken(String token) {
		this.session.setAttribute(ATTR_ID_TOKEN, token);
	}

	public String getTokenType() {
		return (String) session.getAttribute(ATTR_TOKEN_TYPE);
	}

	public void setTokenType(String type) {
		this.session.setAttribute(ATTR_TOKEN_TYPE, type);
	}
}
