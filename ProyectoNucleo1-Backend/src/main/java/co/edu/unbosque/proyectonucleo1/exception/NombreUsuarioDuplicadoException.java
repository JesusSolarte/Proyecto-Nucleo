package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando ya existe un usuario con el nombre de usuario indicado.
 */
public class NombreUsuarioDuplicadoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NombreUsuarioDuplicadoException(String mensaje) {
		super(mensaje);
	}

}
