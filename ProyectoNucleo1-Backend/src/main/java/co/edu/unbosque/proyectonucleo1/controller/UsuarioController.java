package co.edu.unbosque.proyectonucleo1.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.proyectonucleo1.dto.UsuarioDTO;
import co.edu.unbosque.proyectonucleo1.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador de administración de usuarios (solo ADMIN).
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Administración de usuarios del sistema")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping
	@Operation(summary = "Listar usuarios")
	public ResponseEntity<List<UsuarioDTO>> listar() {
		return new ResponseEntity<>(usuarioService.listar(), HttpStatus.OK);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un usuario por id")
	public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable Long id) {
		return new ResponseEntity<>(usuarioService.obtenerPorId(id), HttpStatus.OK);
	}

	@PostMapping
	@Operation(summary = "Crear un usuario")
	public ResponseEntity<UsuarioDTO> crear(@RequestBody UsuarioDTO usuario) {
		return new ResponseEntity<>(usuarioService.crear(usuario), HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar un usuario")
	public ResponseEntity<UsuarioDTO> actualizar(@PathVariable Long id, @RequestBody UsuarioDTO usuario) {
		return new ResponseEntity<>(usuarioService.actualizar(id, usuario), HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Eliminar un usuario")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		usuarioService.eliminar(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
