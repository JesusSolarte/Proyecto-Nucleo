package co.edu.unbosque.proyectonucleo1.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Pruebas unitarias para {@link UsuarioDTO}.
 */
@DisplayName("DTO Usuario")
class UsuarioDTOTest {

	private UsuarioDTO dto;

	@BeforeEach
	void setUp() {
		dto = new UsuarioDTO(1L, "Ana", "García", "ana@correo.com", "ana.garcia", "Pass1234", "3001234567",
				RolUsuario.DOCENTE);
	}

	@Test
	@DisplayName("Constructor parametrizado asigna los campos")
	void constructorParametrizado() {
		assertEquals(1L, dto.getId());
		assertEquals("Ana", dto.getNombre());
		assertEquals("ana.garcia", dto.getNombreUsuario());
		assertEquals(RolUsuario.DOCENTE, dto.getRol());
	}

	@Test
	@DisplayName("Setters actualizan los valores")
	void setters() {
		dto.setNombre("María");
		dto.setRol(RolUsuario.ADMIN);
		assertEquals("María", dto.getNombre());
		assertEquals(RolUsuario.ADMIN, dto.getRol());
	}

	@Test
	@DisplayName("equals y hashCode se basan en el id")
	void equalsHashCode() {
		UsuarioDTO otro = new UsuarioDTO();
		otro.setId(1L);
		assertEquals(dto, otro);
		assertEquals(dto.hashCode(), otro.hashCode());

		otro.setId(2L);
		assertNotEquals(dto, otro);
	}

	@Test
	@DisplayName("toString contiene el correo")
	void toStringContiene() {
		assertTrue(dto.toString().contains("ana@correo.com"));
	}

}
