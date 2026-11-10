package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando ya existe un usuario con el correo indicado.
 */
public class CorreoDuplicadoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CorreoDuplicadoException(String mensaje) {
		super(mensaje);
	}

}
