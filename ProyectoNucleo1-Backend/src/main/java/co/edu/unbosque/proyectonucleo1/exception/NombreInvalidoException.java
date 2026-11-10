package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando un nombre contiene caracteres no permitidos.
 */
public class NombreInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NombreInvalidoException(String mensaje) {
		super(mensaje);
	}

}
