package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando un dato recibido no cumple las validaciones esperadas.
 */
public class DatoInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DatoInvalidoException(String mensaje) {
		super(mensaje);
	}

}
