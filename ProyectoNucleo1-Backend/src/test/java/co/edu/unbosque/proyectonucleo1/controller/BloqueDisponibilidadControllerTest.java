package co.edu.unbosque.proyectonucleo1.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import co.edu.unbosque.proyectonucleo1.dto.BloqueDisponibilidadDTO;
import co.edu.unbosque.proyectonucleo1.entity.BloqueDisponibilidad;
import co.edu.unbosque.proyectonucleo1.entity.Usuario;
import co.edu.unbosque.proyectonucleo1.exception.DatoInvalidoException;
import co.edu.unbosque.proyectonucleo1.exception.UsuarioSinPermisoException;
import co.edu.unbosque.proyectonucleo1.service.BloqueDisponibilidadService;
import co.edu.unbosque.proyectonucleo1.service.UsuarioService;
import co.edu.unbosque.proyectonucleo1.util.enums.DiaSemana;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Pruebas unitarias para {@link BloqueDisponibilidadController}. Se centran en
 * las reglas de permisos: el docente solo gestiona sus bloques y el
 * administrador puede gestionar los de cualquiera.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BloqueDisponibilidadController - Pruebas unitarias")
class BloqueDisponibilidadControllerTest {

	@InjectMocks
	private BloqueDisponibilidadController controller;

	@Mock
	private BloqueDisponibilidadService bloqueService;

	@Mock
	private UsuarioService usuarioService;

	@Mock
	private Authentication autenticacion;

	private Usuario docente;
	private Usuario otroDocente;
	private Usuario admin;
	private BloqueDisponibilidadDTO dto;

	@BeforeEach
	void setUp() {
		docente = new Usuario("Ana", "García", "ana@correo.com", "ana.garcia", "hash", "3001234567",
				RolUsuario.DOCENTE);
		docente.setId(1L);
		otroDocente = new Usuario("Luis", "Ríos", "luis@correo.com", "luis.rios", "hash", "3009876543",
				RolUsuario.DOCENTE);
		otroDocente.setId(2L);
		admin = new Usuario("Administrador", "Sistema", "admin@correo.com", "admin", "hash", "3000000000",
				RolUsuario.ADMIN);
		admin.setId(99L);

		dto = new BloqueDisponibilidadDTO();
		dto.setDiaSemana(DiaSemana.LUNES);
		dto.setHoraInicio(LocalTime.of(8, 0));
		dto.setHoraFin(LocalTime.of(10, 0));

		lenient().when(autenticacion.isAuthenticated()).thenReturn(true);
	}

	/** Simula que el usuario autenticado es el docente "ana.garcia". */
	private void comoDocente() {
		lenient().when(autenticacion.getName()).thenReturn("ana.garcia");
		lenient().when(usuarioService.obtenerEntidadPorNombreUsuario("ana.garcia")).thenReturn(docente);
	}

	/** Simula que el usuario autenticado es el administrador. */
	private void comoAdmin() {
		lenient().when(autenticacion.getName()).thenReturn("admin");
		lenient().when(usuarioService.obtenerEntidadPorNombreUsuario("admin")).thenReturn(admin);
	}

	private BloqueDisponibilidad bloqueDe(Usuario dueno, Long id) {
		BloqueDisponibilidad b = new BloqueDisponibilidad(dueno, DiaSemana.LUNES, LocalTime.of(8, 0),
				LocalTime.of(10, 0));
		b.setId(id);
		return b;
	}

	// ------------------------------------------------------------ listar

	@Test
	@DisplayName("listar() como ADMIN devuelve todos los bloques")
	void listarComoAdmin() {
		comoAdmin();
		List<BloqueDisponibilidadDTO> todos = List.of(dto);
		when(bloqueService.listar()).thenReturn(todos);

		ResponseEntity<List<BloqueDisponibilidadDTO>> respuesta = controller.listar(autenticacion);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertSame(todos, respuesta.getBody());
		verify(bloqueService, never()).listarPorDocente(anyLong());
	}

	@Test
	@DisplayName("listar() como DOCENTE devuelve solo sus propios bloques")
	void listarComoDocente() {
		comoDocente();
		List<BloqueDisponibilidadDTO> propios = List.of(dto);
		when(bloqueService.listarPorDocente(1L)).thenReturn(propios);

		ResponseEntity<List<BloqueDisponibilidadDTO>> respuesta = controller.listar(autenticacion);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertSame(propios, respuesta.getBody());
		verify(bloqueService, never()).listar();
	}

	@Test
	@DisplayName("listar() sin autenticación lanza UsuarioSinPermisoException")
	void listarSinAutenticacion() {
		assertThrows(UsuarioSinPermisoException.class, () -> controller.listar(null));
		verifyNoInteractions(bloqueService);
	}

	@Test
	@DisplayName("listar() con autenticación no válida lanza UsuarioSinPermisoException")
	void listarAutenticacionNoValida() {
		when(autenticacion.isAuthenticated()).thenReturn(false);

		assertThrows(UsuarioSinPermisoException.class, () -> controller.listar(autenticacion));
		verifyNoInteractions(bloqueService);
	}

	// ------------------------------------------------------ obtenerPorId

	@Test
	@DisplayName("obtenerPorId() permite al docente consultar su propio bloque")
	void obtenerPorIdPropio() {
		comoDocente();
		when(bloqueService.obtenerEntidad(10L)).thenReturn(bloqueDe(docente, 10L));
		when(bloqueService.obtenerPorId(10L)).thenReturn(dto);

		ResponseEntity<BloqueDisponibilidadDTO> respuesta = controller.obtenerPorId(10L, autenticacion);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertSame(dto, respuesta.getBody());
	}

