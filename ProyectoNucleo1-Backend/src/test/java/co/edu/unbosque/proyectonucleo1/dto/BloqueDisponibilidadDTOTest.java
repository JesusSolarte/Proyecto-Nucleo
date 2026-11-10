package co.edu.unbosque.proyectonucleo1.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.proyectonucleo1.util.enums.DiaSemana;

/**
 * Pruebas unitarias para {@link BloqueDisponibilidadDTO}.
 */
@DisplayName("DTO BloqueDisponibilidad")
class BloqueDisponibilidadDTOTest {

	private BloqueDisponibilidadDTO dto;

	@BeforeEach
	void setUp() {
		dto = new BloqueDisponibilidadDTO(1L, 2L, "Carlos Pérez", DiaSemana.MARTES, LocalTime.of(8, 0),
				LocalTime.of(10, 0));
	}

	@Test
	@DisplayName("Constructor parametrizado asigna los campos")
	void constructorParametrizado() {
		assertEquals(1L, dto.getId());
		assertEquals(2L, dto.getIdDocente());
		assertEquals(DiaSemana.MARTES, dto.getDiaSemana());
		assertEquals(LocalTime.of(8, 0), dto.getHoraInicio());
	}

	@Test
	@DisplayName("Setters actualizan los valores")
	void setters() {
		dto.setDiaSemana(DiaSemana.VIERNES);
		dto.setHoraFin(LocalTime.of(12, 0));
		assertEquals(DiaSemana.VIERNES, dto.getDiaSemana());
		assertEquals(LocalTime.of(12, 0), dto.getHoraFin());
	}

	@Test
	@DisplayName("equals y hashCode se basan en el id")
	void equalsHashCode() {
		BloqueDisponibilidadDTO otro = new BloqueDisponibilidadDTO();
		otro.setId(1L);
		assertEquals(dto, otro);
		assertEquals(dto.hashCode(), otro.hashCode());

		otro.setId(9L);
		assertNotEquals(dto, otro);
	}

	@Test
	@DisplayName("toString contiene el día de la semana")
	void toStringContiene() {
		assertTrue(dto.toString().contains("MARTES"));
	}

}
