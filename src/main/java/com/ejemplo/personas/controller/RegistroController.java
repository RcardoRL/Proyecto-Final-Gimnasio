
package com.ejemplo.personas.controller;

import com.ejemplo.personas.model.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RegistroController {

    // Mostrar el formulario de registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {

        // Crear un objeto vacío para el formulario
        model.addAttribute("usuario", new Usuario());

        // Mostrar la vista de registro
        return "registro";
    }
}

