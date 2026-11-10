package co.edu.unbosque.proyectonucleo1.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectonucleo1.dto.UsuarioDTO;
import co.edu.unbosque.proyectonucleo1.entity.Usuario;
import co.edu.unbosque.proyectonucleo1.exception.ContrasenaInvalidaException;
import co.edu.unbosque.proyectonucleo1.exception.CorreoDuplicadoException;
import co.edu.unbosque.proyectonucleo1.exception.CredencialesInvalidasException;
import co.edu.unbosque.proyectonucleo1.exception.FormatoCorreoInvalidoException;
import co.edu.unbosque.proyectonucleo1.exception.IdInvalidoException;
import co.edu.unbosque.proyectonucleo1.exception.NombreInvalidoException;
import co.edu.unbosque.proyectonucleo1.exception.NombreUsuarioDuplicadoException;
import co.edu.unbosque.proyectonucleo1.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectonucleo1.exception.TelefonoInvalidoException;
import co.edu.unbosque.proyectonucleo1.exception.TextoDemasiadoLargoException;
import co.edu.unbosque.proyectonucleo1.exception.TextoVacioException;
import co.edu.unbosque.proyectonucleo1.repository.UsuarioRepository;
import co.edu.unbosque.proyectonucleo1.security.JwtUtil;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Servicio de gestión de usuarios (docentes y administradores) y de
 * autenticación.
 */
@Service
public class UsuarioService implements CRUDoperation<UsuarioDTO> {

