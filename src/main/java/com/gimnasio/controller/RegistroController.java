
package com.gimnasio.controller;

import com.gimnasio.model.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gimnasio.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class RegistroController {

    private final UsuarioService usuarioService;

    public RegistroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    // Mostrar el formulario de registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {

        // Crear un objeto vacío para el formulario
        model.addAttribute("usuario", new Usuario());

        // Mostrar la vista de registro
        return "registro";
    }


    // Procesar el registro de un nuevo cliente
    @PostMapping("/registro")
    public String registrar(
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult result,
            Model model) {

        // Verificar errores de los campos
        if (result.hasErrors()) {
            return "registro";
        }

        try {
            // Registrar al usuario como CLIENTE
            usuarioService.registrarCliente(usuario);

            // Enviar al inicio de sesión
            return "redirect:/login?registroExitoso";

        } catch (IllegalArgumentException e) {

            // Mostrar errores como correo o cédula repetidos
            model.addAttribute("error", e.getMessage());

            return "registro";
        }
    }


}

