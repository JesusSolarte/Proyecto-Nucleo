package co.edu.unbosque.proyectonucleo1.dto;

import java.time.LocalTime;
import java.util.Objects;

import co.edu.unbosque.proyectonucleo1.util.enums.DiaSemana;

/**
 * Objeto de transferencia de datos para
 * {@link co.edu.unbosque.proyectonucleo1.entity.BloqueDisponibilidad}.
 */
public class BloqueDisponibilidadDTO {

	private Long id;
	private Long idDocente;
	private String nombreDocente;
	private DiaSemana diaSemana;
	private LocalTime horaInicio;
	private LocalTime horaFin;

	public BloqueDisponibilidadDTO() {
	}

	public BloqueDisponibilidadDTO(Long id, Long idDocente, String nombreDocente, DiaSemana diaSemana,
			LocalTime horaInicio, LocalTime horaFin) {
		this.id = id;
		this.idDocente = idDocente;
		this.nombreDocente = nombreDocente;
		this.diaSemana = diaSemana;
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIdDocente() {
		return idDocente;
	}

	public void setIdDocente(Long idDocente) {
		this.idDocente = idDocente;
	}

	public String getNombreDocente() {
		return nombreDocente;
	}

	public void setNombreDocente(String nombreDocente) {
		this.nombreDocente = nombreDocente;
	}

	public DiaSemana getDiaSemana() {
		return diaSemana;
	}

	public void setDiaSemana(DiaSemana diaSemana) {
		this.diaSemana = diaSemana;
	}

	public LocalTime getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(LocalTime horaInicio) {
		this.horaInicio = horaInicio;
	}

	public LocalTime getHoraFin() {
		return horaFin;
	}

	public void setHoraFin(LocalTime horaFin) {
		this.horaFin = horaFin;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		BloqueDisponibilidadDTO other = (BloqueDisponibilidadDTO) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "BloqueDisponibilidadDTO [id=" + id + ", idDocente=" + idDocente + ", nombreDocente=" + nombreDocente
				+ ", diaSemana=" + diaSemana + ", horaInicio=" + horaInicio + ", horaFin=" + horaFin + "]";
	}

}
