
package com.ejemplo.personas.service;

// Importamos las entidades y repositorios necesarios.
import com.ejemplo.personas.model.Clase;
import com.ejemplo.personas.model.Usuario;
import com.ejemplo.personas.repository.ClaseRepository;
import com.ejemplo.personas.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio encargado de administrar las clases del gimnasio.
 *
 * Aquí se implementan las reglas del negocio, como comprobar
 * que un entrenador no tenga dos clases al mismo tiempo.
 */
@Service
public class ClaseService {

    // Repositorio para guardar y consultar las clases.
    private final ClaseRepository claseRepository;

    // Repositorio para consultar los usuarios y entrenadores.
    private final UsuarioRepository usuarioRepository;

    // Constructor para inyectar los repositorios.
    public ClaseService(
            ClaseRepository claseRepository,
            UsuarioRepository usuarioRepository) {
        this.claseRepository = claseRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene todas las clases ordenadas por fecha y hora.
     */
    public List<Clase> listar() {
        return claseRepository.findAllByOrderByFechaAscHoraInicioAsc();
    }

    /**
     * Busca una clase por su identificador.
     */
    public Clase buscarPorId(Long id) {
        return claseRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("La clase no existe"));
    }

    /**
     * Guarda una nueva clase después de validar sus datos,
     * el entrenador y la disponibilidad del horario.
     */
    @Transactional
    public Clase guardar(Clase clase) {

        validarEntrenador(clase.getEntrenador().getId());

        // Comprobamos que el entrenador esté disponible.
        validarHorario(clase, null);

        return claseRepository.save(clase);
    }

    /**
     * Actualiza una clase existente.
     *
     * Se conservan los datos que no se modifican y se validan
     * nuevamente el entrenador y el horario.
     */
    @Transactional
    public Clase actualizar(Long id, Clase datos) {

        Clase existente = buscarPorId(id);

        validarEntrenador(datos.getEntrenador().getId());

        // Actualizamos los datos de la clase.
        existente.setNombre(datos.getNombre());
        existente.setTipo(datos.getTipo());
        existente.setEntrenador(datos.getEntrenador());
        existente.setFecha(datos.getFecha());
        existente.setHoraInicio(datos.getHoraInicio());
        existente.setCapacidadMaxima(datos.getCapacidadMaxima());

        // Se excluye la propia clase de la búsqueda de traslapes.
        validarHorario(existente, id);

        return claseRepository.save(existente);
    }

    /**
     * Comprueba que el usuario seleccionado exista
     * y que tenga el rol de ENTRENADOR.
     */
    private void validarEntrenador(Long entrenadorId) {

        Usuario entrenador = usuarioRepository.findById(entrenadorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("El entrenador no existe"));

        if (!"ENTRENADOR".equals(entrenador.getRol())) {
            throw new IllegalArgumentException(
                    "El usuario seleccionado no tiene el rol de ENTRENADOR");
        }
    }

    /**
     * Verifica que el entrenador no tenga otra clase
     * cuyo horario se superponga con el horario nuevo.
     *
     * Se utiliza LocalDateTime para comparar fecha y hora juntas.
     * Esto permite detectar correctamente los cruces de medianoche.
     * Todas las clases duran una hora.
     */
    private void validarHorario(Clase clase, Long idExcluir) {

        // Buscamos todas las clases del mismo entrenador.
        // Así también podemos revisar clases del día anterior
        // que podrían terminar después de medianoche.
        List<Clase> clasesDelEntrenador =
                claseRepository.findByEntrenador_Id(
                        clase.getEntrenador().getId()
                );

        // Calculamos la fecha y hora de inicio de la nueva clase.
        LocalDateTime nuevaHoraInicio = LocalDateTime.of(
                clase.getFecha(),
                clase.getHoraInicio()
        );

        // La duración de cada clase es de una hora.
        LocalDateTime nuevaHoraFin = nuevaHoraInicio.plusHours(1);

        // Recorremos las clases existentes del entrenador.
        for (Clase otraClase : clasesDelEntrenador) {

            // Al editar, no debemos comparar la clase consigo misma.
            if (idExcluir != null &&
                    idExcluir.equals(otraClase.getId())) {
                continue;
            }

            // Calculamos el inicio y final de la otra clase.
            LocalDateTime otraHoraInicio = LocalDateTime.of(
                    otraClase.getFecha(),
                    otraClase.getHoraInicio()
            );

            LocalDateTime otraHoraFin = otraHoraInicio.plusHours(1);

            // Comprobamos si los dos horarios se superponen.
            boolean existeTraslape =
                    nuevaHoraInicio.isBefore(otraHoraFin) &&
                    nuevaHoraFin.isAfter(otraHoraInicio);

            // Si existe un traslape, rechazamos el registro.
            if (existeTraslape) {
                throw new IllegalArgumentException(
                        "El entrenador ya tiene otra clase en ese horario");
            }
        }
    }

    /**
     * Elimina una clase por su identificador.
     */
    @Transactional
    public void eliminar(Long id) {
        Clase clase = buscarPorId(id);
        claseRepository.delete(clase);
    }
}
