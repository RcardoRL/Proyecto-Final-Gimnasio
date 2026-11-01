package com.ejemplo.personas.service;

import com.ejemplo.personas.model.Usuario;
import com.ejemplo.personas.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service 
public class UsuarioService {
    
    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder passwordEncoder){
        this.repo = repo;
        this.passwordEncoder=passwordEncoder;
    }

    //LISTAR USUARIOS
    public List<Usuario> listar(){
        return repo.findAll();
    }


    public List<Usuario> buscar(String q) {

        if (q == null || q.isBlank()) {
            return repo.findAll();
        }

        String texto = q.trim(); //elimina espacios al inicio y final

        return repo.findByNombreContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            texto,
            texto
        );
    }


    //Listar 1 usuario
  
    public Usuario buscarPorId(Long id) {

        // Buscar el usuario por su ID
        // Si no existe, devolver null
        return repo.findById(id).orElse(null);
    }


    //CREAR USUARIOS 
    @Transactional 
    public Usuario crear(Usuario usuario){
        if (repo.existsByUsername(usuario.getUsername())){
            throw new IllegalArgumentException("Este usuario ya existe!");
        }
        if(usuario.getPassword() == null || usuario.getPassword().isBlank()){
            throw new IllegalArgumentException("La contrasena es obligatoria!");
        }

        if (repo.existsByEmail(usuario.getEmail())) {//consulta si el correo ya existe en la base de datos.
            throw new IllegalArgumentException(
                "El correo electrónico ya está registrado."
            );
        }

        if (repo.existsByCedula(usuario.getCedula())) {
            throw new IllegalArgumentException(
                "La cédula ya está registrada."
            );
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return repo.save(usuario);

    }

    //ACTUALIZAR USUARIOS 
    @Transactional 
    public Usuario actualizar(Long id, Usuario datos){
        Usuario existente = buscarPorId(id);


        if (repo.existsByEmailAndIdNot(datos.getEmail(), id)) {
            throw new IllegalArgumentException(
                "El correo electrónico ya está registrado por otro usuario."
            );
        }

        if(!existente.getUsername().equals(datos.getUsername())
            && repo.existsByUsername(datos.getUsername())){
                throw new IllegalArgumentException("El username ya existe!");
            }

        existente.setNombre(datos.getNombre());
        existente.setUsername(datos.getUsername());
        existente.setEmail(datos.getEmail());
        existente.setCedula(datos.getCedula());
        existente.setRol(datos.getRol());
        existente.setTelefono(datos.getTelefono());
        existente.setActivo(datos.isActivo());

        if (datos.getPassword() != null && !datos.getPassword().isBlank()){
            existente.setPassword(passwordEncoder.encode(datos.getPassword()));
        }

        return repo.save(existente);

    }

    //ELIMINAR 
    @Transactional
    public void eliminar(Long id, String usernameActual) {

        // Buscar el usuario que se desea eliminar
        Usuario usuario = repo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuario no encontrado"));

        // Evitar que el administrador elimine su propia cuenta
        if (usuario.getUsername().equals(usernameActual)) {
            throw new IllegalArgumentException(
                    "No puedes eliminar tu propia cuenta");
        }

        // Eliminar el usuario
        repo.delete(usuario);
    }


    public Usuario buscarPorUsername(String username) {

        // Buscar al usuario por su nombre de usuario
        return repo.findByUsername(username)
                .orElseThrow(() ->
                    new IllegalArgumentException("Usuario no encontrado"));
    }



}
 
