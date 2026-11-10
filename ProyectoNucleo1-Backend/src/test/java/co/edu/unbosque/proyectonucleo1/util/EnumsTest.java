package co.edu.unbosque.proyectonucleo1.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.proyectonucleo1.util.enums.DiaSemana;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Pruebas unitarias para las enumeraciones del dominio.
 */
@DisplayName("Enumeraciones del dominio")
class EnumsTest {

	@Test
	@DisplayName("DiaSemana contiene los siete días")
	void diasSemana() {
		List<DiaSemana> dias = Arrays.asList(DiaSemana.values());
		assertEquals(7, dias.size());
		assertTrue(dias.contains(DiaSemana.LUNES));
		assertTrue(dias.contains(DiaSemana.DOMINGO));
	}

	@Test
	@DisplayName("DiaSemana expone un nombre legible")
	void nombreDia() {
		assertEquals("Miércoles", DiaSemana.MIERCOLES.getNombre());
	}

	@Test
	@DisplayName("RolUsuario contiene DOCENTE y ADMIN")
	void roles() {
		List<RolUsuario> roles = Arrays.asList(RolUsuario.values());
		assertEquals(2, roles.size());
		assertTrue(roles.contains(RolUsuario.DOCENTE));
		assertTrue(roles.contains(RolUsuario.ADMIN));
	}

}
