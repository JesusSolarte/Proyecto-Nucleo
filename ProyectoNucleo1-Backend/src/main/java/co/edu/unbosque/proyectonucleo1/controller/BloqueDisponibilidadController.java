package co.edu.unbosque.proyectonucleo1.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.proyectonucleo1.dto.BloqueDisponibilidadDTO;
import co.edu.unbosque.proyectonucleo1.entity.BloqueDisponibilidad;
import co.edu.unbosque.proyectonucleo1.entity.Usuario;
import co.edu.unbosque.proyectonucleo1.exception.DatoInvalidoException;
import co.edu.unbosque.proyectonucleo1.exception.UsuarioSinPermisoException;
import co.edu.unbosque.proyectonucleo1.service.BloqueDisponibilidadService;
import co.edu.unbosque.proyectonucleo1.service.UsuarioService;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para que un docente gestione sus propios bloques de
 * disponibilidad. Un administrador puede gestionar los bloques de cualquier
 * docente.
 */
@RestController
@RequestMapping("/api/bloques")
@Tag(name = "Disponibilidad (gestión)", description = "CRUD de bloques de disponibilidad del docente")
public class BloqueDisponibilidadController {

	private final BloqueDisponibilidadService bloqueService;
	private final UsuarioService usuarioService;

	public BloqueDisponibilidadController(BloqueDisponibilidadService bloqueService, UsuarioService usuarioService) {
		this.bloqueService = bloqueService;
		this.usuarioService = usuarioService;
	}

	@GetMapping
	@Operation(summary = "Listar bloques de disponibilidad", description = "El docente ve los suyos; el administrador ve todos.")
	public ResponseEntity<List<BloqueDisponibilidadDTO>> listar(Authentication autenticacion) {
		Usuario actual = usuarioActual(autenticacion);
		if (actual.getRol() == RolUsuario.ADMIN) {
			return new ResponseEntity<>(bloqueService.listar(), HttpStatus.OK);
		}
		return new ResponseEntity<>(bloqueService.listarPorDocente(actual.getId()), HttpStatus.OK);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un bloque por id")
	public ResponseEntity<BloqueDisponibilidadDTO> obtenerPorId(@PathVariable Long id, Authentication autenticacion) {
		verificarPropiedad(id, autenticacion);
		return new ResponseEntity<>(bloqueService.obtenerPorId(id), HttpStatus.OK);
	}

	@PostMapping
	@Operation(summary = "Registrar un bloque de disponibilidad")
	public ResponseEntity<BloqueDisponibilidadDTO> crear(@RequestBody BloqueDisponibilidadDTO bloque,
			Authentication autenticacion) {
		Usuario actual = usuarioActual(autenticacion);
		if (actual.getRol() == RolUsuario.ADMIN) {
			if (bloque.getIdDocente() == null) {
				throw new DatoInvalidoException("El administrador debe indicar el id del docente.");
			}
		} else {
			bloque.setIdDocente(actual.getId());
		}
		return new ResponseEntity<>(bloqueService.crear(bloque), HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar un bloque de disponibilidad")
	public ResponseEntity<BloqueDisponibilidadDTO> actualizar(@PathVariable Long id,
			@RequestBody BloqueDisponibilidadDTO bloque, Authentication autenticacion) {
		verificarPropiedad(id, autenticacion);
		bloque.setIdDocente(null);
		return new ResponseEntity<>(bloqueService.actualizar(id, bloque), HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Eliminar un bloque de disponibilidad")
	public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication autenticacion) {
		verificarPropiedad(id, autenticacion);
		bloqueService.eliminar(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	private Usuario usuarioActual(Authentication autenticacion) {
		if (autenticacion == null || !autenticacion.isAuthenticated()) {
			throw new UsuarioSinPermisoException("Debe iniciar sesión para realizar esta operación.");
		}
		return usuarioService.obtenerEntidadPorNombreUsuario(autenticacion.getName());
	}

	private void verificarPropiedad(Long idBloque, Authentication autenticacion) {
		Usuario actual = usuarioActual(autenticacion);
		if (actual.getRol() == RolUsuario.ADMIN) {
			return;
		}
		BloqueDisponibilidad bloque = bloqueService.obtenerEntidad(idBloque);
		if (bloque.getDocente() == null || !bloque.getDocente().getId().equals(actual.getId())) {
			throw new UsuarioSinPermisoException("El bloque no pertenece al docente autenticado.");
		}
	}

}
