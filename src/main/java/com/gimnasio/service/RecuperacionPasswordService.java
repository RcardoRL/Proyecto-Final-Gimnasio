
package com.gimnasio.service;

import com.gimnasio.model.Usuario;
import com.gimnasio.model.TokenRecuperacion;
import com.gimnasio.repository.UsuarioRepository;
import com.gimnasio.repository.TokenRecuperacionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
public class RecuperacionPasswordService {

    private final UsuarioRepository usuarioRepo;
    private final TokenRecuperacionRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public RecuperacionPasswordService(
            UsuarioRepository usuarioRepo,
            TokenRecuperacionRepository tokenRepo,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepo = usuarioRepo;
        this.tokenRepo = tokenRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // Generar token de recuperación
    @Transactional
    public String generarToken(String email) {

        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElse(null);

        if (usuario == null || !usuario.isActivo()) {
            return null;
        }

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        TokenRecuperacion recuperacion =
                new TokenRecuperacion();

        recuperacion.setToken(calcularHash(token));
        recuperacion.setUsuario(usuario);
        recuperacion.setFechaExpiracion(
                LocalDateTime.now().plusMinutes(15)
        );
        recuperacion.setUtilizado(false);

        tokenRepo.save(recuperacion);

        return token;
    }

    // Calcular huella SHA-256 del token
    private String calcularHash(String token) {
        try {
            byte[] hash = MessageDigest
                    .getInstance("SHA-256")
                    .digest(token.getBytes(
                        java.nio.charset.StandardCharsets.UTF_8
                    ));

            return HexFormat.of().formatHex(hash);

        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "No se pudo generar el hash del token", e
            );
        }
    }

    // Verificar si el token sigue siendo válido
    @Transactional(readOnly = true)
    public boolean tokenValido(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        return tokenRepo.findByToken(calcularHash(token))
                .filter(t -> !t.isUtilizado())
                .filter(t -> t.getFechaExpiracion()
                        .isAfter(LocalDateTime.now()))
                .filter(t -> t.getUsuario().isActivo())
                .isPresent();
    }

    // Restablecer la contraseña
    @Transactional
    public boolean restablecerPassword(
            String token,
            String nuevaPassword) {

        if (token == null || token.isBlank()
                || nuevaPassword == null
                || nuevaPassword.length() < 8) {
            return false;
        }

        TokenRecuperacion recuperacion = tokenRepo
                .findByToken(calcularHash(token))
                .orElse(null);

        if (recuperacion == null
                || recuperacion.isUtilizado()
                || !recuperacion.getFechaExpiracion()
                        .isAfter(LocalDateTime.now())
                || !recuperacion.getUsuario().isActivo()) {
            return false;
        }

        Usuario usuario = recuperacion.getUsuario();

        // Guardar contraseña cifrada con BCrypt
        usuario.setPassword(
                passwordEncoder.encode(nuevaPassword)
        );

        usuarioRepo.save(usuario);

        // Invalidar todos los tokens del usuario
        List<TokenRecuperacion> tokens = tokenRepo.findAll();

        for (TokenRecuperacion t : tokens) {
            if (t.getUsuario().getId().equals(usuario.getId())) {
                t.setUtilizado(true);
                tokenRepo.save(t);
            }
        }

        return true;
    }
}
