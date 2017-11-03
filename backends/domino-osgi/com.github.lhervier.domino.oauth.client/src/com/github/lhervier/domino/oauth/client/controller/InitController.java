package com.github.lhervier.domino.oauth.client.controller;

import static com.github.lhervier.domino.oauth.client.utils.Utils.urlEncode;

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
		String redirectUri = this.props.getRedirectUri();
		String clientId = this.props.getClientId();
		StringBuffer fullRedirectUri = new StringBuffer("redirect:")
					.append(authorizeEndPoint)
					.append("?response_type=").append(urlEncode(this.props.getResponseType()))
					.append("&redirect_uri=").append(urlEncode(redirectUri))
					.append("&client_id=").append(urlEncode(clientId))
					.append("&scope=").append(urlEncode(this.props.getScope()));
		if( !StringUtils.isEmpty(state) )
			fullRedirectUri.append("&state=").append(urlEncode(state));
		fullRedirectUri.append("&nonce=").append(urlEncode(this.session.getId()));
		if( !StringUtils.isEmpty(this.props.getAuthorizeAccessType()) )
			fullRedirectUri.append("&access_type=").append(urlEncode(this.props.getAuthorizeAccessType()));
		if( !StringUtils.isEmpty(this.props.getAuthorizePrompt()) )
			fullRedirectUri.append("&prompt=").append(urlEncode(this.props.getAuthorizePrompt()));
		
		return new ModelAndView(fullRedirectUri.toString());
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
					"&code=" + urlEncode(code) +
					"&redirect_uri=" + urlEncode(this.props.getRedirectUri())
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
						InitController.this.session.setAttribute(Constants.SESSION_ACCESS_TOKEN, grant.getAccessToken());
						InitController.this.session.setAttribute(Constants.SESSION_REFRESH_TOKEN, grant.getRefreshToken());
						InitController.this.session.setAttribute(Constants.SESSION_ID_TOKEN, grant.getIdToken());
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
