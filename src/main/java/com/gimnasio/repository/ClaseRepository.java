

package com.gimnasio.repository;

// Importamos la entidad Clase.
import com.gimnasio.model.Clase;

// Importaciones de Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio encargado de consultar y administrar
 * las clases del gimnasio.
 *
 * JpaRepository proporciona operaciones para guardar,
 * buscar, actualizar y eliminar registros.
 */
@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {

    /**
     * Busca las clases de un entrenador en una fecha específica.
     */
    List<Clase> findByEntrenador_IdAndFecha(
            Long entrenadorId,
            LocalDate fecha
    );

    /**
     * Busca todas las clases asignadas a un entrenador,
     * independientemente de la fecha.
     */
    List<Clase> findByEntrenador_Id(Long entrenadorId);

    /**
     * Recupera todas las clases ordenadas por fecha
     * y hora de inicio.
     */
    List<Clase> findAllByOrderByFechaAscHoraInicioAsc();
}
