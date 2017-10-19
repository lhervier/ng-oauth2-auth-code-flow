package com.github.lhervier.domino.oauth.client.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.lhervier.domino.oauth.client.Oauth2ClientProperties;
import com.github.lhervier.domino.oauth.client.model.GrantError;
import com.github.lhervier.domino.oauth.client.model.GrantResponse;
import com.github.lhervier.domino.oauth.client.utils.HttpConnection;
import com.github.lhervier.domino.oauth.client.utils.Utils;

/**
 * Service to access to the token endpoint
 * @author Lionel HERVIER
 */
@Component
public class TokenService {

	/**
	 * Has the SSL context been initialized
	 */
	private static boolean sslInitialized = false;
	
	/**
	 * Object lock
	 */
	private static final Object LOCK = new Object();
	
	/**
	 * Should we disable the checking of certificates when send https requests ?
	 * Injected at creation time (from notes.ini, or other global property source)
	 */
	@Value("${oauth2.client.disableCheckCertificate:false}")
	private boolean disableCheckCertificate;
	
	/**
	 * The properties
	 */
	@Autowired
	private Oauth2ClientProperties props;
	
	/**
	 * Pour initialiser le contexte SSL.
	 * On ne l'initialise qu'une seule fois. Pour revenir en arrière, il
	 * faut relancer la tâche http.
	 * @return le contexte SSL
	 */
	private SSLSocketFactory getSSLSocketFactory() {
		synchronized(LOCK) {
			if( !this.disableCheckCertificate )
				return null;
			
			if( sslInitialized ) {
				SSLContext context;
				try {
					context = SSLContext.getInstance("SSL");
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e);
				}
				return context.getSocketFactory();
			}
			
			sslInitialized = true;
			
			try {
				SSLContext context = SSLContext.getInstance("SSL");
				TrustManager[] trustAll = new TrustManager[] { new X509TrustManager() {
		            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		                return null;
		            }
		            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		            }
		            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		            }
		        }};
				context.init(null, trustAll, new SecureRandom());
				return context.getSocketFactory();
			} catch (KeyManagementException e) {
				throw new RuntimeException(e);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Prepare an HttpConnection to the token endpoint
	 * @param textContent the text content
	 * @return la connection
	 */
	public HttpConnection<GrantResponse, GrantError> createTokenConnection(String textContent) throws IOException {
		HostnameVerifier verifier = null;
		if( this.props.isDisableHostVerifier() ) {
			verifier = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
		}
		String authMode = this.props.getTokenAuthMode();
		if( "queryString".equals(authMode) || "none".equals(authMode) ) {
			textContent += "&client_id=" + this.props.getClientId();
		}
		if( "queryString".equals(authMode) ) {
			textContent += "&client_secret=" + this.props.getSecret();
		}
		
		HttpConnection<GrantResponse, GrantError> conn = HttpConnection.createConnection(
				this.props.getTokenUrl(), 
				GrantResponse.class, 
				GrantError.class
		)
		.addHeader("Content-Type", "application/x-www-form-urlencoded")
		.withVerifier(verifier)
		.withFactory(this.getSSLSocketFactory())
		.setTextContent(textContent, "UTF-8");
		
		if( "basic".equals(authMode) ) {
			String auth = String.format("%s:%s", this.props.getClientId(), this.props.getSecret());
			conn.addHeader("Authorization", "Basic " + Utils.b64Encode(auth));
		}
		
		return conn;
	}
}
