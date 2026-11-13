package co.edu.unbosque.proyectonucleo1.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.proyectonucleo1.entity.Usuario;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Pruebas unitarias para {@link JwtUtil}.
 */
@DisplayName("JwtUtil - Pruebas unitarias")
class JwtUtilTest {

	private JwtUtil jwtUtil;

	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil();
	}

	@Test
	@DisplayName("generarToken() permite extraer usuario y rol")
	void generarYExtraer() {
		String token = jwtUtil.generarToken("ana.garcia", "DOCENTE");
		assertEquals("ana.garcia", jwtUtil.extraerNombreUsuario(token));
		assertEquals("DOCENTE", jwtUtil.extraerRol(token));
	}

	@Test
	@DisplayName("validarToken() devuelve true para el usuario correcto")
	void validarCorrecto() {
		Usuario usuario = new Usuario("Ana", "García", "ana@correo.com", "ana.garcia", "hash", "300", RolUsuario.DOCENTE);
		String token = jwtUtil.generarToken("ana.garcia", "DOCENTE");
		assertTrue(jwtUtil.validarToken(token, usuario));
	}

	@Test
	@DisplayName("validarToken() devuelve false para otro usuario")
	void validarOtroUsuario() {
		Usuario usuario = new Usuario("Luis", "Ríos", "luis@correo.com", "luis.rios", "hash", "301", RolUsuario.DOCENTE);
		String token = jwtUtil.generarToken("ana.garcia", "DOCENTE");
		assertFalse(jwtUtil.validarToken(token, usuario));
	}

	@Test
	@DisplayName("El token no está expirado al generarse")
	void noExpirado() {
		String token = jwtUtil.generarToken("ana.garcia", "DOCENTE");
		assertTrue(jwtUtil.extraerExpiracion(token).after(new Date()));
	}

}
