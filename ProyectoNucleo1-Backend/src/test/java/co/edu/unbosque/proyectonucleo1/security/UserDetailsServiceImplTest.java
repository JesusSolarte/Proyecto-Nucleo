package co.edu.unbosque.proyectonucleo1.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import co.edu.unbosque.proyectonucleo1.entity.Usuario;
import co.edu.unbosque.proyectonucleo1.repository.UsuarioRepository;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Pruebas unitarias para {@link UserDetailsServiceImpl} con el repositorio mockeado.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl - Pruebas unitarias")
class UserDetailsServiceImplTest {

	@InjectMocks
	private UserDetailsServiceImpl service;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Test
	@DisplayName("loadUserByUsername() devuelve el usuario cuando existe")
	void usuarioExistente() {
		Usuario usuario = new Usuario("Ana", "García", "ana@correo.com", "ana.garcia", "hash", "3001234567",
				RolUsuario.DOCENTE);
		when(usuarioRepository.findByNombreUsuario("ana.garcia")).thenReturn(Optional.of(usuario));

		UserDetails detalles = service.loadUserByUsername("ana.garcia");

		assertSame(usuario, detalles);
		assertEquals("ana.garcia", detalles.getUsername());
		assertEquals("hash", detalles.getPassword());
		verify(usuarioRepository).findByNombreUsuario("ana.garcia");
	}

	@Test
	@DisplayName("loadUserByUsername() lanza UsernameNotFoundException cuando no existe")
	void usuarioInexistente() {
		when(usuarioRepository.findByNombreUsuario("fantasma")).thenReturn(Optional.empty());

		UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
				() -> service.loadUserByUsername("fantasma"));

		assertTrue(ex.getMessage().contains("fantasma"));
	}

}
