package com.github.lhervier.oauth.client.backend.servlet.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Utils {

	public static final String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
