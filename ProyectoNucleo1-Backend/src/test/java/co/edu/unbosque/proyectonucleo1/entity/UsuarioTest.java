package co.edu.unbosque.proyectonucleo1.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Pruebas unitarias para la entidad {@link Usuario}.
 */
@DisplayName("Entidad Usuario")
class UsuarioTest {

	private Usuario usuario;

	@BeforeEach
	void setUp() {
		usuario = new Usuario("Ana", "López", "ana@correo.com", "ana.lopez", "Pass1234", "3001234567",
				RolUsuario.DOCENTE);
	}

	@Test
	@DisplayName("Constructor parametrizado asigna todos los campos")
	void constructorParametrizado() {
		assertEquals("Ana", usuario.getNombre());
		assertEquals("López", usuario.getApellido());
		assertEquals("ana@correo.com", usuario.getCorreo());
		assertEquals("ana.lopez", usuario.getNombreUsuario());
		assertEquals("Pass1234", usuario.getContrasena());
		assertEquals("3001234567", usuario.getTelefono());
		assertEquals(RolUsuario.DOCENTE, usuario.getRol());
	}

	@Test
	@DisplayName("Constructor vacío no asigna valores")
	void constructorVacio() {
		Usuario u = new Usuario();
		assertNull(u.getId());
		assertNull(u.getRol());
	}

	@Test
	@DisplayName("getUsername() devuelve el nombre de usuario")
	void getUsername() {
		assertEquals("ana.lopez", usuario.getUsername());
	}

	@Test
	@DisplayName("getPassword() devuelve la contraseña")
	void getPassword() {
		assertEquals("Pass1234", usuario.getPassword());
	}

	@Test
	@DisplayName("getAuthorities() devuelve ROLE_DOCENTE")
	void getAuthoritiesDocente() {
		assertTrue(usuario.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCENTE")));
	}

	@Test
	@DisplayName("getAuthorities() devuelve ROLE_ADMIN cuando el rol es ADMIN")
	void getAuthoritiesAdmin() {
		usuario.setRol(RolUsuario.ADMIN);
		assertTrue(usuario.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
	}

	@Test
	@DisplayName("Cuenta habilitada y no vencida")
	void estadoCuenta() {
		assertTrue(usuario.isAccountNonExpired());
		assertTrue(usuario.isAccountNonLocked());
		assertTrue(usuario.isCredentialsNonExpired());
		assertTrue(usuario.isEnabled());
	}

	@Test
	@DisplayName("equals y hashCode se basan en el id")
	void equalsHashCode() {
		Usuario otro = new Usuario();
		usuario.setId(1L);
		otro.setId(1L);
		assertEquals(usuario, otro);
		assertEquals(usuario.hashCode(), otro.hashCode());

		otro.setId(2L);
		assertNotEquals(usuario, otro);
	}

	@Test
	@DisplayName("toString contiene el nombre de usuario")
	void toStringContiene() {
		assertTrue(usuario.toString().contains("ana.lopez"));
	}

}
