package co.edu.unbosque.proyectonucleo1.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.proyectonucleo1.dto.BloqueDisponibilidadDTO;
import co.edu.unbosque.proyectonucleo1.dto.UsuarioDTO;
import co.edu.unbosque.proyectonucleo1.service.BloqueDisponibilidadService;
import co.edu.unbosque.proyectonucleo1.service.UsuarioService;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador de consulta pública de la disponibilidad de un docente. Permite a
 * terceros (estudiantes, otros docentes) consultar los horarios disponibles.
 */
@RestController
@RequestMapping("/api/disponibilidad")
@Tag(name = "Disponibilidad (consulta)", description = "Consulta pública de la disponibilidad de un docente")
public class DisponibilidadController {

	private final BloqueDisponibilidadService bloqueService;
	private final UsuarioService usuarioService;

	public DisponibilidadController(BloqueDisponibilidadService bloqueService, UsuarioService usuarioService) {
		this.bloqueService = bloqueService;
		this.usuarioService = usuarioService;
	}

	@GetMapping("/docentes")
	@Operation(summary = "Listar docentes", description = "Devuelve los docentes disponibles para consultar.")
	public ResponseEntity<List<UsuarioDTO>> listarDocentes() {
		List<UsuarioDTO> docentes = new ArrayList<>();
		for (UsuarioDTO usuario : usuarioService.listar()) {
			if (usuario.getRol() == RolUsuario.DOCENTE) {
				UsuarioDTO docente = new UsuarioDTO();
				docente.setId(usuario.getId());
				docente.setNombre(usuario.getNombre());
				docente.setApellido(usuario.getApellido());
				docentes.add(docente);
			}
		}
		return new ResponseEntity<>(docentes, HttpStatus.OK);
	}

	@GetMapping("/docente/{idDocente}")
	@Operation(summary = "Consultar la disponibilidad de un docente", description = "Devuelve los bloques de disponibilidad semanal del docente indicado.")
	public ResponseEntity<List<BloqueDisponibilidadDTO>> consultarPorDocente(@PathVariable Long idDocente) {
		return new ResponseEntity<>(bloqueService.listarPorDocente(idDocente), HttpStatus.OK);
	}

}
