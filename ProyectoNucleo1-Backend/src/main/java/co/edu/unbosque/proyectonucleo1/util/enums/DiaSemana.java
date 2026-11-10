package co.edu.unbosque.proyectonucleo1.util.enums;

/**
 * Días de la semana en los que un docente puede registrar disponibilidad.
 */
public enum DiaSemana {

	LUNES("Lunes"), MARTES("Martes"), MIERCOLES("Miércoles"), JUEVES("Jueves"), VIERNES("Viernes"),
	SABADO("Sábado"), DOMINGO("Domingo");

	private final String nombre;

	private DiaSemana(String nombre) {
		this.nombre = nombre;
	}

	public String getNombre() {
		return nombre;
	}

}
