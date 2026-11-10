package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando un correo electrónico no tiene un formato válido.
 */
public class FormatoCorreoInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FormatoCorreoInvalidoException(String mensaje) {
		super(mensaje);
	}

}
