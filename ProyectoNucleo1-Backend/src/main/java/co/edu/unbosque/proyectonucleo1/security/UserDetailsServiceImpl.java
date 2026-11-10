package co.edu.unbosque.proyectonucleo1.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.unbosque.proyectonucleo1.repository.UsuarioRepository;

/**
 * Carga los datos de un usuario a partir de su nombre de usuario, para que
 * Spring Security pueda autenticarlo.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
		return usuarioRepository.findByNombreUsuario(nombreUsuario).orElseThrow(
				() -> new UsernameNotFoundException("Usuario no encontrado con nombre: " + nombreUsuario));
	}

}
