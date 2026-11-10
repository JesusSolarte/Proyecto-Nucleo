package co.edu.unbosque.proyectonucleo1.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que valida el token JWT enviado en el encabezado Authorization y
 * establece la autenticación en el contexto de seguridad.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserDetailsServiceImpl userDetailsService;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		final String encabezado = request.getHeader("Authorization");

		if (encabezado != null && encabezado.startsWith("Bearer ")) {
			final String token = encabezado.substring(7);
			try {
				final String nombreUsuario = jwtUtil.extraerNombreUsuario(token);
				if (nombreUsuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					UserDetails usuario = userDetailsService.loadUserByUsername(nombreUsuario);
					if (jwtUtil.validarToken(token, usuario)) {
						UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
								usuario, null, usuario.getAuthorities());
						autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(autenticacion);
					}
				}
			} catch (Exception ex) {
				SecurityContextHolder.clearContext();
			}
		}

		filterChain.doFilter(request, response);
	}

}
