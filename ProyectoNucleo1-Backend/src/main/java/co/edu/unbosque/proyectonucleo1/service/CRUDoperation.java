package co.edu.unbosque.proyectonucleo1.service;

import java.util.List;

/**
 * Contrato genérico de operaciones CRUD utilizado por los servicios.
 *
 * @param <T> tipo de dato (DTO) con el que opera el servicio
 */
public interface CRUDoperation<T> {

	T crear(T dato);

	List<T> listar();

	T obtenerPorId(Long id);

	T actualizar(Long id, T dato);

	void eliminar(Long id);

}
