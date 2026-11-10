package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando un número de teléfono no tiene un formato válido.
 */
public class TelefonoInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TelefonoInvalidoException(String mensaje) {
		super(mensaje);
	}

}
