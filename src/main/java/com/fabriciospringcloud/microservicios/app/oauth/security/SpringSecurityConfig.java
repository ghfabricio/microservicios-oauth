package com.fabriciospringcloud.microservicios.app.oauth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter{
	
	//inyecta UserDetailsService implementado con cliente http Feign que se comunica con microservicio usuarios
	//para obtenr el usuario por el username para poder autenticarse.
	@Autowired
	private UserDetailsService usuarioService;
	
	@Autowired
	private AuthenticationEventPublisher eventPublisher;
	

	//registramos usuarioService en el AuthenticationManager sobreescribiendo configure(AuthenticationManagerBuilder auth).
	@Override
	@Autowired
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		//registramos el usuarioService
		auth.userDetailsService(this.usuarioService)
		//encriptamos el password
		.passwordEncoder(passwordEncoder())
		.and().authenticationEventPublisher(eventPublisher);
	}

	//encripta el password. Luego se usa en la configuracion del servidor de autorizaciones.
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
	// Registramos el AuthenticationManager como componente Spring para luego inyectarlo en la configuracion del servidor 
	//de autorizaciones.
	@Override
	@Bean
	protected AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}
	
	
	

}