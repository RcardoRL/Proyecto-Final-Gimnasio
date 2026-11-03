
package com.gimnasio.repository;

import com.gimnasio.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Cuenta las reservas activas de una clase.
    long countByClase_IdAndEstado(Long claseId, String estado);
    


    // Verifica si un cliente ya tiene una reserva activa para esa clase.
    boolean existsByClase_IdAndCliente_IdAndEstado(
        Long claseId, Long clienteId, String estado);

    // Busca todas las reservas realizadas por un cliente.
    List<Reserva> findByCliente_Id(Long clienteId);

    // Busca las reservas activas de un cliente.
    List<Reserva> findByCliente_IdAndEstado(
        Long clienteId,
        String estado
    );
}
