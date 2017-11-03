package com.github.lhervier.domino.oauth.client.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.github.lhervier.domino.oauth.client.ex.OauthClientException;

public class Utils {

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
	public static final boolean checkIdTokenNonce(String idToken, String nonce) throws OauthClientException {
		if( idToken == null )
			return true;
		if( nonce == null )
			return true;
		
		int pos = idToken.indexOf('.');
		int pos2 = idToken.lastIndexOf('.');
		
		// Invalid id token
		if( pos == -1 || pos2 == -1 )
			throw new OauthClientException("Invalid IdToken");
		if( pos == pos2 )
			throw new OauthClientException("Invalid IdToken");
		
		String payload = b64Decode(idToken.substring(pos + 1, pos2));
		ObjectMapper mapper = new ObjectMapper();
		try {
			NonceIdToken n = mapper.readValue(payload, NonceIdToken.class);
			return nonce.equals(n.getNonce());
		} catch (JsonParseException e) {
			throw new OauthClientException(e);
		} catch (JsonMappingException e) {
			throw new OauthClientException(e);
		} catch (IOException e) {
			throw new OauthClientException(e);
		}
	}
	
	/**
	 * Decode a base64 string
	 * @param s the source string (base64 encoded)
	 * @return the decoded string
	 */
	public static final String b64Decode(String s) {
		try {
			if( s.length() % 4 == 2 )
				s = s + "==";
			else if ( s.length() % 4 == 3 )
				s = s + "=";
			byte[] decoded = Base64.decodeBase64(s.getBytes("UTF-8"));
			return new String(decoded, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);		// UTF-8 is supported !
		}
	}
	
	/**
	 * Encode a string to base64
	 * @param s the string to encode
	 * @return the encoded string
	 */
	public static final String b64Encode(String s) {
		try {
			return new String(Base64.encodeBase64(s.getBytes("UTF-8")), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * URL encode a value
	 * @param value 
	 * @return the encoded value
	 */
	public static final String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);		// UTF-8 is supported !
		}
	}
}
