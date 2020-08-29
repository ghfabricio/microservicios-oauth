package com.fabriciospringcloud.microservicios.app.oauth.services;

import com.fabriciospringcloud.microservicios.app.commons.usuarios.models.entity.Usuario;

public interface IUsuarioService {
	
	public Usuario findByUsername(String username);
	
	public Usuario update(Usuario usuario, Long id);
	
	public void sendMail (String from, String to, String subject, String body) throws Throwable;

	public void AuditLoginSuccess(Usuario usuario, String referencia);
	
	public void AuditLoginFailure(Usuario usuario, String referencia);
}

