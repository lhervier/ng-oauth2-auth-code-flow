package com.github.lhervier.domino.oauth.client.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.lhervier.domino.oauth.client.Constants;
import com.github.lhervier.domino.oauth.client.model.TokensResponse;

@Controller
public class TokensController {

	/**
	 * The http session
	 */
	@Autowired
	private HttpSession httpSession;
	
	/**
	 * Send the access token
	 */
	@RequestMapping(value = "/tokens", method = RequestMethod.GET)
	public @ResponseBody TokensResponse tokens() {
		TokensResponse resp = new TokensResponse();
		resp.setAccessToken((String) this.httpSession.getAttribute(Constants.SESSION_ACCESS_TOKEN));
		resp.setIdToken((String) this.httpSession.getAttribute(Constants.SESSION_ID_TOKEN));
		return resp;
	}
}
