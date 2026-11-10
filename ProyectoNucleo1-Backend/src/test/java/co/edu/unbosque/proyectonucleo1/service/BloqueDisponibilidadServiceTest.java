package co.edu.unbosque.proyectonucleo1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.proyectonucleo1.dto.BloqueDisponibilidadDTO;
import co.edu.unbosque.proyectonucleo1.entity.BloqueDisponibilidad;
import co.edu.unbosque.proyectonucleo1.entity.Usuario;
import co.edu.unbosque.proyectonucleo1.exception.CruceHorarioException;
import co.edu.unbosque.proyectonucleo1.exception.RangoHorarioInvalidoException;
import co.edu.unbosque.proyectonucleo1.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectonucleo1.repository.BloqueDisponibilidadRepository;
import co.edu.unbosque.proyectonucleo1.util.enums.DiaSemana;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Pruebas unitarias para {@link BloqueDisponibilidadService}, incluyendo la
 * validación de cruces de horario.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BloqueDisponibilidadService - Pruebas unitarias")
class BloqueDisponibilidadServiceTest {

	@InjectMocks
	private BloqueDisponibilidadService service;

	@Mock
	private BloqueDisponibilidadRepository repo;
	@Mock
	private UsuarioService usuarioService;

	private Usuario docente;
	private BloqueDisponibilidadDTO dto;

	@BeforeEach
	void setUp() {
		docente = new Usuario("Carlos", "Pérez", "carlos@correo.com", "carlos", "hash", "3001112233",
				RolUsuario.DOCENTE);
		docente.setId(1L);

		dto = new BloqueDisponibilidadDTO();
		dto.setIdDocente(1L);
		dto.setDiaSemana(DiaSemana.LUNES);
		dto.setHoraInicio(LocalTime.of(8, 0));
		dto.setHoraFin(LocalTime.of(10, 0));
	}

	@Test
	@DisplayName("crear() con datos válidos guarda el bloque")
	void crearValido() {
		when(usuarioService.obtenerEntidad(1L)).thenReturn(docente);
		when(repo.findByDocenteIdAndDiaSemana(1L, DiaSemana.LUNES)).thenReturn(List.of());

		BloqueDisponibilidadDTO resultado = service.crear(dto);

		assertEquals(DiaSemana.LUNES, resultado.getDiaSemana());
		verify(repo, times(1)).save(any(BloqueDisponibilidad.class));
	}

	@Test
	@DisplayName("crear() con rango inválido lanza RangoHorarioInvalidoException")
	void crearRangoInvalido() {
		when(usuarioService.obtenerEntidad(1L)).thenReturn(docente);
		dto.setHoraInicio(LocalTime.of(12, 0));
		dto.setHoraFin(LocalTime.of(10, 0));

		assertThrows(RangoHorarioInvalidoException.class, () -> service.crear(dto));
	}

	@Test
	@DisplayName("crear() con cruce de horario lanza CruceHorarioException")
	void crearCruce() {
		BloqueDisponibilidad existente = new BloqueDisponibilidad(docente, DiaSemana.LUNES, LocalTime.of(9, 0),
				LocalTime.of(11, 0));
		existente.setId(5L);
		when(usuarioService.obtenerEntidad(1L)).thenReturn(docente);
		when(repo.findByDocenteIdAndDiaSemana(1L, DiaSemana.LUNES)).thenReturn(List.of(existente));

		dto.setHoraInicio(LocalTime.of(10, 0));
		dto.setHoraFin(LocalTime.of(12, 0));

		assertThrows(CruceHorarioException.class, () -> service.crear(dto));
	}

	@Test
	@DisplayName("listarPorDocente() devuelve los bloques ordenados")
	void listarPorDocente() {
		BloqueDisponibilidad b1 = new BloqueDisponibilidad(docente, DiaSemana.MARTES, LocalTime.of(8, 0),
				LocalTime.of(9, 0));
		BloqueDisponibilidad b2 = new BloqueDisponibilidad(docente, DiaSemana.LUNES, LocalTime.of(10, 0),
				LocalTime.of(11, 0));
		when(usuarioService.obtenerEntidad(1L)).thenReturn(docente);
		when(repo.findByDocenteId(1L)).thenReturn(List.of(b1, b2));

		List<BloqueDisponibilidadDTO> resultado = service.listarPorDocente(1L);

		assertEquals(2, resultado.size());
		assertEquals(DiaSemana.LUNES, resultado.get(0).getDiaSemana());
	}

	@Test
	@DisplayName("obtenerPorId() inexistente lanza RecursoNoEncontradoException")
	void obtenerInexistente() {
		when(repo.findById(99L)).thenReturn(Optional.empty());
		assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(99L));
	}

	@Test
	@DisplayName("eliminar() elimina un bloque existente")
	void eliminar() {
		BloqueDisponibilidad bloque = new BloqueDisponibilidad(docente, DiaSemana.LUNES, LocalTime.of(8, 0),
				LocalTime.of(10, 0));
		bloque.setId(3L);
		when(repo.findById(3L)).thenReturn(Optional.of(bloque));

		service.eliminar(3L);

		verify(repo, times(1)).delete(bloque);
	}

}