	private static final String PATRON_CORREO = "^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
	private static final String PATRON_TELEFONO = "^[0-9]{7,15}$";
	private static final String PATRON_NOMBRE = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]{2,60}$";
	private static final int LONGITUD_MINIMA_CONTRASENA = 8;

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	@Override
	@Transactional
	public UsuarioDTO crear(UsuarioDTO dato) {
		return crearConRol(dato, null);
	}

	/**
	 * Registra un nuevo docente. El rol siempre es DOCENTE.
	 */
	@Transactional
	public UsuarioDTO registrarDocente(UsuarioDTO dato) {
		return crearConRol(dato, RolUsuario.DOCENTE);
	}

	private UsuarioDTO crearConRol(UsuarioDTO dato, RolUsuario rolForzado) {
		if (dato == null) {
			throw new TextoVacioException("Los datos del usuario no pueden ser nulos.");
		}
		validar(dato, true);

		if (usuarioRepository.existsByNombreUsuario(dato.getNombreUsuario())) {
			throw new NombreUsuarioDuplicadoException(
					"El nombre de usuario '" + dato.getNombreUsuario() + "' ya está registrado.");
		}
		if (usuarioRepository.existsByCorreo(dato.getCorreo())) {
			throw new CorreoDuplicadoException("El correo '" + dato.getCorreo() + "' ya está registrado.");
		}

		Usuario usuario = new Usuario(dato.getNombre().trim(), dato.getApellido().trim(), dato.getCorreo().trim(),
				dato.getNombreUsuario().trim(), passwordEncoder.encode(dato.getContrasena()), dato.getTelefono(),
				rolForzado != null ? rolForzado : (dato.getRol() != null ? dato.getRol() : RolUsuario.DOCENTE));
		usuarioRepository.save(usuario);
		return convertirADTO(usuario);
	}

	@Override
	public List<UsuarioDTO> listar() {
		List<UsuarioDTO> resultado = new ArrayList<>();
		usuarioRepository.findAll().forEach(usuario -> resultado.add(convertirADTO(usuario)));
		return resultado;
	}

	@Override
	public UsuarioDTO obtenerPorId(Long id) {
		return convertirADTO(obtenerEntidad(id));
	}

	@Override
	@Transactional
	public UsuarioDTO actualizar(Long id, UsuarioDTO dato) {
		Usuario usuario = obtenerEntidad(id);
		if (dato == null) {
			throw new TextoVacioException("Los datos del usuario no pueden ser nulos.");
		}
		validar(dato, false);

		if (dato.getNombreUsuario() != null && !dato.getNombreUsuario().equals(usuario.getNombreUsuario())) {
			if (usuarioRepository.existsByNombreUsuario(dato.getNombreUsuario())) {
				throw new NombreUsuarioDuplicadoException(
						"El nombre de usuario '" + dato.getNombreUsuario() + "' ya está registrado.");
			}
			usuario.setNombreUsuario(dato.getNombreUsuario().trim());
		}
		if (dato.getCorreo() != null && !dato.getCorreo().equals(usuario.getCorreo())) {
			if (usuarioRepository.existsByCorreo(dato.getCorreo())) {
				throw new CorreoDuplicadoException("El correo '" + dato.getCorreo() + "' ya está registrado.");
			}
			usuario.setCorreo(dato.getCorreo().trim());
		}
		if (dato.getNombre() != null && !dato.getNombre().isBlank()) {
			usuario.setNombre(dato.getNombre().trim());
		}
		if (dato.getApellido() != null && !dato.getApellido().isBlank()) {
			usuario.setApellido(dato.getApellido().trim());
		}
		if (dato.getTelefono() != null && !dato.getTelefono().isBlank()) {
			usuario.setTelefono(dato.getTelefono());
		}
		if (dato.getRol() != null) {
			usuario.setRol(dato.getRol());
		}
		if (dato.getContrasena() != null && !dato.getContrasena().isBlank()) {
			validarContrasena(dato.getContrasena());
			usuario.setContrasena(passwordEncoder.encode(dato.getContrasena()));
		}

		usuarioRepository.save(usuario);
		return convertirADTO(usuario);
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		Usuario usuario = obtenerEntidad(id);
		usuarioRepository.delete(usuario);
	}

	/**
	 * Inicia sesión validando las credenciales y generando un token JWT.
	 *
	 * @return mapa con el token y los datos del usuario autenticado
	 */
	public Map<String, Object> iniciarSesion(String nombreUsuario, String contrasena) {
		if (nombreUsuario == null || nombreUsuario.isBlank() || contrasena == null || contrasena.isBlank()) {
			throw new CredencialesInvalidasException("Debe ingresar usuario y contraseña.");
		}
		try {
			authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(nombreUsuario.trim(), contrasena));
		} catch (BadCredentialsException ex) {
			throw new CredencialesInvalidasException("Usuario o contraseña incorrectos.");
		}
		Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario.trim())
				.orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));

		String token = jwtUtil.generarToken(usuario.getNombreUsuario(), usuario.getRol().name());
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("token", token);
		respuesta.put("tipoToken", "Bearer");
		respuesta.put("usuario", convertirADTO(usuario));
		return respuesta;
	}

	public Usuario obtenerEntidad(Long id) {
		if (id == null || id <= 0) {
			throw new IdInvalidoException("El identificador debe ser un número positivo.");
		}
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con el id " + id + "."));
	}

	public Usuario obtenerEntidadPorNombreUsuario(String nombreUsuario) {
		return usuarioRepository.findByNombreUsuario(nombreUsuario).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe un usuario con el nombre " + nombreUsuario + "."));
	}

	public UsuarioDTO convertirADTO(Usuario usuario) {
		UsuarioDTO dto = new UsuarioDTO();
		dto.setId(usuario.getId());
		dto.setNombre(usuario.getNombre());
		dto.setApellido(usuario.getApellido());
		dto.setCorreo(usuario.getCorreo());
		dto.setNombreUsuario(usuario.getNombreUsuario());
		dto.setTelefono(usuario.getTelefono());
		dto.setRol(usuario.getRol());
		return dto;
	}

	private void validar(UsuarioDTO dato, boolean exigirContrasena) {
		if (dato.getNombre() == null || dato.getNombre().isBlank()) {
			throw new TextoVacioException("El nombre es obligatorio.");
		}
		if (!dato.getNombre().matches(PATRON_NOMBRE)) {
			throw new NombreInvalidoException("El nombre contiene caracteres no permitidos.");
		}
		if (dato.getApellido() == null || dato.getApellido().isBlank()) {
			throw new TextoVacioException("El apellido es obligatorio.");
		}
		if (!dato.getApellido().matches(PATRON_NOMBRE)) {
			throw new NombreInvalidoException("El apellido contiene caracteres no permitidos.");
		}
		if (dato.getCorreo() == null || dato.getCorreo().isBlank()) {
			throw new TextoVacioException("El correo es obligatorio.");
		}
		if (!dato.getCorreo().matches(PATRON_CORREO)) {
			throw new FormatoCorreoInvalidoException("El correo no tiene un formato válido.");
		}
		if (dato.getNombreUsuario() == null || dato.getNombreUsuario().isBlank()) {
			throw new TextoVacioException("El nombre de usuario es obligatorio.");
		}
		if (dato.getNombreUsuario().length() > 40) {
			throw new TextoDemasiadoLargoException("El nombre de usuario no puede superar 40 caracteres.");
		}
		if (dato.getTelefono() != null && !dato.getTelefono().isBlank()
				&& !dato.getTelefono().matches(PATRON_TELEFONO)) {
			throw new TelefonoInvalidoException("El teléfono debe contener entre 7 y 15 dígitos.");
		}
		if (exigirContrasena) {
			validarContrasena(dato.getContrasena());
		}
	}

	private void validarContrasena(String contrasena) {
		if (contrasena == null || contrasena.isBlank()) {
			throw new ContrasenaInvalidaException("La contraseña es obligatoria.");
		}
		if (contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
			throw new ContrasenaInvalidaException(
					"La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres.");
		}
	}

}
