package co.edu.unbosque.proyectonucleo1.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.proyectonucleo1.entity.BloqueDisponibilidad;
import co.edu.unbosque.proyectonucleo1.entity.Usuario;
import co.edu.unbosque.proyectonucleo1.repository.BloqueDisponibilidadRepository;
import co.edu.unbosque.proyectonucleo1.repository.UsuarioRepository;
import co.edu.unbosque.proyectonucleo1.util.enums.DiaSemana;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Pruebas unitarias para {@link DataInitializer} con repositorios y
 * codificador de contraseñas mockeados.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DataInitializer - Pruebas unitarias")
class DataInitializerTest {

	@InjectMocks
	private DataInitializer initializer;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private BloqueDisponibilidadRepository bloqueRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Captor
	private ArgumentCaptor<Usuario> usuarioCaptor;

	@Captor
	private ArgumentCaptor<Iterable<BloqueDisponibilidad>> bloquesCaptor;

	@Test
	@DisplayName("run() crea admin, docente de ejemplo y sus bloques cuando la BD está vacía")
	void creaTodoCuandoNoExiste() {
		when(usuarioRepository.existsByNombreUsuario("admin")).thenReturn(false);
		when(usuarioRepository.existsByNombreUsuario("docente")).thenReturn(false);
		when(passwordEncoder.encode("Admin12345")).thenReturn("hashAdmin");
		when(passwordEncoder.encode("Docente123")).thenReturn("hashDocente");
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

		initializer.run();

		verify(usuarioRepository, times(2)).save(usuarioCaptor.capture());
		List<Usuario> guardados = usuarioCaptor.getAllValues();

		Usuario admin = guardados.get(0);
		assertEquals("admin", admin.getNombreUsuario());
		assertEquals("hashAdmin", admin.getContrasena());
		assertEquals(RolUsuario.ADMIN, admin.getRol());

		Usuario docente = guardados.get(1);
		assertEquals("docente", docente.getNombreUsuario());
		assertEquals("hashDocente", docente.getContrasena());
		assertEquals(RolUsuario.DOCENTE, docente.getRol());

		verify(bloqueRepository).saveAll(bloquesCaptor.capture());
		List<BloqueDisponibilidad> bloques = new ArrayList<>();
		bloquesCaptor.getValue().forEach(bloques::add);

		assertEquals(2, bloques.size());
		assertSame(docente, bloques.get(0).getDocente());
		assertEquals(DiaSemana.LUNES, bloques.get(0).getDiaSemana());
		assertEquals(LocalTime.of(8, 0), bloques.get(0).getHoraInicio());
		assertEquals(LocalTime.of(10, 0), bloques.get(0).getHoraFin());
		assertSame(docente, bloques.get(1).getDocente());
		assertEquals(DiaSemana.MIERCOLES, bloques.get(1).getDiaSemana());
		assertEquals(LocalTime.of(14, 0), bloques.get(1).getHoraInicio());
		assertEquals(LocalTime.of(16, 0), bloques.get(1).getHoraFin());
	}

	@Test
	@DisplayName("run() no crea nada cuando admin y docente ya existen")
	void noCreaNadaCuandoYaExisten() {
		when(usuarioRepository.existsByNombreUsuario("admin")).thenReturn(true);
		when(usuarioRepository.existsByNombreUsuario("docente")).thenReturn(true);

		initializer.run();

		verify(usuarioRepository, never()).save(any());
		verifyNoInteractions(bloqueRepository, passwordEncoder);
	}

	@Test
	@DisplayName("run() solo crea el docente y sus bloques si el admin ya existe")
	void soloCreaDocente() {
		when(usuarioRepository.existsByNombreUsuario("admin")).thenReturn(true);
		when(usuarioRepository.existsByNombreUsuario("docente")).thenReturn(false);
		when(passwordEncoder.encode("Docente123")).thenReturn("hashDocente");
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

		initializer.run();

		verify(usuarioRepository, times(1)).save(usuarioCaptor.capture());
		assertEquals("docente", usuarioCaptor.getValue().getNombreUsuario());
		verify(bloqueRepository).saveAll(any());
	}

	@Test
	@DisplayName("run() solo crea el admin si el docente de ejemplo ya existe")
	void soloCreaAdmin() {
		when(usuarioRepository.existsByNombreUsuario("admin")).thenReturn(false);
		when(usuarioRepository.existsByNombreUsuario("docente")).thenReturn(true);
		when(passwordEncoder.encode("Admin12345")).thenReturn("hashAdmin");

		initializer.run();

		verify(usuarioRepository, times(1)).save(usuarioCaptor.capture());
		assertEquals("admin", usuarioCaptor.getValue().getNombreUsuario());
		assertEquals(RolUsuario.ADMIN, usuarioCaptor.getValue().getRol());
		verify(bloqueRepository, never()).saveAll(any());
	}

}
