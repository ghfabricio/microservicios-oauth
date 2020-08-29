package com.fabriciospringcloud.microservicios.app.oauth.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fabriciospringcloud.microservicios.app.commons.usuarios.models.entity.Usuario;

@FeignClient(name="microservicios-auditoria")
public interface AuditoriaFeignClient {

	@PostMapping("/login-success")
	public void loginSuccess(@RequestBody Usuario entity, @RequestParam String referencia);
	
	@PostMapping("/login-failure")
	public void loginFailure(@RequestBody Usuario entity, @RequestParam String referencia);
	
}
