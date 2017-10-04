package com.github.lhervier.oauth.client.backend.servlet.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.github.lhervier.oauth.client.backend.servlet.ex.AuthorizeException;
import com.github.lhervier.oauth.client.backend.servlet.ex.GrantException;

@ControllerAdvice
public class ExceptionController {

	@ExceptionHandler(RuntimeException.class)
	public ModelAndView handleRuntimeException(RuntimeException e) {
		ModelAndView resp = new ModelAndView();
		resp.addObject("error", e.getMessage());
		resp.setViewName("error");
		return resp;
	}
	
	@ExceptionHandler(AuthorizeException.class)
	public ModelAndView handleAuthorizeException(AuthorizeException e) {
		ModelAndView resp = new ModelAndView();
		resp.addObject("error", e.getError());
		resp.setViewName("authorizeError");
		return resp;
	}
	
	@ExceptionHandler(GrantException.class)
	public ModelAndView handleGrantException(GrantException e) {
		ModelAndView resp = new ModelAndView();
		resp.addObject("error", e.getError());
		resp.setViewName("grantError");
		return resp;
	}
}
