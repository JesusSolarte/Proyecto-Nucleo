package co.edu.unbosque.proyectonucleo1.exception;

/**
 * Se lanza cuando un bloque de disponibilidad se cruza con otro bloque ya
 * registrado para el mismo docente y día.
 */
public class CruceHorarioException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CruceHorarioException(String mensaje) {
		super(mensaje);
	}

}
