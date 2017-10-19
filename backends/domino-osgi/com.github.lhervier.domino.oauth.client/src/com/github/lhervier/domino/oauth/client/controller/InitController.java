package com.github.lhervier.domino.oauth.client.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lotus.domino.NotesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.domino.oauth.client.Constants;
import com.github.lhervier.domino.oauth.client.Oauth2ClientProperties;
import com.github.lhervier.domino.oauth.client.ex.OauthClientException;
import com.github.lhervier.domino.oauth.client.model.AuthorizeError;
import com.github.lhervier.domino.oauth.client.model.GrantError;
import com.github.lhervier.domino.oauth.client.model.GrantResponse;
import com.github.lhervier.domino.oauth.client.service.TokenService;
import com.github.lhervier.domino.oauth.client.utils.Callback;
import com.github.lhervier.domino.oauth.client.utils.QueryStringUtils;
import com.github.lhervier.domino.oauth.client.utils.StringUtils;
import com.github.lhervier.domino.oauth.client.utils.Utils;

@Controller
public class InitController {

	/**
	 * The http servlet request
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * The http session
	 */
	@Autowired
	private HttpSession session;
	
	/**
	 * Spring environment
	 */
	@Autowired
	private Oauth2ClientProperties props;
	
	/**
	 * The token service
	 */
	@Autowired
	private TokenService tokenSvc;
	
	/**
	 * Initialisation
	 * @throws OauthClientException
	 */
	@RequestMapping(value = "/init")
	public ModelAndView init(
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "redirect_url", required = false) String redirectUrl) throws OauthClientException {
		// If we have a authorization code, we can process it
		if( !StringUtils.isEmpty(code) )
			return this.processAuthorizationCode(code);		// dans state, on retrouve notre url de redirection initiale
		
		// If we have an error, we display it
		if( !StringUtils.isEmpty(error) ) {
			AuthorizeError authError = QueryStringUtils.createBean(
					this.request,
					AuthorizeError.class
			);
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("error", authError);
			return new ModelAndView("authorizeError", model);
		}
		
		// Otherwise, we redirect to the authorize endpoint
		this.session.setAttribute(Constants.SESSION_REDIRECT_URL, redirectUrl);
		String authorizeEndPoint = this.props.getAuthorizeUrl();
		String redirectUri = Utils.getEncodedRedirectUri(this.props.getRedirectUri());
		String clientId = this.props.getClientId();
		String fullRedirectUri = authorizeEndPoint + "?" +
					"response_type=" + this.props.getResponseType() + "&" +
					"redirect_uri=" + redirectUri + "&" +
					"client_id=" + clientId + "&" +
					"scope=" + this.props.getScope() + "&" +
					"state=" + state + "&" +
					"nonce=" + session.getId();
		if( !StringUtils.isEmpty(this.props.getAuthorizeAccessType()) )
			fullRedirectUri += "&access_type=" + this.props.getAuthorizeAccessType();
		
		return new ModelAndView("redirect:" + fullRedirectUri);
	}
	
	/**
	 * Process authorization code, and fill session with tokens.
	 * @param code le code autorisation
	 * @throws IOException 
	 * @throws NotesException 
	 */
	private ModelAndView processAuthorizationCode(final String code) throws OauthClientException {
		try {
			// The response object
			final ModelAndView ret = new ModelAndView();
			
			// Create the connection
			this.tokenSvc.createTokenConnection(
					"grant_type=authorization_code" +
					"&code=" + code +
					"&redirect_uri=" + Utils.getEncodedRedirectUri(this.props.getRedirectUri())
			)
			
			// OK => Mémorise les tokens en session et redirige vers l'url initiale
			.onOk(new Callback<GrantResponse>() {
				@Override
				public void run(GrantResponse grant) throws Exception {
					if( !"Bearer".equalsIgnoreCase(grant.getTokenType()) )
						throw new RuntimeException("Le seul type de token géré est 'Bearer'... (et j'ai '"  + grant.getTokenType() + "')");
					
					// If we have an id_token in the response, we must check the nonce value
					if( !Utils.checkIdTokenNonce(grant.getIdToken(), InitController.this.session.getId()) ) {
						GrantError error = new GrantError();
						error.setError("invalid_nonce_in_id_token");
						ret.addObject("error", error);
						ret.setViewName("grantError");
					} else {
						InitController.this.session.setAttribute("ACCESS_TOKEN", grant.getAccessToken());
						InitController.this.session.setAttribute("REFRESH_TOKEN", grant.getRefreshToken());
						InitController.this.session.setAttribute("ID_TOKEN", grant.getIdToken());
						ret.setViewName("redirect:" + InitController.this.session.getAttribute(Constants.SESSION_REDIRECT_URL));
					}
				}
			})
			
			// KO => Affiche l'erreur dans la XPage
			.onError(new Callback<GrantError>() {
				@Override
				public void run(GrantError error) throws IOException {
					ret.addObject("error", error);
					ret.setViewName("grantError");
				}
			})
			
			// Send the request
			.execute();
			
			return ret;
		} catch(IOException e) {
			throw new OauthClientException(e);
		}
	}
}
