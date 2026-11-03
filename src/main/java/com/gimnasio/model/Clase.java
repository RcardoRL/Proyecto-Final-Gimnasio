
package com.gimnasio.model;

// Importaciones necesarias para trabajar con JPA y las validaciones.
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Esta clase representa una clase o actividad que se ofrece en el gimnasio.
 *
 * La anotación @Entity permite guardar sus datos en la base de datos.
 * La anotación @Table indica el nombre de la tabla que se utilizará.
 */
@Entity
@Table(name = "clases")
public class Clase {

    // Identificador único de cada clase.
    // Se genera automáticamente al guardar un nuevo registro.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre de la clase, por ejemplo: Yoga, Spinning o Pilates.
    // No se permite dejar este campo vacío.
    @NotBlank(message = "El nombre de la clase es obligatorio")
    private String nombre;

    // Tipo o categoría de la actividad que se impartirá.
    // Este campo también es obligatorio.
    @NotBlank(message = "El tipo de clase es obligatorio")
    private String tipo;

    // Relación entre la clase y el usuario que será su entrenador.
    // Muchas clases pueden estar asignadas al mismo entrenador.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", nullable = false)
    @NotNull(message = "Debe seleccionar un entrenador")
    private Usuario entrenador;

    // Fecha en la que se impartirá la clase.
    // Es un dato obligatorio.
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    // Hora en la que comienza la clase.
    // Es un dato obligatorio.
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    // Cantidad máxima de personas que pueden inscribirse.
    // Se establece que debe ser como mínimo 1.
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    private int capacidadMaxima;

    // Constructor vacío requerido por JPA para crear objetos de esta entidad.
    public Clase() {
    }

    /**
     * Calcula automáticamente la hora de finalización.
     *
     * Todas las clases tienen una duración fija de una hora.
     * Por ejemplo, si comienza a las 8:00, termina a las 9:00.
     *
     * @Transient indica que este valor no se almacena como columna
     * independiente en la base de datos.
     */
    @Transient
    public LocalTime getHoraFin() {

        // Si todavía no se ha establecido una hora de inicio,
        // no es posible calcular la hora de finalización.
        if (horaInicio == null) {
            return null;
        }

        // Se suma una hora a la hora de inicio.
        return horaInicio.plusHours(1);
    }

    // Obtiene el identificador de la clase.
    public Long getId() {
        return id;
    }

    // Establece el identificador de la clase.
    public void setId(Long id) {
        this.id = id;
    }

    // Obtiene el nombre de la clase.
    public String getNombre() {
        return nombre;
    }

    // Establece el nombre de la clase.
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Obtiene el tipo de actividad.
    public String getTipo() {
        return tipo;
    }

    // Establece el tipo de actividad.
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // Obtiene el entrenador asignado a la clase.
    public Usuario getEntrenador() {
        return entrenador;
    }

    // Asigna un entrenador a la clase.
    public void setEntrenador(Usuario entrenador) {
        this.entrenador = entrenador;
    }

    // Obtiene la fecha de la clase.
    public LocalDate getFecha() {
        return fecha;
    }

    // Establece la fecha de la clase.
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    // Obtiene la hora de inicio.
    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    // Establece la hora de inicio.
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    // Obtiene la capacidad máxima de participantes.
    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    // Establece la capacidad máxima de participantes.
    public void setCapacidadMaxima(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }
}
