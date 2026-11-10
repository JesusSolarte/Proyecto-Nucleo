package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando las credenciales de autenticación son incorrectas.
 */
public class CredencialesInvalidasException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CredencialesInvalidasException(String mensaje) {
		super(mensaje);
	}

}
