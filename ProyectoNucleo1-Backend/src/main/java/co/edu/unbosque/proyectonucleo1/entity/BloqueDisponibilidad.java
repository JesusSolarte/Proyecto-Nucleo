package co.edu.unbosque.proyectonucleo1.entity;

import java.time.LocalTime;
import java.util.Objects;

import co.edu.unbosque.proyectonucleo1.util.enums.DiaSemana;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Bloque de disponibilidad semanal de un docente. Representa un intervalo de
 * tiempo (horaInicio - horaFin) dentro de un día de la semana.
 */
@Entity
@Table(name = "bloque_disponibilidad")
public class BloqueDisponibilidad {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "docente_id", nullable = false)
	private Usuario docente;

	@Enumerated(EnumType.STRING)
	@Column(name = "dia_semana", nullable = false, length = 12)
	private DiaSemana diaSemana;

	@Column(name = "hora_inicio", nullable = false)
	private LocalTime horaInicio;

	@Column(name = "hora_fin", nullable = false)
	private LocalTime horaFin;

	public BloqueDisponibilidad() {
	}

	public BloqueDisponibilidad(Usuario docente, DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFin) {
		this.docente = docente;
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

	public Usuario getDocente() {
		return docente;
	}

	public void setDocente(Usuario docente) {
		this.docente = docente;
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
		BloqueDisponibilidad other = (BloqueDisponibilidad) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "BloqueDisponibilidad [id=" + id + ", diaSemana=" + diaSemana + ", horaInicio=" + horaInicio
				+ ", horaFin=" + horaFin + "]";
	}

}
