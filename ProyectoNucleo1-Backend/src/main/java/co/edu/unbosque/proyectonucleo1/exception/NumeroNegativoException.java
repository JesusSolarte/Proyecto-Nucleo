package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando un valor numérico es negativo y no debería serlo.
 */
public class NumeroNegativoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NumeroNegativoException(String mensaje) {
		super(mensaje);
	}

}
