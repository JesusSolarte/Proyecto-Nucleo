package co.edu.unbosque.proyectonucleo1.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import co.edu.unbosque.proyectonucleo1.entity.Usuario;
import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;
import jakarta.servlet.FilterChain;

/**
 * Pruebas unitarias para {@link JwtAuthenticationFilter} con sus dependencias
 * mockeadas y objetos servlet simulados de Spring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter - Pruebas unitarias")
class JwtAuthenticationFilterTest {

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private UserDetailsServiceImpl userDetailsService;

	@Mock
	private FilterChain filterChain;

	private JwtAuthenticationFilter filter;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private Usuario usuario;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.clearContext();
		filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		usuario = new Usuario("Ana", "García", "ana@correo.com", "ana.garcia", "hash", "3001234567",
				RolUsuario.DOCENTE);
		usuario.setId(1L);
	}

	@AfterEach
	void limpiarContexto() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("Sin encabezado Authorization continúa la cadena sin autenticar")
	void sinEncabezado() throws Exception {
		filter.doFilter(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
		verifyNoInteractions(jwtUtil, userDetailsService);
	}

	@Test
	@DisplayName("Encabezado sin prefijo 'Bearer ' se ignora")
	void encabezadoSinBearer() throws Exception {
		request.addHeader("Authorization", "Basic YW5hOmNsYXZl");

		filter.doFilter(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
		verifyNoInteractions(jwtUtil, userDetailsService);
	}

	@Test
	@DisplayName("Token válido autentica al usuario en el SecurityContext")
	void tokenValido() throws Exception {
		request.addHeader("Authorization", "Bearer tok123");
		when(jwtUtil.extraerNombreUsuario("tok123")).thenReturn("ana.garcia");
		when(userDetailsService.loadUserByUsername("ana.garcia")).thenReturn(usuario);
		when(jwtUtil.validarToken("tok123", usuario)).thenReturn(true);

		filter.doFilter(request, response, filterChain);

		Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
		assertNotNull(autenticacion);
		assertSame(usuario, autenticacion.getPrincipal());
		assertTrue(autenticacion.isAuthenticated());
		assertEquals("ROLE_DOCENTE", autenticacion.getAuthorities().iterator().next().getAuthority());
		assertNotNull(autenticacion.getDetails());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Token que no valida contra el usuario no autentica")
	void tokenInvalido() throws Exception {
		request.addHeader("Authorization", "Bearer tok123");
		when(jwtUtil.extraerNombreUsuario("tok123")).thenReturn("ana.garcia");
		when(userDetailsService.loadUserByUsername("ana.garcia")).thenReturn(usuario);
		when(jwtUtil.validarToken("tok123", usuario)).thenReturn(false);

		filter.doFilter(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Token sin nombre de usuario no consulta el UserDetailsService")
	void tokenSinNombreUsuario() throws Exception {
		request.addHeader("Authorization", "Bearer tok123");
		when(jwtUtil.extraerNombreUsuario("tok123")).thenReturn(null);

		filter.doFilter(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verifyNoInteractions(userDetailsService);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Token malformado o expirado deja el contexto limpio y continúa la cadena")
	void tokenMalformado() throws Exception {
		request.addHeader("Authorization", "Bearer basura");
		when(jwtUtil.extraerNombreUsuario("basura")).thenThrow(new RuntimeException("Token malformado"));

		filter.doFilter(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Usuario inexistente en BD deja el contexto limpio y continúa la cadena")
	void usuarioInexistente() throws Exception {
		request.addHeader("Authorization", "Bearer tok123");
		when(jwtUtil.extraerNombreUsuario("tok123")).thenReturn("fantasma");
		when(userDetailsService.loadUserByUsername("fantasma"))
				.thenThrow(new UsernameNotFoundException("Usuario no encontrado"));

		filter.doFilter(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Si ya hay autenticación en el contexto no la reemplaza")
	void yaAutenticado() throws Exception {
		Authentication previa = new UsernamePasswordAuthenticationToken("previo", null, List.of());
		SecurityContextHolder.getContext().setAuthentication(previa);
		request.addHeader("Authorization", "Bearer tok123");
		when(jwtUtil.extraerNombreUsuario("tok123")).thenReturn("ana.garcia");

		filter.doFilter(request, response, filterChain);

		assertSame(previa, SecurityContextHolder.getContext().getAuthentication());
		verifyNoInteractions(userDetailsService);
		verify(filterChain).doFilter(request, response);
	}

}
