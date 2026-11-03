package com.gimnasio.service;

import com.gimnasio.model.Usuario;
import com.gimnasio.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service 
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repo;

    public UsuarioDetailsService(UsuarioRepository repo){
        this.repo = repo;
    }

    @Override 
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = repo.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("El usuario no existe: "+ username));

        return User.withUsername(u.getUsername())
                .password(u.getPassword())
                .roles(u.getRol()) //Asigna los permisos según el rol
                .disabled(!u.isActivo())//Spring Security considera la cuenta deshabilitada y rechaza su autenticación.
                .build();
    }
    
}
 