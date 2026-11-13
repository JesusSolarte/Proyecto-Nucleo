package co.edu.unbosque.proyectonucleo1.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectonucleo1.dto.BloqueDisponibilidadDTO;
import co.edu.unbosque.proyectonucleo1.entity.BloqueDisponibilidad;
import co.edu.unbosque.proyectonucleo1.entity.Usuario;
import co.edu.unbosque.proyectonucleo1.exception.CruceHorarioException;
import co.edu.unbosque.proyectonucleo1.exception.IdInvalidoException;
import co.edu.unbosque.proyectonucleo1.exception.RangoHorarioInvalidoException;
import co.edu.unbosque.proyectonucleo1.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectonucleo1.exception.TextoVacioException;
import co.edu.unbosque.proyectonucleo1.repository.BloqueDisponibilidadRepository;
import co.edu.unbosque.proyectonucleo1.util.enums.DiaSemana;

/**
 * Servicio de gestión de los bloques de disponibilidad semanal de los
 * docentes. Valida los rangos horarios y evita cruces de horarios para un mismo
 * docente.
 */
@Service
public class BloqueDisponibilidadService implements CRUDoperation<BloqueDisponibilidadDTO> {

	private final BloqueDisponibilidadRepository bloqueRepository;
	private final UsuarioService usuarioService;

	public BloqueDisponibilidadService(BloqueDisponibilidadRepository bloqueRepository, UsuarioService usuarioService) {
		this.bloqueRepository = bloqueRepository;
		this.usuarioService = usuarioService;
	}

	@Override
	@Transactional
	public BloqueDisponibilidadDTO crear(BloqueDisponibilidadDTO dato) {
		if (dato == null) {
			throw new TextoVacioException("Los datos del bloque no pueden ser nulos.");
		}
		Usuario docente = usuarioService.obtenerEntidad(dato.getIdDocente());
		validarRango(dato);
		verificarCruce(docente.getId(), dato.getDiaSemana(), dato.getHoraInicio(), dato.getHoraFin(), null);

		BloqueDisponibilidad bloque = new BloqueDisponibilidad(docente, dato.getDiaSemana(), dato.getHoraInicio(),
				dato.getHoraFin());
		bloqueRepository.save(bloque);
		return convertirADTO(bloque);
	}

	@Override
	public List<BloqueDisponibilidadDTO> listar() {
		List<BloqueDisponibilidadDTO> resultado = new ArrayList<>();
		bloqueRepository.findAll().forEach(bloque -> resultado.add(convertirADTO(bloque)));
		return resultado;
	}

	@Override
	public BloqueDisponibilidadDTO obtenerPorId(Long id) {
		return convertirADTO(obtenerEntidad(id));
	}

	@Override
	@Transactional
	public BloqueDisponibilidadDTO actualizar(Long id, BloqueDisponibilidadDTO dato) {
		BloqueDisponibilidad bloque = obtenerEntidad(id);
		if (dato == null) {
			throw new TextoVacioException("Los datos del bloque no pueden ser nulos.");
		}
		if (dato.getDiaSemana() != null) {
			bloque.setDiaSemana(dato.getDiaSemana());
		}
		if (dato.getHoraInicio() != null) {
			bloque.setHoraInicio(dato.getHoraInicio());
		}
		if (dato.getHoraFin() != null) {
			bloque.setHoraFin(dato.getHoraFin());
		}

		validarRango(convertirADTO(bloque));
		verificarCruce(bloque.getDocente().getId(), bloque.getDiaSemana(), bloque.getHoraInicio(), bloque.getHoraFin(),
				bloque.getId());

		bloqueRepository.save(bloque);
		return convertirADTO(bloque);
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		BloqueDisponibilidad bloque = obtenerEntidad(id);
		bloqueRepository.delete(bloque);
	}

	/**
	 * Lista los bloques de disponibilidad de un docente, ordenados por día y hora.
	 */
	public List<BloqueDisponibilidadDTO> listarPorDocente(Long docenteId) {
		usuarioService.obtenerEntidad(docenteId);
		List<BloqueDisponibilidad> bloques = new ArrayList<>(bloqueRepository.findByDocenteId(docenteId));
		bloques.sort((a, b) -> {
			int porDia = a.getDiaSemana().compareTo(b.getDiaSemana());
			if (porDia != 0) {
				return porDia;
			}
			return a.getHoraInicio().compareTo(b.getHoraInicio());
		});
		List<BloqueDisponibilidadDTO> resultado = new ArrayList<>();
		bloques.forEach(bloque -> resultado.add(convertirADTO(bloque)));
		return resultado;
	}

	public BloqueDisponibilidad obtenerEntidad(Long id) {
		if (id == null || id <= 0) {
			throw new IdInvalidoException("El identificador debe ser un número positivo.");
		}
		return bloqueRepository.findById(id).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe un bloque con el id " + id + "."));
	}

	public BloqueDisponibilidadDTO convertirADTO(BloqueDisponibilidad bloque) {
		BloqueDisponibilidadDTO dto = new BloqueDisponibilidadDTO();
		dto.setId(bloque.getId());
		Usuario docente = bloque.getDocente();
		if (docente != null) {
			dto.setIdDocente(docente.getId());
			dto.setNombreDocente(docente.getNombre() + " " + docente.getApellido());
		}
		dto.setDiaSemana(bloque.getDiaSemana());
		dto.setHoraInicio(bloque.getHoraInicio());
		dto.setHoraFin(bloque.getHoraFin());
		return dto;
	}

	private void validarRango(BloqueDisponibilidadDTO dato) {
		if (dato.getDiaSemana() == null) {
			throw new TextoVacioException("El día de la semana es obligatorio.");
		}
		if (dato.getHoraInicio() == null || dato.getHoraFin() == null) {
			throw new TextoVacioException("La hora de inicio y la hora de fin son obligatorias.");
		}
		if (!dato.getHoraInicio().isBefore(dato.getHoraFin())) {
			throw new RangoHorarioInvalidoException("La hora de inicio debe ser anterior a la hora de fin.");
		}
	}

	private void verificarCruce(Long docenteId, DiaSemana dia, LocalTime inicio, LocalTime fin, Long idExcluir) {
		List<BloqueDisponibilidad> existentes = bloqueRepository.findByDocenteIdAndDiaSemana(docenteId, dia);
		for (BloqueDisponibilidad existente : existentes) {
			if (idExcluir != null && existente.getId().equals(idExcluir)) {
				continue;
			}
			if (inicio.isBefore(existente.getHoraFin()) && fin.isAfter(existente.getHoraInicio())) {
				throw new CruceHorarioException("El bloque se cruza con otro ya registrado: "
						+ dia.getNombre().toLowerCase() + " " + formatearHora(existente.getHoraInicio()) + "–"
						+ formatearHora(existente.getHoraFin()) + ".");
			}
		}
	}

	private String formatearHora(LocalTime hora) {
		String minutos = String.valueOf(hora.getMinute());
		if (hora.getMinute() < 10) {
			minutos = "0" + minutos;
		}
		return hora.getHour() + ":" + minutos;
	}

}
