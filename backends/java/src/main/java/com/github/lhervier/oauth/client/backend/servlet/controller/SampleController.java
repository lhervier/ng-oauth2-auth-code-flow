package com.github.lhervier.oauth.client.backend.servlet.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to send server config to the client
 * Specific to the sample app
 * @author Lionel HERVIER
 */
@RestController
public class SampleController {

	@Value("${app.userInfoEndPoint}")
	private String userInfoEndPoint;
	
	public static class ParamResponse {
		private String userInfoEndPoint;
		public String getUserInfoEndPoint() { return userInfoEndPoint; }
		public void setUserInfoEndPoint(String userInfoEndPoint) { this.userInfoEndPoint = userInfoEndPoint; }
	}
	
	@RequestMapping(value = "/params", method = RequestMethod.GET)
	public ParamResponse params() {
		ParamResponse ret = new ParamResponse();
		ret.setUserInfoEndPoint(this.userInfoEndPoint);
		return ret;
	}
}
