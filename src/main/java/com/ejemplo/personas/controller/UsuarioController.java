
package com.ejemplo.personas.controller;

import com.ejemplo.personas.model.Usuario;
import com.ejemplo.personas.service.UsuarioService;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.util.List;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    
@GetMapping("/usuarios")
public String listar(
        @RequestParam(name = "q", required = false) String q,
        Model model) {

        List<Usuario> usuarios = usuarioService.buscar(q);

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("q", q == null ? "" : q);

        return "usuarios/lista";
    }



@GetMapping("/usuarios/nuevo")
public String mostrarFormulario(Model model) {

    Usuario usuario = new Usuario();

    model.addAttribute("usuario", usuario);

    return "usuarios/formulario";
}


@PostMapping("/usuarios/guardar")
public String guardar( //metodo para crear usuarios
        @Valid @ModelAttribute("usuario") Usuario usuario,
        BindingResult result,
        Model model) {

    // Verificar errores de validación
    if (result.hasErrors()) {
        return "usuarios/formulario";
    }

    try {
        // Guardar mediante el servicio
        usuarioService.crear(usuario);

        // Regresar a la lista
        return "redirect:/usuarios";

    } catch (IllegalArgumentException e) {

        // Mostrar errores
        model.addAttribute("error", e.getMessage());

        return "usuarios/formulario";
    }
}


@GetMapping("/usuarios/editar/{id}")
public String mostrarFormularioEditar(
        @PathVariable Long id,
        Model model) {

    Usuario usuario = usuarioService.buscarPorId(id);

    model.addAttribute("usuario", usuario);

    return "usuarios/formulario";
}


@PostMapping("/usuarios/actualizar/{id}")
public String actualizar(
        @PathVariable Long id,
        @Valid @ModelAttribute("usuario") Usuario usuario,
        BindingResult result,
        Model model) {

    // Mantener el ID del usuario que estamos editando
    usuario.setId(id);

    // Verificar los errores de validación
    if (result.hasErrors()) {
        return "usuarios/formulario";
    }

    try {
        // Actualizar mediante la capa de servicio
        usuarioService.actualizar(id, usuario);

        // Regresar a la lista de usuarios
        return "redirect:/usuarios";

    } catch (IllegalArgumentException e) {

        // Mostrar el error en el formulario
        model.addAttribute("error", e.getMessage());

        return "usuarios/formulario";
    }
}


@PostMapping("/usuarios/eliminar/{id}")
public String eliminarUsuario(
        @PathVariable Long id,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    try {
        // Obtener el usuario que inició sesión
        String usernameActual = authentication.getName();

        // Intentar eliminar el usuario
        usuarioService.eliminar(id, usernameActual);

        redirectAttributes.addFlashAttribute(
                "mensaje", "Usuario eliminado correctamente");

    } catch (IllegalArgumentException e) {
        redirectAttributes.addFlashAttribute(
                "error", e.getMessage());
    }

    return "redirect:/usuarios";
}


@GetMapping("/usuarios/{id}")
public String verDetalleUsuario(
        @PathVariable Long id,
        Model model) {

    // Buscar el usuario por su ID
    Usuario usuario = usuarioService.buscarPorId(id);

    // Si no existe, mostrar un mensaje
    if (usuario == null) {
        model.addAttribute(
            "error",
            "El usuario solicitado no existe"
        );
        return "usuarios/no-encontrado";
    }

    // Enviar los datos a la vista de detalles
    model.addAttribute("usuario", usuario);

    return "usuarios/detalle";
}


@GetMapping("/usuarios/mi-perfil")
public String verMiPerfil(
        Authentication authentication,
        Model model) {

    // Obtener el nombre del usuario autenticado
    String username = authentication.getName();

    // Buscar al usuario en la base de datos
    Usuario usuario = usuarioService.buscarPorUsername(username);

    // Enviar sus datos a la pantalla de detalles
    model.addAttribute("usuario", usuario);

    return "usuarios/detalle";
}


}
