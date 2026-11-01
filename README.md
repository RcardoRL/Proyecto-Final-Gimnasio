# Sistema de Gestión de Usuarios

**Estudiante:** Ricardo Ramírez Luna

## Descripción

Este proyecto se desarrolló en Java con Spring Boot para realizar un sistema de gestión de usuarios con inicio de sesión y operaciones CRUD.

Se trabajaron cinco ejercicios relacionados con validaciones, búsqueda, seguridad y consulta de información.

## Cómo ejecutar el proyecto

1. Abrir el proyecto en VS Code.
2. Abrir una terminal en la carpeta principal del proyecto.
3. Ejecutar el comando:

   `./mvnw spring-boot:run`

4. Abrir en el navegador:

   http://localhost:8080/login

**Usuarios de prueba:**

- Administrador: `admin` / `admin123`
- Usuario normal: `maria` / `maria123`

## Cómo correr las pruebas

Para ejecutar las pruebas automáticas del proyecto, utilizar:

`./mvnw test`

Para comprobar que el proyecto compila y genera el archivo de ejecución:

`./mvnw clean package`

También se realizaron pruebas manuales desde el navegador para verificar las funcionalidades:

- Registrar usuarios y validar teléfonos de 8 dígitos.
- Intentar registrar correos electrónicos repetidos.
- Buscar usuarios por nombre o nombre de usuario.
- Comprobar que los usuarios inactivos no puedan iniciar sesión.
- Verificar que el administrador no pueda eliminar su propia cuenta.
- Consultar los detalles de un usuario y comprobar los permisos de edición.
- Consultar un ID inexistente para verificar el mensaje de error.

## Archivos modificados o creados por ejercicio

### Ejercicio 1. Teléfono y estado activo

- `Usuario.java`: se agregaron los campos teléfono y activo con sus validaciones.
- `formulario.html`: se agregaron los campos al formulario.
- `lista.html`: se agregó el teléfono y el estado a la tabla.
- `UsuarioService.java`: se actualizaron los datos que se guardan y editan.

### Ejercicio 2. Correo electrónico único

- `UsuarioRepository.java`: se agregaron métodos para verificar correos repetidos.
- `UsuarioService.java`: se agregaron las validaciones al crear y editar.
- `UsuarioController.java`: se manejan los mensajes de error en el formulario.

### Ejercicio 3. Búsqueda de usuarios

- `UsuarioRepository.java`: se agregó la búsqueda por nombre o username.
- `UsuarioService.java`: se agregó el método de búsqueda.
- `UsuarioController.java`: se recibe el texto de búsqueda.
- `lista.html`: se agregó el buscador y el botón Limpiar.

### Ejercicio 4. Seguridad de usuarios

- `UsuarioDetailsService.java`: se bloqueó el inicio de sesión de usuarios inactivos.
- `UsuarioService.java`: se agregó la validación para impedir que un administrador elimine su propia cuenta.
- `UsuarioController.java`: se agregó la eliminación de usuarios.
- `SecurityConfig.java`: se configuraron los permisos según el rol.
- `lista.html`: se agregó el botón Eliminar y los mensajes de confirmación.

### Ejercicio 5. Detalles de usuario

- `UsuarioController.java`: se agregaron las rutas para consultar detalles y el perfil del usuario.
- `UsuarioService.java`: se agregaron métodos para buscar usuarios.
- `detalle.html`: archivo creado para mostrar los datos del usuario.
- `no-encontrado.html`: archivo creado para mostrar un mensaje cuando el usuario no existe.
- `lista.html`: se agregó el botón Ver detalles.
- `SecurityConfig.java`: se ajustó la redirección después del inicio de sesión.

## Observaciones

Se utilizó una base de datos H2 en memoria, por lo que los datos se reinician cuando se detiene la aplicación.

Las contraseñas se almacenan cifradas con BCrypt y las operaciones administrativas están protegidas mediante Spring Security.