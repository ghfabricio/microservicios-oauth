package com.fabriciospringcloud.microservicios.app.oauth.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

/*Anotamos la clase con @Configuration y ademas la habilitamos como un servidor de configuracion con la anotacion
 * @EnableAuthorizationServer.
 * Utiliza los metodos BCryptPasswordEncoder y AuthenticationManager de la clase SpringSecurityConfig.
 * 
 * 
 * 
 * 
 * 
 */
@RefreshScope
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter{


	@Autowired
	private Environment env;

	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private InfoAdicionalToken infoAdicionalToken;

	//registramos el authenticationManager en AuthorizationServerConfigurerAdapter implementando los sigientes metodos.
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.tokenKeyAccess("permitAll()")
		.checkTokenAccess("isAuthenticated()");
	}


	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

		//clients.inMemory().withClient("frontendapp")
		//.secret(passwordEncoder.encode("12345"))		
		clients.inMemory().withClient(env.getProperty("config.security.oauth.client.id"))
		.secret(passwordEncoder.encode(env.getProperty("config.security.oauth.client.secret")))
		.scopes("read", "write")
		.authorizedGrantTypes("password", "refresh_token")
		.accessTokenValiditySeconds(3600)
		.refreshTokenValiditySeconds(3600);
		
/*	
 		//PARA AGREGAR OTRO CLIENTE
		//.and()
		//.withClient(env.getProperty("config.security.oauth.client.id"))
		//.secret(passwordEncoder.encode(env.getProperty("config.security.oauth.client.secret")))
		//.scopes("read", "write")
		//.authorizedGrantTypes("password", "refresh_token")
		//.accessTokenValiditySeconds(3600)
		//.refreshTokenValiditySeconds(3600);
*/
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(infoAdicionalToken, accessTokenConverter()));
		
		//registramos el authenticationManager
		endpoints.authenticationManager(authenticationManager)
		.tokenStore(tokenStore())
		.accessTokenConverter(accessTokenConverter())
		.tokenEnhancer(tokenEnhancerChain);
	}

	//genera el token con los datos del accessTokenConverter y se encarga de almacenarlo.
	@Bean
	public JwtTokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	//configuramos para que el token sea del tipo JWT
	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		
		JwtAccessTokenConverter tokenConverter = new JwtAccessTokenConverter();
		//tokenConverter.setSigningKey("algun_codigo_secreto_aeiou");
		//validamos la firma del toquen.
		tokenConverter.setSigningKey(env.getProperty("config.security.oauth.jwt.key"));
		return tokenConverter;

	}
	
	
	
	
}

