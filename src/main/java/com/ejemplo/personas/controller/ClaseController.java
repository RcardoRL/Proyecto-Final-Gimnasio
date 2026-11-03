
package com.ejemplo.personas.controller;

import com.ejemplo.personas.model.Clase;
import com.ejemplo.personas.model.Usuario;
import com.ejemplo.personas.repository.UsuarioRepository;
import com.ejemplo.personas.repository.ReservaRepository;
import com.ejemplo.personas.service.ClaseService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/clases")
public class ClaseController {

    private final ClaseService claseService;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;

    public ClaseController(
            ClaseService claseService,
            UsuarioRepository usuarioRepository,
            ReservaRepository reservaRepository) {

        this.claseService = claseService;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
    }

    /**
     * Muestra todas las clases registradas para su gestión.
     */
    @GetMapping
    public String listar(Model model) {

        model.addAttribute("clases", claseService.listar());

        return "clases/lista";
    }

    /**
     * Muestra únicamente las clases asignadas al entrenador
     * que inició sesión.
     */
    @GetMapping("/mis-clases")
    public String misClases(Model model, Principal principal) {

        Usuario entrenador = usuarioRepository
                .findByUsername(principal.getName())
                .orElse(null);

        // Verificamos que el usuario sea un entrenador.
        if (entrenador == null ||
                !"ENTRENADOR".equals(entrenador.getRol())) {
            return "redirect:/usuarios/mi-perfil";
        }

        // Filtramos las clases asignadas a este entrenador.
        List<Clase> clasesAsignadas = claseService.listar()
                .stream()
                .filter(clase -> clase.getEntrenador() != null
                        && clase.getEntrenador().getId()
                        .equals(entrenador.getId()))
                .toList();

        // Calculamos los cupos ocupados y disponibles.
        Map<Long, Long> ocupados = new HashMap<>();
        Map<Long, Long> disponibles = new HashMap<>();

        for (Clase clase : clasesAsignadas) {

            long cantidadOcupados = reservaRepository
                    .countByClase_IdAndEstado(
                            clase.getId(), "ACTIVA");

            long cantidadDisponibles =
                    clase.getCapacidadMaxima() - cantidadOcupados;

            ocupados.put(clase.getId(), cantidadOcupados);
            disponibles.put(
                    clase.getId(),
                    Math.max(0, cantidadDisponibles));
        }

        model.addAttribute("entrenador", entrenador);
        model.addAttribute("clases", clasesAsignadas);
        model.addAttribute("ocupados", ocupados);
        model.addAttribute("disponibles", disponibles);

        return "clases/mis-clases";
    }

    /**
     * Abre el formulario para registrar una nueva clase.
     */
    @GetMapping("/nueva")
    public String nueva(Model model) {

        Clase clase = new Clase();
        clase.setEntrenador(new Usuario());

        model.addAttribute("clase", clase);
        cargarEntrenadores(model);

        return "clases/formulario";
    }

    /**
     * Guarda una nueva clase.
     */
    @PostMapping("/guardar")
    public String guardar(
            @Valid @ModelAttribute("clase") Clase clase,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            cargarEntrenadores(model);
            return "clases/formulario";
        }

        try {
            claseService.guardar(clase);

            redirectAttributes.addFlashAttribute(
                    "mensaje", "La clase se registró correctamente");

            return "redirect:/clases";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            cargarEntrenadores(model);

            return "clases/formulario";
        }
    }

    /**
     * Busca una clase y abre el formulario para editarla.
     */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {

        Clase clase = claseService.buscarPorId(id);

        model.addAttribute("clase", clase);
        cargarEntrenadores(model);

        return "clases/formulario";
    }

    /**
     * Actualiza los datos de una clase existente.
     */
    @PostMapping("/actualizar/{id}")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("clase") Clase clase,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            cargarEntrenadores(model);
            return "clases/formulario";
        }

        try {
            claseService.actualizar(id, clase);

            redirectAttributes.addFlashAttribute(
                    "mensaje", "La clase se actualizó correctamente");

            return "redirect:/clases";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            cargarEntrenadores(model);

            return "clases/formulario";
        }
    }

    /**
     * Elimina una clase seleccionada.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            claseService.eliminar(id);

            redirectAttributes.addFlashAttribute(
                    "mensaje", "La clase se eliminó correctamente");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/clases";
    }

    /**
     * Carga los usuarios que tienen el rol ENTRENADOR.
     */
    private void cargarEntrenadores(Model model) {

        List<Usuario> entrenadores = usuarioRepository.findAll()
                .stream()
                .filter(usuario ->
                        "ENTRENADOR".equals(usuario.getRol()))
                .toList();

        model.addAttribute("entrenadores", entrenadores);
    }
}
