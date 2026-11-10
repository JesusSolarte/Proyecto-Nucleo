package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando un usuario intenta una operación para la que no está
 * autorizado.
 */
public class UsuarioSinPermisoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UsuarioSinPermisoException(String mensaje) {
		super(mensaje);
	}

}
