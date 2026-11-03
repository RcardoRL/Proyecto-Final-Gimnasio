
package com.gimnasio.service;

import com.gimnasio.model.Clase;
import com.gimnasio.model.Reserva;
import com.gimnasio.model.Usuario;
import com.gimnasio.repository.ClaseRepository;
import com.gimnasio.repository.ReservaRepository;
import com.gimnasio.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClaseRepository claseRepository;
    private final UsuarioRepository usuarioRepository;

    public ReservaService(
            ReservaRepository reservaRepository,
            ClaseRepository claseRepository,
            UsuarioRepository usuarioRepository) {

        this.reservaRepository = reservaRepository;
        this.claseRepository = claseRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Registra una reserva para un cliente.
    @Transactional
    public Reserva reservar(Long clienteId, Long claseId) {

        // Buscar al cliente.
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() ->
                        new IllegalArgumentException("El cliente no existe"));

        // Verificar que el usuario tenga el rol de CLIENTE.
        if (!"CLIENTE".equals(cliente.getRol())) {
            throw new IllegalArgumentException(
                    "Solamente los clientes pueden reservar clases");
        }

        // Buscar la clase seleccionada.
        Clase clase = claseRepository.findById(claseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("La clase no existe"));

        // Verificar que la clase todavía no haya comenzado.
        LocalDateTime inicioClase = LocalDateTime.of(
                clase.getFecha(), clase.getHoraInicio());

        if (!inicioClase.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "No se puede reservar una clase que ya comenzó");
        }

        // Evitar que un cliente reserve dos veces la misma clase.
        boolean yaReservo = reservaRepository
                .existsByClase_IdAndCliente_IdAndEstado(
                        claseId, clienteId, "ACTIVA");

        if (yaReservo) {
            throw new IllegalArgumentException(
                    "Ya tienes una reserva activa para esta clase");
        }


        // Verificar si el cliente tiene otra reserva
        // activa que coincida con el horario de la clase.
        List<Reserva> reservasActivas = reservaRepository
                .findByCliente_IdAndEstado(clienteId, "ACTIVA");

        for (Reserva reservaExistente : reservasActivas) {

            Clase otraClase = reservaExistente.getClase();

            // Solo comparar clases de la misma fecha.
            if (!otraClase.getFecha().equals(clase.getFecha())) {
                continue;
            }

            // Detectar horarios superpuestos.
            boolean hayConflicto =
                    clase.getHoraInicio().isBefore(otraClase.getHoraFin())
                    && clase.getHoraFin().isAfter(otraClase.getHoraInicio());

            if (hayConflicto) {
                throw new IllegalArgumentException(
                        "Ya tienes otra clase reservada en ese horario");
            }
        }


        // Contar las reservas que están activas.
        long ocupados = reservaRepository
                .countByClase_IdAndEstado(claseId, "ACTIVA");

        // Verificar que todavía existan espacios disponibles.
        if (ocupados >= clase.getCapacidadMaxima()) {
            throw new IllegalArgumentException(
                    "La clase ya no tiene cupos disponibles");
        }

        // Crear y guardar la reserva.
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setClase(clase);
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setEstado("ACTIVA");

        return reservaRepository.save(reserva);
    }


    // Permite al cliente cancelar su propia reserva.
    @Transactional
    public Reserva cancelar(Long reservaId, Long clienteId) {

        // Buscar la reserva.
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() ->
                        new IllegalArgumentException("La reserva no existe"));

        // Verificar que la reserva pertenezca al cliente.
        if (!reserva.getCliente().getId().equals(clienteId)) {
            throw new IllegalArgumentException(
                    "No puedes cancelar una reserva de otro cliente");
        }

        // Verificar que la reserva esté activa.
        if (!"ACTIVA".equals(reserva.getEstado())) {
            throw new IllegalArgumentException(
                    "Esta reserva ya fue cancelada");
        }

        // Obtener la fecha y hora de inicio de la clase.
        Clase clase = reserva.getClase();

        LocalDateTime inicioClase = LocalDateTime.of(
                clase.getFecha(), clase.getHoraInicio());

       // Permitir cancelar hasta 15 minutos después del inicio
        LocalDateTime limiteCancelacion = inicioClase.plusMinutes(15);

        if (LocalDateTime.now().isAfter(limiteCancelacion)) {
                throw new IllegalArgumentException(
                "Solo puedes cancelar hasta 15 minutos después de iniciada la clase");
}

        // Cambiar el estado para liberar el cupo.
        reserva.setEstado("CANCELADA");

        return reservaRepository.save(reserva);
    }


    // Consulta todas las reservas realizadas por un cliente.
        public List<Reserva> listarPorCliente(Long clienteId) {
        return reservaRepository.findByCliente_Id(clienteId);
}


}
