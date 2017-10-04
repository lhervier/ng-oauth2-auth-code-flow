package com.github.lhervier.domino.oauth.client;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ComponentScan
@EnableAspectJAutoProxy(proxyTargetClass=true)
@EnableWebMvc
public class OauthClientConfig extends WebMvcConfigurerAdapter {

	/**
	 * To set default property values null
	 * See https://stackoverflow.com/questions/11991194/can-i-set-null-as-the-default-value-for-a-value-in-spring
	 */
	@Bean
	public PropertyPlaceholderConfigurer propertyConfigurer() {
		PropertyPlaceholderConfigurer ret = new PropertyPlaceholderConfigurer();
		ret.setNullValue("@null");
		return ret;
	}
}
