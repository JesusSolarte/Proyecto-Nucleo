package co.edu.unbosque.proyectonucleo1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.proyectonucleo1.dto.UsuarioDTO;
import co.edu.unbosque.proyectonucleo1.entity.Usuario;
import co.edu.unbosque.proyectonucleo1.exception.ContrasenaInvalidaException;
import co.edu.unbosque.proyectonucleo1.exception.CorreoDuplicadoException;
import co.edu.unbosque.proyectonucleo1.exception.CredencialesInvalidasException;
import co.edu.unbosque.proyectonucleo1.exception.FormatoCorreoInvalidoException;
import co.edu.unbosque.proyectonucleo1.exception.IdInvalidoException;
import co.edu.unbosque.proyectonucleo1.exception.NombreUsuarioDuplicadoException;
import co.edu.unbosque.proyectonucleo1.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectonucleo1.exception.TextoVacioException;
import co.edu.unbosque.proyectonucleo1.repository.UsuarioRepository;
import co.edu.unbosque.proyectonucleo1.security.JwtUtil;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Pruebas unitarias para {@link UsuarioService} con dependencias mockeadas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Pruebas unitarias")
class UsuarioServiceTest {

	@InjectMocks
	private UsuarioService service;

	@Mock
	private UsuarioRepository repo;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private JwtUtil jwtUtil;

	private UsuarioDTO dto;
	private Usuario entidad;

	@BeforeEach
	void setUp() {
		dto = new UsuarioDTO();
		dto.setNombre("Ana");
		dto.setApellido("García");
		dto.setCorreo("ana@correo.com");
		dto.setNombreUsuario("ana.garcia");
		dto.setContrasena("Clave1234");
		dto.setTelefono("3001234567");

		entidad = new Usuario("Ana", "García", "ana@correo.com", "ana.garcia", "hash", "3001234567",
				RolUsuario.DOCENTE);
		entidad.setId(1L);
	}

	@Test
	@DisplayName("crear() con datos válidos guarda el usuario")
	void crearValido() {
		lenient().when(passwordEncoder.encode(anyString())).thenReturn("hashBCrypt");
		UsuarioDTO resultado = service.crear(dto);
		assertNotNull(resultado);
		verify(repo, times(1)).save(any(Usuario.class));
	}

	@Test
	@DisplayName("crear() con nombre vacío lanza TextoVacioException")
	void crearNombreVacio() {
		dto.setNombre("");
		assertThrows(TextoVacioException.class, () -> service.crear(dto));
		verify(repo, never()).save(any());
	}

	@Test
	@DisplayName("crear() con correo inválido lanza FormatoCorreoInvalidoException")
	void crearCorreoInvalido() {
		dto.setCorreo("no-es-correo");
		assertThrows(FormatoCorreoInvalidoException.class, () -> service.crear(dto));
	}

	@Test
	@DisplayName("crear() con contraseña corta lanza ContrasenaInvalidaException")
	void crearContrasenaCorta() {
		dto.setContrasena("abc");
		assertThrows(ContrasenaInvalidaException.class, () -> service.crear(dto));
	}

	@Test
	@DisplayName("crear() con usuario duplicado lanza NombreUsuarioDuplicadoException")
	void crearUsuarioDuplicado() {
		when(repo.existsByNombreUsuario("ana.garcia")).thenReturn(true);
		assertThrows(NombreUsuarioDuplicadoException.class, () -> service.crear(dto));
		verify(repo, never()).save(any());
	}

	@Test
	@DisplayName("crear() con correo duplicado lanza CorreoDuplicadoException")
	void crearCorreoDuplicado() {
		when(repo.existsByCorreo("ana@correo.com")).thenReturn(true);
		assertThrows(CorreoDuplicadoException.class, () -> service.crear(dto));
		verify(repo, never()).save(any());
	}

	@Test
	@DisplayName("listar() devuelve todos los usuarios")
	void listar() {
		when(repo.findAll()).thenReturn(List.of(entidad));
		List<UsuarioDTO> resultado = service.listar();
		assertEquals(1, resultado.size());
		assertEquals("Ana", resultado.get(0).getNombre());
	}

	@Test
	@DisplayName("obtenerPorId() devuelve el usuario existente")
	void obtenerPorId() {
		when(repo.findById(1L)).thenReturn(Optional.of(entidad));
		assertEquals("ana.garcia", service.obtenerPorId(1L).getNombreUsuario());
	}

	@Test
	@DisplayName("obtenerPorId() con id inválido lanza IdInvalidoException")
	void obtenerPorIdInvalido() {
		assertThrows(IdInvalidoException.class, () -> service.obtenerPorId(0L));
	}

	@Test
	@DisplayName("obtenerPorId() inexistente lanza RecursoNoEncontradoException")
	void obtenerPorIdInexistente() {
		when(repo.findById(99L)).thenReturn(Optional.empty());
		assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(99L));
	}

	@Test
	@DisplayName("eliminar() elimina el usuario existente")
	void eliminar() {
		when(repo.findById(1L)).thenReturn(Optional.of(entidad));
		service.eliminar(1L);
		verify(repo, times(1)).delete(entidad);
	}

	@Test
	@DisplayName("iniciarSesion() correcto devuelve token y usuario")
	void iniciarSesionCorrecto() {
		when(repo.findByNombreUsuario("ana.garcia")).thenReturn(Optional.of(entidad));
		when(jwtUtil.generarToken(anyString(), anyString())).thenReturn("token.jwt");

		Map<String, Object> resultado = service.iniciarSesion("ana.garcia", "Clave1234");

		assertEquals("token.jwt", resultado.get("token"));
		assertNotNull(resultado.get("usuario"));
	}

	@Test
	@DisplayName("iniciarSesion() con credenciales incorrectas lanza CredencialesInvalidasException")
	void iniciarSesionIncorrecto() {
		when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("mal"));
		assertThrows(CredencialesInvalidasException.class, () -> service.iniciarSesion("ana.garcia", "malaClave"));
	}

	@Test
	@DisplayName("iniciarSesion() sin datos lanza CredencialesInvalidasException")
	void iniciarSesionSinDatos() {
		assertThrows(CredencialesInvalidasException.class, () -> service.iniciarSesion("", ""));
	}

}
