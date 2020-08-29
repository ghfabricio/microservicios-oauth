package com.fabriciospringcloud.microservicios.app.oauth.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fabriciospringcloud.microservicios.app.commons.usuarios.models.entity.Usuario;
import com.fabriciospringcloud.microservicios.app.oauth.clients.AuditoriaFeignClient;
import com.fabriciospringcloud.microservicios.app.oauth.clients.UsuarioFeignClient;

import brave.Tracer;
import feign.FeignException;

/*Esta clase es propia de Spring Security que implementa la interfaz UserDetailsService.
 * Tiene el metodo loadUserByUsername que se encarga de autenticar y que vamos a implementar.
 * Como utilizamos Feign para autenticar, tenemos que inyectar UsuarioFeignClient.
 * El metodo loadUserByUsername retorna un UserDetails, que es un tipo de interface que representa un usuario
 * de Spring Security, pero se debe retornar la implementacion concreta.
 * Esta clase la anotamos con @Service porque la vamos a ocupar, la tenemos configurar en Spring Security para
 * indicar que el proceso de autenticacion del proceso de login se va a realizar con esta clase UserDetailsService,
 * tiene que ir a buscar al usuario mediante el username consumiento un Api Rest mediante Feign en el microservicio
 * usuarios.
 * 
 */
@Service
public class UsuarioService implements IUsuarioService, UserDetailsService {

	private Logger log = LoggerFactory.getLogger(UsuarioService.class);

	// inyecta el cliente http
	@Autowired
	private UsuarioFeignClient client;
	
	@Autowired
	private AuditoriaFeignClient clientAuditoria;
	
	@Autowired
	private Tracer tracer;
	
	@Autowired
    private JavaMailSender javaMailSender;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		try {
            // trae el usuario
			Usuario usuario = client.findByUsername(username);
			log.info("Retornando usuario a logueado mediante client feign : "+usuario.getUsername()+"...");

			log.info("Conviertiendo roles del usuario a authorities...");
			// obtiene los roles convirtiendo de tipo entity a GrantedAuthority. Para eso utilizamos al Api
			// stream de java 8
			List<GrantedAuthority> authorities = usuario.getRoles().stream()
					//map convierte el flujo, por cada rol lo obtenemos por parametro en la funcion lamda y lo 
					//convertimos usando la clase concreta de SimpleGrantedAuthority que implementa GrantedAuthority.
					.map(role -> new SimpleGrantedAuthority(role.getNombre()))
					//peek permite imprimir el nombre del rol o autority en el log.
					.peek(authority -> log.info("Role: " + authority.getAuthority()))
					//collect convierte el tipo string a List.
					.collect(Collectors.toList());

			return new User(usuario.getUsername(), usuario.getPassword(), usuario.getEnabled(), true, true, true,
					authorities);

		} catch (FeignException e) {
			String error = "Error en el login, no existe el usuario '" + username + "' en el sistema";
			log.error(error);
			tracer.currentSpan().tag("error.mensaje", error + ": " + e.getMessage());
			throw new UsernameNotFoundException(error);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario findByUsername(String username) {
		return client.findByUsername(username);
	}

	@Override
	public Usuario update(Usuario usuario, Long id) {
		return client.update(usuario, id);
	}
	
	@Override
	public void AuditLoginSuccess(Usuario usuario, String referencia) {
		clientAuditoria.loginSuccess(usuario, referencia);
	}	

	@Override
	public void AuditLoginFailure(Usuario usuario, String referencia) {
		clientAuditoria.loginFailure(usuario, referencia);
	}
	
	@Override
	public void sendMail(String from, String to, String subject, String body) throws Throwable{
		SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(body);
       try {
    	   
        javaMailSender.send(mail);
         
       }catch (Exception e) {
    	   log.info("mail fallido");
		   e.printStackTrace();
	   }
	}

}