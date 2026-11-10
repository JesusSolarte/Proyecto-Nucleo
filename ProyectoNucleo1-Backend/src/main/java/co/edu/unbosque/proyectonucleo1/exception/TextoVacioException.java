package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando un campo de texto obligatorio está vacío.
 */
public class TextoVacioException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TextoVacioException(String mensaje) {
		super(mensaje);
	}

}
