package co.edu.unbosque.proyectonucleo1.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.proyectonucleo1.util.enums.DiaSemana;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Pruebas unitarias para la entidad {@link BloqueDisponibilidad}.
 */
@DisplayName("Entidad BloqueDisponibilidad")
class BloqueDisponibilidadTest {

	private BloqueDisponibilidad bloque;

	@BeforeEach
	void setUp() {
		Usuario docente = new Usuario("Carlos", "Pérez", "carlos@correo.com", "carlos", "Pass1234", "3001112233",
				RolUsuario.DOCENTE);
		docente.setId(1L);
		bloque = new BloqueDisponibilidad(docente, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(10, 0));
	}

	@Test
	@DisplayName("Constructor parametrizado asigna todos los campos")
	void constructorParametrizado() {
		assertEquals(DiaSemana.LUNES, bloque.getDiaSemana());
		assertEquals(LocalTime.of(8, 0), bloque.getHoraInicio());
		assertEquals(LocalTime.of(10, 0), bloque.getHoraFin());
		assertEquals(1L, bloque.getDocente().getId());
	}

	@Test
	@DisplayName("equals y hashCode se basan en el id")
	void equalsHashCode() {
		BloqueDisponibilidad otro = new BloqueDisponibilidad();
		bloque.setId(5L);
		otro.setId(5L);
		assertEquals(bloque, otro);
		assertEquals(bloque.hashCode(), otro.hashCode());

		otro.setId(6L);
		assertNotEquals(bloque, otro);
	}

	@Test
	@DisplayName("toString contiene el día de la semana")
	void toStringContiene() {
		assertTrue(bloque.toString().contains("LUNES"));
	}

}
