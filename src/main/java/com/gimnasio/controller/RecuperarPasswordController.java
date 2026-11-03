
package com.gimnasio.controller;

import com.gimnasio.service.RecuperacionPasswordService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RecuperarPasswordController {

    private final RecuperacionPasswordService recuperacionService;

    public RecuperarPasswordController(
            RecuperacionPasswordService recuperacionService) {
        this.recuperacionService = recuperacionService;
    }

    // Mostrar formulario para solicitar recuperación
    @GetMapping("/recuperar-password")
    public String mostrarFormulario() {
        return "recuperar-password";
    }

    // Procesar solicitud de recuperación
    @PostMapping("/recuperar-password")
    public String solicitarRecuperacion(
            @RequestParam String email,
            Model model) {

        // Generar token temporal
        // Pendiente: enviar el enlace por Microsoft Graph
        String token = recuperacionService.generarToken(email);

                // SOLO PARA PRUEBAS LOCALES.
        // Eliminar antes de publicar la aplicación.
        if (token != null) {
            System.out.println(
                "ENLACE DE RECUPERACIÓN (PRUEBA LOCAL): "
                + "http://localhost:8080/restablecer-password?token="
                + token
            );
        }

        model.addAttribute(
            "mensaje",
            "Si el correo está registrado y la cuenta está activa, " +
            "podrás recibir instrucciones para recuperar tu contraseña."
        );

        return "recuperar-password";
    }

    // Mostrar formulario para establecer nueva contraseña
    @GetMapping("/restablecer-password")
    public String mostrarRestablecimiento(
            @RequestParam(required = false) String token,
            Model model) {

        if (!recuperacionService.tokenValido(token)) {
            model.addAttribute(
                "error",
                "El enlace de recuperación no es válido o ha expirado."
            );
            model.addAttribute("tokenInvalido", true);
            return "restablecer-password";
        }

        model.addAttribute("token", token);
        return "restablecer-password";
    }

    // Guardar nueva contraseña
    @PostMapping("/restablecer-password")
    public String restablecerPassword(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmarPassword,
            Model model) {

        // Verificar primero que el token siga siendo válido
        if (!recuperacionService.tokenValido(token)) {
            model.addAttribute(
                "error",
                "El enlace es inválido, ha expirado o ya fue utilizado."
            );
            model.addAttribute("tokenInvalido", true);
            return "restablecer-password";
        }

        model.addAttribute("token", token);

        if (!password.equals(confirmarPassword)) {
            model.addAttribute(
                "error",
                "Las contraseñas no coinciden."
            );
            return "restablecer-password";
        }

        if (password.length() < 8) {
            model.addAttribute(
                "error",
                "La contraseña debe tener al menos 8 caracteres."
            );
            return "restablecer-password";
        }

        boolean actualizado = recuperacionService
                .restablecerPassword(token, password);

        if (!actualizado) {
            model.addAttribute(
                "error",
                "No se pudo restablecer la contraseña. Solicita otro enlace."
            );
            model.addAttribute("tokenInvalido", true);
            return "restablecer-password";
        }

        return "redirect:/login?passwordRestablecida";
    }
}
