
package com.ejemplo.personas.config;

import com.ejemplo.personas.model.Usuario;
import com.ejemplo.personas.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner cargarUsuarios( //ejecuta codigo cuando inicia la aplicación
            UsuarioRepository repo,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (!repo.existsByUsername("admin")) { //existsByUsername, evita crear usuario duplicados
                Usuario admin = new Usuario(
                    "Administrador",
                    "admin",
                    "admin@ejemplo.com",
                    passwordEncoder.encode("admin123"),
                    "ADMIN"
                );

                
                    admin.setCedula("101230456");
                    admin.setTelefono("88881234");
                    admin.setActivo(true);


                repo.save(admin);
            }

            if (!repo.existsByUsername("maria")) {
                Usuario maria = new Usuario(
                    "María Pérez",
                    "maria",
                    "maria@ejemplo.com",
                    passwordEncoder.encode("maria123"),
                    "CLIENTE"
                );
                    //datos para pruebas
                maria.setCedula("102340567");
                maria.setTelefono("88885678");
                maria.setActivo(true);

                repo.save(maria);
            }
        };
    }
}
