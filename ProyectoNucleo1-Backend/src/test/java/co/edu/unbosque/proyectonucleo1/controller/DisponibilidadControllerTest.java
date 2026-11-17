package co.edu.unbosque.proyectonucleo1.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.unbosque.proyectonucleo1.dto.BloqueDisponibilidadDTO;
import co.edu.unbosque.proyectonucleo1.dto.UsuarioDTO;
import co.edu.unbosque.proyectonucleo1.service.BloqueDisponibilidadService;
import co.edu.unbosque.proyectonucleo1.service.UsuarioService;
import co.edu.unbosque.proyectonucleo1.util.enums.DiaSemana;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Pruebas unitarias para {@link DisponibilidadController} (consulta pública).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DisponibilidadController - Pruebas unitarias")
class DisponibilidadControllerTest {

	@InjectMocks
	private DisponibilidadController controller;

	@Mock
	private BloqueDisponibilidadService bloqueService;

	@Mock
	private UsuarioService usuarioService;

	private UsuarioDTO crearUsuario(Long id, String nombre, String apellido, RolUsuario rol) {
		UsuarioDTO u = new UsuarioDTO();
		u.setId(id);
		u.setNombre(nombre);
		u.setApellido(apellido);
		u.setCorreo(nombre.toLowerCase() + "@correo.com");
		u.setNombreUsuario(nombre.toLowerCase());
		u.setContrasena("secreta123");
		u.setTelefono("3001234567");
		u.setRol(rol);
		return u;
	}

	@Test
	@DisplayName("listarDocentes() devuelve solo usuarios con rol DOCENTE")
	void listarDocentesFiltraPorRol() {
		when(usuarioService.listar()).thenReturn(List.of(
				crearUsuario(1L, "Ana", "García", RolUsuario.DOCENTE),
				crearUsuario(2L, "Admin", "Sistema", RolUsuario.ADMIN),
				crearUsuario(3L, "Luis", "Ríos", RolUsuario.DOCENTE)));

		ResponseEntity<List<UsuarioDTO>> respuesta = controller.listarDocentes();

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertEquals(2, respuesta.getBody().size());
		assertEquals(1L, respuesta.getBody().get(0).getId());
		assertEquals(3L, respuesta.getBody().get(1).getId());
	}

	@Test
	@DisplayName("listarDocentes() no expone datos sensibles (correo, usuario, contraseña, teléfono, rol)")
	void listarDocentesOcultaDatosSensibles() {
		when(usuarioService.listar()).thenReturn(List.of(crearUsuario(1L, "Ana", "García", RolUsuario.DOCENTE)));

		UsuarioDTO docente = controller.listarDocentes().getBody().get(0);

		assertEquals("Ana", docente.getNombre());
		assertEquals("García", docente.getApellido());
		assertNull(docente.getCorreo());
		assertNull(docente.getNombreUsuario());
		assertNull(docente.getContrasena());
		assertNull(docente.getTelefono());
		assertNull(docente.getRol());
	}

	@Test
	@DisplayName("listarDocentes() devuelve lista vacía si solo hay administradores")
	void listarDocentesSinDocentes() {
		when(usuarioService.listar()).thenReturn(List.of(crearUsuario(2L, "Admin", "Sistema", RolUsuario.ADMIN)));

		ResponseEntity<List<UsuarioDTO>> respuesta = controller.listarDocentes();

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertTrue(respuesta.getBody().isEmpty());
	}

	@Test
	@DisplayName("consultarPorDocente() devuelve los bloques del docente")
	void consultarPorDocente() {
		BloqueDisponibilidadDTO bloque = new BloqueDisponibilidadDTO();
		bloque.setId(10L);
		bloque.setIdDocente(1L);
		bloque.setDiaSemana(DiaSemana.LUNES);
		bloque.setHoraInicio(LocalTime.of(8, 0));
		bloque.setHoraFin(LocalTime.of(10, 0));
		List<BloqueDisponibilidadDTO> bloques = List.of(bloque);
		when(bloqueService.listarPorDocente(1L)).thenReturn(bloques);

		ResponseEntity<List<BloqueDisponibilidadDTO>> respuesta = controller.consultarPorDocente(1L);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertSame(bloques, respuesta.getBody());
	}

}
