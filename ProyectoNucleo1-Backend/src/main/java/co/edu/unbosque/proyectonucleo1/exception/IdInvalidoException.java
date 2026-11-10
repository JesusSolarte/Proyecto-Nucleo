package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando un identificador es nulo, negativo o no válido.
 */
public class IdInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IdInvalidoException(String mensaje) {
		super(mensaje);
	}

}
