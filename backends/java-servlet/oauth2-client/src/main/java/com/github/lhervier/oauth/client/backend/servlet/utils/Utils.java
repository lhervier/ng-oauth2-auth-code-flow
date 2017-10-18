package com.github.lhervier.oauth.client.backend.servlet.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lhervier.oauth.client.backend.servlet.ex.GrantException;
import com.github.lhervier.oauth.client.backend.servlet.model.GrantError;

public class Utils {

	public static final String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * An idtoken with a nonce attribute
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class NonceIdToken {
		private String nonce;
		public String getNonce() { return nonce; }
		public void setNonce(String nonce) { this.nonce = nonce; }
	}
	
	/**
	 * Check that the nonce parameter of an idToken is coherent
	 * @param idToken
	 * @param nonce
	 */
	public static final boolean checkIdTokenNonce(String idToken, String nonce) throws GrantException {
		if( idToken == null )
			return true;
		if( nonce == null )
			return true;
		
		int pos = idToken.indexOf('.');
		int pos2 = idToken.lastIndexOf('.');
		
		if( pos == -1 || pos2 == -1 || pos == pos2 ) {
			GrantError error = new GrantError();
			error.setError("invalid_id_token");
			throw new GrantException(error);
		}
		
		try {
			String b64Payload = idToken.substring(pos + 1, pos2);
			if( b64Payload.length() % 4 == 2 )
				b64Payload += "==";
			else if ( b64Payload.length() % 4 == 3 )
				b64Payload += "=";
			String payload = new String(Base64.getDecoder().decode(b64Payload.getBytes("UTF-8")), "UTF-8");
			ObjectMapper mapper = new ObjectMapper();
			NonceIdToken n = mapper.readValue(payload, NonceIdToken.class);
			return nonce.equals(n.getNonce());
		} catch (IOException e) {
			GrantError error = new GrantError();
			error.setError(e.getMessage());
			throw new GrantException(error);
		}
	}
}
