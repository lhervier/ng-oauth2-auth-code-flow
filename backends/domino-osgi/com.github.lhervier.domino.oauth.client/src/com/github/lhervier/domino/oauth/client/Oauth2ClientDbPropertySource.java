package com.github.lhervier.domino.oauth.client;

import lotus.domino.Database;
import lotus.domino.NotesException;

import com.github.lhervier.domino.spring.servlet.BaseParamViewPropertySource;

public class Oauth2ClientDbPropertySource extends BaseParamViewPropertySource {

	/**
	 * Constructor
	 */
	public Oauth2ClientDbPropertySource() {
		super("oauth2-client-property-source");
	}

	/**
	 * Return the name of the view that contains the parameters
	 */
	@Override
	protected String getViewName() {
		return "Oauth2Params";
	}
	
	/**
	 * @see com.github.lhervier.domino.spring.servlet.BaseParamViewPropertySource#getPrefix()
	 */
	@Override
	protected String getPrefix() {
		return "oauth2.client.";
	}

	/**
	 * @see com.github.lhervier.domino.spring.servlet.BaseParamViewPropertySource#checkDb(lotus.domino.Database)
	 */
	@Override
	public boolean checkDb(Database database) throws NotesException {
		return true;
	}
}
