package com.formacionbdi.springboot.app.oauth.security.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.formacionbdi.springboot.app.oauth.services.IUsuarioService;
import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Usuario;

import feign.FeignException;

@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher {

	private Logger logger = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);

	@Autowired
	private IUsuarioService usuarioService;

	@Override
	public void publishAuthenticationSuccess(Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String mensaje = "Sucesss login: " + userDetails.getUsername();
		System.out.println(mensaje);
		logger.info(mensaje);

		Usuario usuario = usuarioService.findByUsername(authentication.getName());

		if (usuario.getIntentos() != null && usuario.getIntentos() > 0) {
			usuario.setIntentos(0);
			usuarioService.update(usuario, usuario.getId());
		}

	}

	@Override
	public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		String mensaje = "Error en el login: " + exception.getMessage();
		logger.error(mensaje);
		System.out.println(mensaje);
		logger.info(mensaje);

		try {
			Usuario usuario = usuarioService.findByUsername(authentication.getName());

			if (usuario.getIntentos() == null) {

				usuario.setIntentos(0);

			}

			logger.info("Intentos actual es de: " + usuario.getIntentos());

			usuario.setIntentos(usuario.getIntentos() + 1);

			logger.info("Intentos después es de: " + usuario.getIntentos());

			if (usuario.getIntentos() >= 3) {
				logger.error(
						String.format("El usuario %s deshabilitado por máximo de intentos.", usuario.getUsername()));
				usuario.setEnabled(false);
			}

			usuarioService.update(usuario, usuario.getId());

		} catch (FeignException e) {
			logger.error(String.format("El usuario %s no existe en el sistema", authentication.getName()));
		}

	}

}
