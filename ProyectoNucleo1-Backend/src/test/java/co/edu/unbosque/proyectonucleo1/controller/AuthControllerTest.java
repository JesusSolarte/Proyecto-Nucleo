package co.edu.unbosque.proyectonucleo1.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.unbosque.proyectonucleo1.dto.LoginDTO;
import co.edu.unbosque.proyectonucleo1.dto.UsuarioDTO;
import co.edu.unbosque.proyectonucleo1.exception.CredencialesInvalidasException;
import co.edu.unbosque.proyectonucleo1.exception.NombreUsuarioDuplicadoException;
import co.edu.unbosque.proyectonucleo1.service.UsuarioService;

/**
 * Pruebas unitarias para {@link AuthController} con el servicio mockeado.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Pruebas unitarias")
class AuthControllerTest {

	@InjectMocks
	private AuthController controller;

	@Mock
	private UsuarioService usuarioService;

	private UsuarioDTO entrada;

	@BeforeEach
	void setUp() {
		entrada = new UsuarioDTO();
		entrada.setNombre("Ana");
		entrada.setApellido("García");
		entrada.setCorreo("ana@correo.com");
		entrada.setNombreUsuario("ana.garcia");
		entrada.setContrasena("Clave1234");
		entrada.setTelefono("3001234567");
	}

	@Test
	@DisplayName("registrar() devuelve 201 con el docente creado")
	void registrarExitoso() {
		UsuarioDTO creado = new UsuarioDTO();
		creado.setId(1L);
		creado.setNombreUsuario("ana.garcia");
		when(usuarioService.registrarDocente(entrada)).thenReturn(creado);

		ResponseEntity<UsuarioDTO> respuesta = controller.registrar(entrada);

		assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
		assertSame(creado, respuesta.getBody());
		verify(usuarioService).registrarDocente(entrada);
	}

	@Test
	@DisplayName("registrar() propaga la excepción cuando el usuario ya existe")
	void registrarDuplicado() {
		when(usuarioService.registrarDocente(entrada))
				.thenThrow(new NombreUsuarioDuplicadoException("Nombre de usuario duplicado"));

		assertThrows(NombreUsuarioDuplicadoException.class, () -> controller.registrar(entrada));
	}

	@Test
	@DisplayName("iniciarSesion() devuelve 200 con el token generado")
	void loginExitoso() {
		Map<String, Object> respuestaServicio = new HashMap<>();
		respuestaServicio.put("token", "jwt.token.firma");
		respuestaServicio.put("tipoToken", "Bearer");
		when(usuarioService.iniciarSesion("ana.garcia", "Clave1234")).thenReturn(respuestaServicio);

		ResponseEntity<Map<String, Object>> respuesta = controller
				.iniciarSesion(new LoginDTO("ana.garcia", "Clave1234"));

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertEquals("jwt.token.firma", respuesta.getBody().get("token"));
		assertEquals("Bearer", respuesta.getBody().get("tipoToken"));
	}

	@Test
	@DisplayName("iniciarSesion() propaga CredencialesInvalidasException")
	void loginCredencialesInvalidas() {
		when(usuarioService.iniciarSesion("ana.garcia", "mala"))
				.thenThrow(new CredencialesInvalidasException("Usuario o contraseña incorrectos."));

		assertThrows(CredencialesInvalidasException.class,
				() -> controller.iniciarSesion(new LoginDTO("ana.garcia", "mala")));
	}

	@Test
	@DisplayName("cerrarSesion() devuelve 200 con mensaje y no usa el servicio")
	void logout() {
		ResponseEntity<Map<String, String>> respuesta = controller.cerrarSesion();

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertEquals("Sesión cerrada correctamente.", respuesta.getBody().get("mensaje"));
		verifyNoInteractions(usuarioService);
	}

}
