package com.fabriciospringcloud.microservicios.app.oauth.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fabriciospringcloud.microservicios.app.commons.usuarios.models.entity.Usuario;

import org.springframework.web.bind.annotation.RequestBody;


/*Este cliente http Feign se comunica con el microservicio usuarios mediante Api Rest.
 * Con la anotacion @FeignClient(name="microservicios-usuarios").
 * El metodo findByUserName es anotado con @GetMapping("/usuarios/search/buscar-username") para definir el
 * endpoint donde vamos a ir a buscar al usuario al backend por el username.
 * Este endpoint se forma de la ruta del repositorio DAO de la clase usuario y el path del metodo 
 * buscar-username con el prefijo search.
 * 
 * Luego este cliente Feign se utiliza cuando implementemos la calse de Spring Security para realizar y personalizar
 * el login, la autenticacion enviando el username al microservicio usuarios y obtener el usuarios que queremos
 * autenticar.
 * 
 * 
 * 
 */
@FeignClient(name="microservicios-usuarios")
public interface UsuarioFeignClient {


	//@GetMapping("/usuarios/search/buscar-username")
	@GetMapping("/buscar-usuario")
	public Usuario findByUsername(@RequestParam String username);
	
	
	@PutMapping("/usuarios/{id}")
	public Usuario update(@RequestBody Usuario usuario, @PathVariable Long id);
}
