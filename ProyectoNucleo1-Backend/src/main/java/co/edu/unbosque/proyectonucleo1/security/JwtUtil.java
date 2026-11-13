package co.edu.unbosque.proyectonucleo1.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Utilidad para la generación y validación de tokens JWT.
 */
@Component
public class JwtUtil {

	private static final long TIEMPO_EXPIRACION = 1000L * 60 * 60 * 10;
	private static final String SECRETO = "claveSecretaJwtProyectoNucleo1DisponibilidadDocente2026UnbosqueSegura";

	private Key obtenerClave() {
		return Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8));
	}

	public String generarToken(String nombreUsuario, String rol) {
		Map<String, Object> reclamos = new HashMap<>();
		reclamos.put("rol", rol);
		return Jwts.builder().setClaims(reclamos).setSubject(nombreUsuario).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + TIEMPO_EXPIRACION))
				.signWith(obtenerClave(), SignatureAlgorithm.HS256).compact();
	}

	public String extraerNombreUsuario(String token) {
		return extraerReclamo(token, Claims::getSubject);
	}

	public String extraerRol(String token) {
		return extraerReclamo(token, claims -> claims.get("rol", String.class));
	}

	public Date extraerExpiracion(String token) {
		return extraerReclamo(token, Claims::getExpiration);
	}

	public <T> T extraerReclamo(String token, Function<Claims, T> resolutor) {
		return resolutor.apply(extraerTodosLosReclamos(token));
	}

	private Claims extraerTodosLosReclamos(String token) {
		return Jwts.parserBuilder().setSigningKey(obtenerClave()).build().parseClaimsJws(token).getBody();
	}

	private boolean estaExpirado(String token) {
		return extraerExpiracion(token).before(new Date());
	}

	public boolean validarToken(String token, UserDetails usuario) {
		final String nombreUsuario = extraerNombreUsuario(token);
		return nombreUsuario.equals(usuario.getUsername()) && !estaExpirado(token);
	}

}
