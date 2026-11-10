package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando un valor no corresponde a una opción permitida.
 */
public class OpcionNoValidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OpcionNoValidaException(String mensaje) {
		super(mensaje);
	}

}
