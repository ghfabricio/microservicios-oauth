package com.fabriciospringcloud.microservicios.app.oauth.security.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.fabriciospringcloud.microservicios.app.commons.usuarios.models.entity.Usuario;
import com.fabriciospringcloud.microservicios.app.oauth.services.IUsuarioService;

import feign.FeignException;

import brave.Tracer;

@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher{

	private Logger log = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);
	
	@Autowired
	private IUsuarioService usuarioService;
	
	@Autowired
	private Environment env;

	@Autowired
	private Tracer tracer;
	
	@Override
	public void publishAuthenticationSuccess(Authentication authentication) {
		

		if (authentication.getName().equalsIgnoreCase(env.getProperty("config.security.oauth.client.id"))) {
			log.error("ERROR: error de lectura, el nombre de usuario: "+authentication.getName()+" es igual al nombre de la aplicacion cliente: "+env.getProperty("config.security.oauth.client.id"));
       		return; // si es igual a frontendapp se salen del método!
		}

		log.info("Autenticando usuario: "+authentication.getName()+"...");
		UserDetails user = (UserDetails) authentication.getPrincipal();
		
		log.info("Obteniendo usuario "+user.getUsername()+"...");
		Usuario usuario = usuarioService.findByUsername(user.getUsername());

		log.info("Actualizando cantidad de intentos para usuario "+user.getUsername()+"...");
		if(usuario.getIntentos() != null && usuario.getIntentos() > 0) {
			usuario.setIntentos(0);
			usuarioService.update(usuario, usuario.getId());
			log.info("OK: cantidad de intentos para usuario "+user.getUsername()+" restablecido a 0...");
		}
		
		log.info("Registrando auditoria login success para usuario "+user.getUsername()+"...");
		usuarioService.AuditLoginSuccess(usuario, "login success");
		
		log.info("Enviando email de login para usuario: "+user.getUsername()+" ...");
		try {
			String msgmail = usuario.getNombre() + " " + usuario.getApellido();
			usuarioService.sendMail(env.getProperty("spring.mail.properties.inicioSesion.from") , 
					                env.getProperty("spring.mail.properties.inicioSesion.to"), 
					                env.getProperty("spring.mail.properties.inicioSesion.subject"), 
					                msgmail);
			log.info("OK: Envio de email exitoso para login de usuario: "+user.getUsername()+"...");
			
		} catch (Throwable e) {
			log.error("ERROR: Envio de email fallido para login de usuario: "+user.getUsername()+"...");
			e.printStackTrace();
		}

	}

	@Override
	public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		String mensaje = "\"ERROR: error de autenticacion para usuario: " +authentication.getName()+" EXCEPCION: "+ exception.getMessage();
		log.error(mensaje);
		
		try {
			
			StringBuilder errors = new StringBuilder();
			errors.append(mensaje);
			
			log.info("Obteniendo usuario logueado "+authentication.getName()+"...");
			Usuario usuario = usuarioService.findByUsername(authentication.getName());
			
			if (usuario != null)  {

				log.info("Actualizando cantidad de intentos de logueos para usuario " + usuario.getUsername() + "...");
				if (usuario.getIntentos() == null) {
					usuario.setIntentos(0);
					log.info("OK: cantidad de intentos de logueos para usuario " + usuario.getUsername()
							+ " establecido en 0...");
				}

				log.info("OK: Intentos actual de logueos para usuario " + usuario.getUsername() + " es de "
						+ usuario.getIntentos() + "...");

				log.info("OK: Incrementando intentos de logueos para usuario " + usuario.getUsername() + " ...");
				usuario.setIntentos(usuario.getIntentos() + 1);

				// log.info("Intentos después es de: " + usuario.getIntentos());
				log.info("OK: Intentos final de logueos para usuario " + usuario.getUsername() + " es de "
						+ usuario.getIntentos() + "...");

				errors.append(" - Intentos del login: " + usuario.getIntentos());

				if (usuario.getIntentos() >= 3) {
					log.error("ERROR: Desabilitando usuario: " + usuario.getUsername()
							+ " por maximos intentos de logueo...");
					String errorMaxIntentos = String.format("El usuario %s des-habilitado por máximos intentos.",
							usuario.getUsername());
					log.error(errorMaxIntentos);
					errors.append(" - " + errorMaxIntentos);
					usuario.setEnabled(false);
				}

				log.info("OK: Actualizando intentos para usuario " + usuario.getUsername() + " ...");
				usuarioService.update(usuario, usuario.getId());

				tracer.currentSpan().tag("error.mensaje", errors.toString());
				
				log.info("Registrando auditoria de login failure para usuario "+usuario.getUsername()+"...");
				usuarioService.AuditLoginFailure(usuario, "password incorrecto");

			}else {
				Usuario username = new Usuario();
				username.setUsername(authentication.getName());
				log.info("Registrando auditoria de login failure para usuario "+authentication.getName()+"...");
				usuarioService.AuditLoginFailure(username, "username incorrecto");
			}
			
				
		} catch (FeignException e) {
			log.error(String.format("Registrando auditoria de login failure para usuario "+authentication.getName()+"..."));
		}
	}

}
