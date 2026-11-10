package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando un valor numérico excede el máximo permitido.
 */
public class NumeroDemasiadoGrandeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NumeroDemasiadoGrandeException(String mensaje) {
		super(mensaje);
	}

}
