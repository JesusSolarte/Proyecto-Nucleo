package co.edu.unbosque.proyectonucleo1.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.proyectonucleo1.entity.Usuario;

/**
 * Repositorio de acceso a datos para {@link Usuario}.
 */
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

	Optional<Usuario> findByNombreUsuario(String nombreUsuario);

	Optional<Usuario> findByCorreo(String correo);

	boolean existsByNombreUsuario(String nombreUsuario);

	boolean existsByCorreo(String correo);

}