	@Test
	@DisplayName("obtenerPorId() rechaza al docente que consulta un bloque ajeno")
	void obtenerPorIdAjeno() {
		comoDocente();
		when(bloqueService.obtenerEntidad(10L)).thenReturn(bloqueDe(otroDocente, 10L));

		assertThrows(UsuarioSinPermisoException.class, () -> controller.obtenerPorId(10L, autenticacion));
		verify(bloqueService, never()).obtenerPorId(anyLong());
	}

	@Test
	@DisplayName("obtenerPorId() rechaza bloques sin docente asociado")
	void obtenerPorIdSinDocente() {
		comoDocente();
		when(bloqueService.obtenerEntidad(10L)).thenReturn(bloqueDe(null, 10L));

		assertThrows(UsuarioSinPermisoException.class, () -> controller.obtenerPorId(10L, autenticacion));
	}

	@Test
	@DisplayName("obtenerPorId() permite al ADMIN consultar cualquier bloque sin verificar propiedad")
	void obtenerPorIdAdmin() {
		comoAdmin();
		when(bloqueService.obtenerPorId(10L)).thenReturn(dto);

		ResponseEntity<BloqueDisponibilidadDTO> respuesta = controller.obtenerPorId(10L, autenticacion);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		verify(bloqueService, never()).obtenerEntidad(anyLong());
	}

	// ------------------------------------------------------------- crear

	@Test
	@DisplayName("crear() como DOCENTE fuerza el idDocente del usuario autenticado")
	void crearComoDocenteAsignaSuId() {
		comoDocente();
		dto.setIdDocente(2L); // intento de crear un bloque a nombre de otro docente
		BloqueDisponibilidadDTO creado = new BloqueDisponibilidadDTO();
		creado.setId(10L);
		when(bloqueService.crear(dto)).thenReturn(creado);

		ResponseEntity<BloqueDisponibilidadDTO> respuesta = controller.crear(dto, autenticacion);

		assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
		assertSame(creado, respuesta.getBody());
		assertEquals(1L, dto.getIdDocente());
	}

	@Test
	@DisplayName("crear() como ADMIN sin idDocente lanza DatoInvalidoException")
	void crearAdminSinIdDocente() {
		comoAdmin();
		dto.setIdDocente(null);

		assertThrows(DatoInvalidoException.class, () -> controller.crear(dto, autenticacion));
		verify(bloqueService, never()).crear(any());
	}

	@Test
	@DisplayName("crear() como ADMIN con idDocente respeta el docente indicado")
	void crearAdminConIdDocente() {
		comoAdmin();
		dto.setIdDocente(2L);
		when(bloqueService.crear(dto)).thenReturn(dto);

		ResponseEntity<BloqueDisponibilidadDTO> respuesta = controller.crear(dto, autenticacion);

		assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
		assertEquals(2L, dto.getIdDocente());
	}

	// -------------------------------------------------------- actualizar

	@Test
	@DisplayName("actualizar() verifica propiedad y descarta el idDocente enviado")
	void actualizarPropio() {
		comoDocente();
		dto.setIdDocente(55L);
		when(bloqueService.obtenerEntidad(10L)).thenReturn(bloqueDe(docente, 10L));
		when(bloqueService.actualizar(10L, dto)).thenReturn(dto);

		ResponseEntity<BloqueDisponibilidadDTO> respuesta = controller.actualizar(10L, dto, autenticacion);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertNull(dto.getIdDocente());
		verify(bloqueService).actualizar(10L, dto);
	}

	@Test
	@DisplayName("actualizar() rechaza al docente que edita un bloque ajeno")
	void actualizarAjeno() {
		comoDocente();
		when(bloqueService.obtenerEntidad(10L)).thenReturn(bloqueDe(otroDocente, 10L));

		assertThrows(UsuarioSinPermisoException.class, () -> controller.actualizar(10L, dto, autenticacion));
		verify(bloqueService, never()).actualizar(anyLong(), any());
	}

	@Test
	@DisplayName("actualizar() permite al ADMIN editar cualquier bloque")
	void actualizarAdmin() {
		comoAdmin();
		when(bloqueService.actualizar(10L, dto)).thenReturn(dto);

		ResponseEntity<BloqueDisponibilidadDTO> respuesta = controller.actualizar(10L, dto, autenticacion);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		verify(bloqueService, never()).obtenerEntidad(anyLong());
	}

	// --------------------------------------------------------- eliminar

	@Test
	@DisplayName("eliminar() devuelve 204 cuando el bloque es del docente autenticado")
	void eliminarPropio() {
		comoDocente();
		when(bloqueService.obtenerEntidad(10L)).thenReturn(bloqueDe(docente, 10L));

		ResponseEntity<Void> respuesta = controller.eliminar(10L, autenticacion);

		assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
		verify(bloqueService).eliminar(10L);
	}

	@Test
	@DisplayName("eliminar() rechaza al docente que elimina un bloque ajeno")
	void eliminarAjeno() {
		comoDocente();
		when(bloqueService.obtenerEntidad(10L)).thenReturn(bloqueDe(otroDocente, 10L));

		assertThrows(UsuarioSinPermisoException.class, () -> controller.eliminar(10L, autenticacion));
		verify(bloqueService, never()).eliminar(anyLong());
	}

	@Test
	@DisplayName("eliminar() permite al ADMIN eliminar cualquier bloque")
	void eliminarAdmin() {
		comoAdmin();

		ResponseEntity<Void> respuesta = controller.eliminar(10L, autenticacion);

		assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
		verify(bloqueService).eliminar(10L);
	}

}
