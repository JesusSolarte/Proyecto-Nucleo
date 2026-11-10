package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando una contraseña no cumple las políticas de seguridad.
 */
public class ContrasenaInvalidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ContrasenaInvalidaException(String mensaje) {
		super(mensaje);
	}

}
