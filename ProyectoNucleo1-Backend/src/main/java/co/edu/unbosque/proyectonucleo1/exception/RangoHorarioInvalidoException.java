package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando el rango horario de un bloque no es válido (por ejemplo, la
 * hora de inicio es posterior o igual a la hora de fin).
 */
public class RangoHorarioInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RangoHorarioInvalidoException(String mensaje) {
		super(mensaje);
	}

}
