package co.edu.unbosque.proyectonucleo1.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import co.edu.unbosque.proyectonucleo1.dto.UsuarioDTO;
import co.edu.unbosque.proyectonucleo1.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectonucleo1.service.UsuarioService;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Pruebas unitarias para {@link UsuarioController} con el servicio mockeado.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController - Pruebas unitarias")
class UsuarioControllerTest {

	@InjectMocks
	private UsuarioController controller;

	@Mock
	private UsuarioService usuarioService;

	private UsuarioDTO dto;

	@BeforeEach
	void setUp() {
		dto = new UsuarioDTO();
		dto.setId(1L);
		dto.setNombre("Ana");
		dto.setApellido("García");
		dto.setCorreo("ana@correo.com");
		dto.setNombreUsuario("ana.garcia");
		dto.setRol(RolUsuario.DOCENTE);
	}

	@Test
	@DisplayName("listar() devuelve 200 con todos los usuarios")
	void listar() {
		when(usuarioService.listar()).thenReturn(List.of(dto));

		ResponseEntity<List<UsuarioDTO>> respuesta = controller.listar();

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertEquals(1, respuesta.getBody().size());
		assertSame(dto, respuesta.getBody().get(0));
	}

	@Test
	@DisplayName("listar() devuelve lista vacía cuando no hay usuarios")
	void listarVacio() {
		when(usuarioService.listar()).thenReturn(List.of());

		ResponseEntity<List<UsuarioDTO>> respuesta = controller.listar();

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertTrue(respuesta.getBody().isEmpty());
	}

	@Test
	@DisplayName("obtenerPorId() devuelve 200 con el usuario")
	void obtenerPorId() {
		when(usuarioService.obtenerPorId(1L)).thenReturn(dto);

		ResponseEntity<UsuarioDTO> respuesta = controller.obtenerPorId(1L);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertSame(dto, respuesta.getBody());
	}

	@Test
	@DisplayName("obtenerPorId() propaga RecursoNoEncontradoException")
	void obtenerPorIdNoExiste() {
		when(usuarioService.obtenerPorId(99L)).thenThrow(new RecursoNoEncontradoException("No existe"));

		assertThrows(RecursoNoEncontradoException.class, () -> controller.obtenerPorId(99L));
	}

	@Test
	@DisplayName("crear() devuelve 201 con el usuario creado")
	void crear() {
		when(usuarioService.crear(dto)).thenReturn(dto);

		ResponseEntity<UsuarioDTO> respuesta = controller.crear(dto);

		assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
		assertSame(dto, respuesta.getBody());
		verify(usuarioService).crear(dto);
	}

	@Test
	@DisplayName("actualizar() devuelve 200 con el usuario actualizado")
	void actualizar() {
		when(usuarioService.actualizar(1L, dto)).thenReturn(dto);

		ResponseEntity<UsuarioDTO> respuesta = controller.actualizar(1L, dto);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertSame(dto, respuesta.getBody());
		verify(usuarioService).actualizar(1L, dto);
	}

	@Test
	@DisplayName("eliminar() devuelve 204 sin cuerpo y delega en el servicio")
	void eliminar() {
		ResponseEntity<Void> respuesta = controller.eliminar(1L);

		assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
		assertNull(respuesta.getBody());
		verify(usuarioService).eliminar(1L);
	}

}
