package co.edu.unbosque.proyectonucleo1.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.proyectonucleo1.entity.BloqueDisponibilidad;
import co.edu.unbosque.proyectonucleo1.util.enums.DiaSemana;

/**
 * Repositorio de acceso a datos para {@link BloqueDisponibilidad}.
 */
public interface BloqueDisponibilidadRepository extends CrudRepository<BloqueDisponibilidad, Long> {

	List<BloqueDisponibilidad> findByDocenteId(Long docenteId);

	List<BloqueDisponibilidad> findByDocenteIdAndDiaSemana(Long docenteId, DiaSemana diaSemana);

	List<BloqueDisponibilidad> findByDiaSemana(DiaSemana diaSemana);

}
