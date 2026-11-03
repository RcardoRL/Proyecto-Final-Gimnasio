
package com.ejemplo.personas.model.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;


    // Seguridad principal del gimnasio
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        if (!securityEnabled) {
            http
                .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.sameOrigin()));

            return http.build();
        }

        http
            .authorizeHttpRequests(auth -> auth

                // Rutas públicas
                .requestMatchers(
                    "/login",
                    "/registro",
                    "/recuperar-password",
                    "/restablecer-password"
                ).permitAll()

                // Consola H2
                .requestMatchers("/h2-console/**")
                .hasRole("ADMIN")

                // Gestión administrativa
                .requestMatchers(
                    "/usuarios",
                    "/usuarios/nuevo",
                    "/usuarios/guardar",
                    "/usuarios/editar/**",
                    "/usuarios/actualizar/**"
                ).hasRole("ADMIN")

                // Gestión de clases: administradores y entrenadores
                .requestMatchers("/clases", "/clases/**")
                .hasAnyRole("ADMIN", "ENTRENADOR")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")

                .successHandler((request, response, authentication) -> {

                    boolean esAdmin = authentication.getAuthorities()
                        .stream()
                        .anyMatch(a ->
                            a.getAuthority().equals("ROLE_ADMIN")
                        );

                    if (esAdmin) {
                        response.sendRedirect(
                            request.getContextPath() + "/usuarios"
                        );
                    } else {
                        response.sendRedirect(
                            request.getContextPath() + "/usuarios/mi-perfil"
                        );
                    }
                })

                .failureHandler((request, response, exception) -> {

                    if (exception instanceof DisabledException) {
                        response.sendRedirect(
                            request.getContextPath() + "/login?desactivado"
                        );
                    } else {
                        response.sendRedirect(
                            request.getContextPath() + "/login?error"
                        );
                    }
                })
            )

            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )

            .csrf(csrf ->
                csrf.ignoringRequestMatchers("/h2-console/**")
            )

            .headers(h ->
                h.frameOptions(f -> f.sameOrigin())
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
