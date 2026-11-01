package com.ejemplo.personas.model.security;

import java.util.concurrent.ExecutionException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.beans.factory.annotation.Value;


@Configuration 
public class SecurityConfig {
    
    @Value("${app.security.enabled:true}")
    private boolean securityEnabled; 

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http) throws ExecutionException{

        if (!securityEnabled){
            http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.sameOrigin()));
            return http.build();
        }

        http
            
            .authorizeHttpRequests(auth -> auth
                // Permitir el acceso al login
                .requestMatchers("/login").permitAll()

                // Proteger el acceso a la consola H2
                .requestMatchers("/h2-console/**").hasRole("ADMIN")

                // Solo los administradores pueden gestionar usuarios
                .requestMatchers(
                    "/usuarios",
                    "/usuarios/nuevo",
                    "/usuarios/guardar",
                    "/usuarios/editar/**",
                    "/usuarios/actualizar/**",
                    "/usuarios/eliminar/**"
                ).hasRole("ADMIN")

                // Las demás rutas requieren iniciar sesión
                .anyRequest().authenticated()
            )
            
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {

                    // Comprobar si el usuario tiene rol ADMIN
                    boolean esAdmin = authentication.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                    if (esAdmin) {
                        // Administrador: mostrar lista de usuarios
                        response.sendRedirect(
                            request.getContextPath() + "/usuarios");
                    } else {
                        // El usuario normal ingresa a su propio perfil
                        response.sendRedirect(
                            request.getContextPath() + "/usuarios/mi-perfil");
                    }
                })
                .failureUrl("/login?error")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )

            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(h -> h.frameOptions(f -> f.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    
}
 