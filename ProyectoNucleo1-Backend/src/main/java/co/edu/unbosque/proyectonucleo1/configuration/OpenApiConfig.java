package co.edu.unbosque.proyectonucleo1.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Configuración de la documentación OpenAPI (Swagger UI).
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI apiPersonalizada() {
		final String esquemaSeguridad = "bearerAuth";
		return new OpenAPI()
				.info(new Info().title("Proyecto Núcleo 1 - Disponibilidad Docente")
						.description("API REST para la gestión de la disponibilidad horaria semanal de los docentes.")
						.version("1.0.0")
						.contact(new Contact().name("P.N.1 - Universidad El Bosque").email("pn1@unbosque.edu.co"))
						.license(new License().name("Uso académico")))
				.components(new Components().addSecuritySchemes(esquemaSeguridad, new SecurityScheme()
						.name(esquemaSeguridad).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList(esquemaSeguridad));
	}

}
