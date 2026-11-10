package co.edu.unbosque.proyectonucleo1.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones. Traduce las excepciones de negocio a
 * respuestas HTTP con un cuerpo uniforme.
 */
@RestControllerAdvice
public class LanzadorDeExcepcion {

	private ResponseEntity<Map<String, Object>> construir(HttpStatus estado, String mensaje) {
		Map<String, Object> cuerpo = new LinkedHashMap<>();
		cuerpo.put("fecha", LocalDateTime.now());
		cuerpo.put("estado", estado.value());
		cuerpo.put("mensaje", mensaje);
		return new ResponseEntity<>(cuerpo, estado);
	}

	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ResponseEntity<Map<String, Object>> manejarNoEncontrado(RecursoNoEncontradoException ex) {
		return construir(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(CruceHorarioException.class)
	public ResponseEntity<Map<String, Object>> manejarCruce(CruceHorarioException ex) {
		return construir(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler({ CorreoDuplicadoException.class, NombreUsuarioDuplicadoException.class })
	public ResponseEntity<Map<String, Object>> manejarDuplicado(RuntimeException ex) {
		return construir(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(CredencialesInvalidasException.class)
	public ResponseEntity<Map<String, Object>> manejarCredenciales(CredencialesInvalidasException ex) {
		return construir(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler(UsuarioSinPermisoException.class)
	public ResponseEntity<Map<String, Object>> manejarPermiso(UsuarioSinPermisoException ex) {
		return construir(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> manejarAccesoDenegado(AccessDeniedException ex) {
		return construir(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta acción.");
	}

	@ExceptionHandler({ DatoInvalidoException.class, IdInvalidoException.class, TextoVacioException.class,
			TextoDemasiadoLargoException.class, OpcionNoValidaException.class, NumeroNegativoException.class,
			NumeroDemasiadoGrandeException.class, ContrasenaInvalidaException.class, NombreInvalidoException.class,
			FormatoCorreoInvalidoException.class, TelefonoInvalidoException.class, RangoHorarioInvalidoException.class })
	public ResponseEntity<Map<String, Object>> manejarValidacion(RuntimeException ex) {
		return construir(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> manejarGeneral(Exception ex) {
		return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor: " + ex.getMessage());
	}

}
