package co.edu.unbosque.proyectonucleo1.configuration;

import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import co.edu.unbosque.proyectonucleo1.entity.BloqueDisponibilidad;
import co.edu.unbosque.proyectonucleo1.entity.Usuario;
import co.edu.unbosque.proyectonucleo1.repository.BloqueDisponibilidadRepository;
import co.edu.unbosque.proyectonucleo1.repository.UsuarioRepository;
import co.edu.unbosque.proyectonucleo1.util.enums.DiaSemana;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Carga datos iniciales: un administrador y un docente de ejemplo con
 * disponibilidad registrada.
 */
@Component
public class DataInitializer implements CommandLineRunner {

	@Value("${app.admin.nombre}")
	private String adminNombre;
	@Value("${app.admin.apellido}")
	private String adminApellido;
	@Value("${app.admin.correo}")
	private String adminCorreo;
	@Value("${app.admin.nombre-usuario}")
	private String adminNombreUsuario;
	@Value("${app.admin.contrasena}")
	private String adminContrasena;
	@Value("${app.admin.telefono}")
	private String adminTelefono;

	private final UsuarioRepository usuarioRepository;
	private final BloqueDisponibilidadRepository bloqueRepository;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(UsuarioRepository usuarioRepository, BloqueDisponibilidadRepository bloqueRepository,
			PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.bloqueRepository = bloqueRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		if (!usuarioRepository.existsByNombreUsuario(adminNombreUsuario)) {
			usuarioRepository.save(new Usuario(adminNombre, adminApellido, adminCorreo, adminNombreUsuario,
					passwordEncoder.encode(adminContrasena), adminTelefono, RolUsuario.ADMIN));
		}

		if (!usuarioRepository.existsByNombreUsuario("docente")) {
			Usuario docente = usuarioRepository.save(new Usuario("Carlos", "Docente Ejemplo",
					"docente@proyectonucleo1.local", "docente", passwordEncoder.encode("Docente123"),
					"3100000000", RolUsuario.DOCENTE));
			bloqueRepository.saveAll(List.of(
					new BloqueDisponibilidad(docente, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(10, 0)),
					new BloqueDisponibilidad(docente, DiaSemana.MIERCOLES, LocalTime.of(14, 0),
							LocalTime.of(16, 0))));
		}
	}

}
