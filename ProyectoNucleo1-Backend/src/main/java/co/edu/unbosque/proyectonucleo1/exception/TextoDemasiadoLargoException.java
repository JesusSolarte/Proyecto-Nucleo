package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando un campo de texto excede la longitud permitida.
 */
public class TextoDemasiadoLargoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TextoDemasiadoLargoException(String mensaje) {
		super(mensaje);
	}

}
