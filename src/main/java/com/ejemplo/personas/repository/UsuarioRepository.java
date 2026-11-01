package com.ejemplo.personas.repository;

import com.ejemplo.personas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UsuarioRepository extends JpaRepository <Usuario, Long>{

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);


    boolean existsByEmail(String email); //comprueba si un correo ya está registrado

    boolean existsByEmailAndIdNot(String email, Long id); //comprueba si otro usuario tiene ese correo, excluyendo al usuario que estamos editando


    // Verificar si una cédula ya está registrada
    boolean existsByCedula(String cedula);

    // Verificar si la cédula pertenece a otro usuario al editar
    boolean existsByCedulaAndIdNot(String cedula, Long id);


    List<Usuario> findByNombreContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String nombre,
            String username
    );
        //busca el nombre o el usuario, ignora mayúsculas y minúsculas
    
}

