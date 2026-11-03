
package com.gimnasio.repository;

import com.gimnasio.model.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRecuperacionRepository
        extends JpaRepository<TokenRecuperacion, Long> {

    // Buscar un token de recuperación
    Optional<TokenRecuperacion> findByToken(String token);
}

