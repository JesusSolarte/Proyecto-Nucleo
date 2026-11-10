package co.edu.unbosque.proyectonucleo1;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Pruebas de arranque de la aplicación Spring Boot.
 */
@SpringBootTest
class ProyectoNucleo1ApplicationTests {

	@Autowired
	private ApplicationContext context;

	@Test
	void contextLoads() {
		assertNotNull(context, "El contexto de Spring no debería ser nulo.");
	}

}
