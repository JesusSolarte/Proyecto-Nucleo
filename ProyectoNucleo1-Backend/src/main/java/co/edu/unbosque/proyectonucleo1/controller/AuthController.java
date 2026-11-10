package co.edu.unbosque.proyectonucleo1.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.proyectonucleo1.dto.LoginDTO;
import co.edu.unbosque.proyectonucleo1.dto.UsuarioDTO;
import co.edu.unbosque.proyectonucleo1.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador de autenticación: registro de docentes, inicio y cierre de
 * sesión.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, inicio y cierre de sesión")
public class AuthController {

	private final UsuarioService usuarioService;

	public AuthController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@PostMapping("/registro")
	@Operation(summary = "Registrar un docente", description = "Crea un nuevo usuario con rol DOCENTE.")
	public ResponseEntity<UsuarioDTO> registrar(@RequestBody UsuarioDTO usuario) {
		return new ResponseEntity<>(usuarioService.registrarDocente(usuario), HttpStatus.CREATED);
	}

	@PostMapping("/login")
	@Operation(summary = "Iniciar sesión", description = "Valida las credenciales y devuelve un token JWT.")
	public ResponseEntity<Map<String, Object>> iniciarSesion(@RequestBody LoginDTO login) {
		return new ResponseEntity<>(usuarioService.iniciarSesion(login.getNombreUsuario(), login.getContrasena()),
				HttpStatus.OK);
	}

	@PostMapping("/logout")
	@Operation(summary = "Cerrar sesión", description = "Cierre de sesión en un esquema sin estado (el cliente descarta el token).")
	public ResponseEntity<Map<String, String>> cerrarSesion() {
		return new ResponseEntity<>(Map.of("mensaje", "Sesión cerrada correctamente."), HttpStatus.OK);
	}

}
