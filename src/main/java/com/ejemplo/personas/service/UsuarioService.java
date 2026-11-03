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


    // REGISTRO DE CLIENTES
    @Transactional
    public Usuario registrarCliente(Usuario usuario) {

        // Asignar siempre el rol CLIENTE
        usuario.setRol("CLIENTE");

        // Activar la cuenta automáticamente
        usuario.setActivo(true);

        // Evitar que se reutilice un ID enviado desde el formulario
        usuario.setId(null);

        // Reutilizar las validaciones y el cifrado BCrypt
        return crear(usuario);
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

    public Usuario buscarPorUsername(String username) {

        // Buscar al usuario por su nombre de usuario
        return repo.findByUsername(username)
                .orElseThrow(() ->
                    new IllegalArgumentException("Usuario no encontrado"));
    }


    @Transactional
    public void actualizarMiPerfil(
            String usernameActual,
            String nombre,
            String email,
            String cedula,
            String telefono,
            String nuevaPassword) {

        Usuario usuario = repo.findByUsername(usernameActual)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuario no encontrado"));

        // Validar campos obligatorios
        if (nombre == null || nombre.isBlank()
                || email == null || email.isBlank()
                || cedula == null || !cedula.matches("\\d{9}")
                || telefono == null || !telefono.matches("\\d{8}")) {
            throw new IllegalArgumentException(
                    "Verifica los datos del formulario");
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Correo electrónico inválido");
        }

        // Evitar correos y cédulas duplicadas
        if (repo.existsByEmailAndIdNot(email, usuario.getId())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        if (repo.existsByCedulaAndIdNot(cedula, usuario.getId())) {
            throw new IllegalArgumentException("La cédula ya está registrada");
        }

        // Actualizar únicamente los campos permitidos
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setCedula(cedula);
        usuario.setTelefono(telefono);

        // Cambiar contraseña solo cuando se indique una nueva
        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        }

        repo.save(usuario);
    }


}
 
