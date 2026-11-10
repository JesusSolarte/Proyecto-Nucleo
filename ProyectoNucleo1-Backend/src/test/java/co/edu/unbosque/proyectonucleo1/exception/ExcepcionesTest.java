package co.edu.unbosque.proyectonucleo1.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Pruebas unitarias para las excepciones de negocio y el manejador global.
 */
@DisplayName("Excepciones y manejador global")
class ExcepcionesTest {

	@Test
	@DisplayName("Las excepciones conservan su mensaje")
	void mensajes() {
		assertEquals("a", new RecursoNoEncontradoException("a").getMessage());
		assertEquals("b", new DatoInvalidoException("b").getMessage());
		assertEquals("c", new CruceHorarioException("c").getMessage());
		assertEquals("d", new RangoHorarioInvalidoException("d").getMessage());
		assertEquals("e", new CredencialesInvalidasException("e").getMessage());
		assertEquals("f", new UsuarioSinPermisoException("f").getMessage());
		assertEquals("g", new CorreoDuplicadoException("g").getMessage());
		assertEquals("h", new NombreUsuarioDuplicadoException("h").getMessage());
	}

	@Test
	@DisplayName("manejarNoEncontrado devuelve 404")
	void noEncontrado() {
		LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
		ResponseEntity<Map<String, Object>> respuesta = lanzador
				.manejarNoEncontrado(new RecursoNoEncontradoException("No existe"));
		assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
		assertEquals("No existe", respuesta.getBody().get("mensaje"));
	}

	@Test
	@DisplayName("manejarCruce devuelve 409")
	void cruce() {
		LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
		ResponseEntity<Map<String, Object>> respuesta = lanzador.manejarCruce(new CruceHorarioException("Cruce"));
		assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
		assertNotNull(respuesta.getBody());
	}

	@Test
	@DisplayName("manejarCredenciales devuelve 401")
	void credenciales() {
		LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
		ResponseEntity<Map<String, Object>> respuesta = lanzador
				.manejarCredenciales(new CredencialesInvalidasException("Credenciales"));
		assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
	}

	@Test
	@DisplayName("manejarValidacion devuelve 400")
	void validacion() {
		LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
		ResponseEntity<Map<String, Object>> respuesta = lanzador
				.manejarValidacion(new TextoVacioException("Vacío"));
		assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
	}

}
