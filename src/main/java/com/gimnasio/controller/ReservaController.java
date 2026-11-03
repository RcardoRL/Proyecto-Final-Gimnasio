
package com.gimnasio.controller;

import com.gimnasio.model.Clase;
import com.gimnasio.model.Usuario;
import com.gimnasio.repository.ClaseRepository;
import com.gimnasio.repository.ReservaRepository;
import com.gimnasio.repository.UsuarioRepository;
import com.gimnasio.service.ReservaService;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

        private final ReservaService reservaService;
        private final ReservaRepository reservaRepository;
        private final ClaseRepository claseRepository;
        private final UsuarioRepository usuarioRepository;

        public ReservaController(
                        ReservaService reservaService,
                        ReservaRepository reservaRepository,
                        ClaseRepository claseRepository,
                        UsuarioRepository usuarioRepository) {

                this.reservaService = reservaService;
                this.reservaRepository = reservaRepository;
                this.claseRepository = claseRepository;
                this.usuarioRepository = usuarioRepository;
        }

        // Muestra las clases disponibles y sus cupos.
        @GetMapping("/clases")
        public String verClases(Model model, Principal principal) {

                Usuario usuario = usuarioRepository
                                .findByUsername(principal.getName())
                                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

                List<Clase> clases = claseRepository.findAllByOrderByFechaAscHoraInicioAsc();

                Map<Long, Long> ocupadosPorClase = new HashMap<>();
                Map<Long, Long> disponiblesPorClase = new HashMap<>();

                for (Clase clase : clases) {

                        long ocupados = reservaRepository
                                        .countByClase_IdAndEstado(clase.getId(), "ACTIVA");

                        long disponibles = Math.max(
                                        0,
                                        clase.getCapacidadMaxima() - ocupados);

                        ocupadosPorClase.put(clase.getId(), ocupados);
                        disponiblesPorClase.put(clase.getId(), disponibles);
                }

                model.addAttribute("usuario", usuario);
                model.addAttribute("clases", clases);
                model.addAttribute("ocupadosPorClase", ocupadosPorClase);
                model.addAttribute("disponiblesPorClase", disponiblesPorClase);

                return "reservas/clases-disponibles";
        }

        // Registra una reserva para el cliente que inició sesión.
        @PreAuthorize("hasRole('CLIENTE')")
        @PostMapping("/crear/{claseId}")
        public String reservar(
                        @PathVariable Long claseId,
                        Principal principal,
                        RedirectAttributes redirectAttributes) {

                try {
                        Usuario cliente = usuarioRepository
                                        .findByUsername(principal.getName())
                                        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

                        reservaService.reservar(cliente.getId(), claseId);

                        redirectAttributes.addFlashAttribute(
                                        "mensaje", "La reserva se realizó correctamente");

                } catch (IllegalArgumentException e) {

                        redirectAttributes.addFlashAttribute(
                                        "error", e.getMessage());
                }

                return "redirect:/reservas/clases";
        }

        // Muestra las reservas del cliente que inició sesión.
        @PreAuthorize("hasRole('CLIENTE')")
        @GetMapping("/mis-reservas")
        public String misReservas(
                        Model model,
                        Principal principal) {

                Usuario cliente = usuarioRepository
                                .findByUsername(principal.getName())
                                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

                model.addAttribute(
                                "reservas",
                                reservaService.listarPorCliente(cliente.getId()));

                return "reservas/mis-reservas";
        }

        // Permite cancelar una reserva propia.
        @PreAuthorize("hasRole('CLIENTE')")
        @PostMapping("/cancelar/{reservaId}")
        public String cancelar(
                        @PathVariable Long reservaId,
                        Principal principal,
                        RedirectAttributes redirectAttributes) {

                try {
                        Usuario cliente = usuarioRepository
                                        .findByUsername(principal.getName())
                                        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

                        reservaService.cancelar(reservaId, cliente.getId());

                        redirectAttributes.addFlashAttribute(
                                        "mensaje", "La reserva fue cancelada correctamente");

                } catch (IllegalArgumentException e) {

                        redirectAttributes.addFlashAttribute(
                                        "error", e.getMessage());
                }

                return "redirect:/reservas/mis-reservas";
        }
}
