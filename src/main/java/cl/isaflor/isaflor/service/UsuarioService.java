
package cl.isaflor.isaflor.service;

import java.util.List;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import cl.isaflor.isaflor.dto.UsuarioDTO;
import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.repository.UsuarioRepository;
import cl.isaflor.isaflor.repository.ComunaRepository;
import cl.isaflor.isaflor.dto.RegistroUsuarioDTO;
import cl.isaflor.isaflor.model.Comuna;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioValidaciones usuarioValidaciones;
    private final UsuarioRepository usuarioRepository;
    private final ComunaRepository comunaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioValidaciones usuarioValidaciones,
            UsuarioRepository usuarioRepository,
            ComunaRepository comunaRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioValidaciones = usuarioValidaciones;
        this.usuarioRepository = usuarioRepository;
        this.comunaRepository = comunaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Listar todos los usuarios
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuarioValidaciones::convertirADTO)
                .toList();
    }

    // Buscar usuario por ID
    public UsuarioDTO buscarPorId(Integer id) {
        Usuario usuario = buscarUsuarioEntidad(id);
        return usuarioValidaciones.convertirADTO(usuario);
    }

    // Registrar usuario
    public UsuarioDTO guardarUsuario(Usuario usuario) {

        usuario.setPassword(
                generarHashPassword(usuario.getPassword())
        );

        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        return usuarioValidaciones.convertirADTO(nuevoUsuario);
    }


    // Registro seguro de clientes
    public UsuarioDTO registrarCliente(RegistroUsuarioDTO datos) {

        String run = datos.getRun().trim();
        String correo = datos.getCorreo().trim()
                .toLowerCase(Locale.ROOT);

        // Validar RUN duplicado
        if (usuarioRepository.existsByRun(run)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El RUN ya se encuentra registrado"
            );
        }

        // Validar correo duplicado
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya se encuentra registrado"
            );
        }

        // Comprobar que la comuna exista
        Comuna comuna = comunaRepository
                .findById(datos.getIdComuna())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La comuna seleccionada no existe"
                ));

        // Crear el nuevo usuario
        Usuario usuario = new Usuario();

        usuario.setRun(run);
        usuario.setNombre(datos.getNombre());
        usuario.setApellidoPaterno(datos.getApellidoPaterno());
        usuario.setApellidoMaterno(datos.getApellidoMaterno());
        usuario.setCorreo(correo);
        usuario.setPassword(datos.getPassword());
        usuario.setFechaNacimiento(datos.getFechaNacimiento());
        usuario.setDireccion(datos.getDireccion());
        usuario.setComuna(comuna);

        // Asignar el tipo de usuario desde el backend
        usuario.setTipoUsuario("cliente");

        // Guardar utilizando el método que aplica BCrypt
        return guardarUsuario(usuario);
    }


    // Actualizar usuario
    public UsuarioDTO actualizarUsuario(Integer id, Usuario datos) {

        Usuario usuario = buscarUsuarioEntidad(id);

        usuario.setRun(datos.getRun());
        usuario.setNombre(datos.getNombre());
        usuario.setApellidoPaterno(datos.getApellidoPaterno());
        usuario.setApellidoMaterno(datos.getApellidoMaterno());
        usuario.setCorreo(datos.getCorreo());

        // Actualizar contraseña solamente si se proporciona una nueva
        if (datos.getPassword() != null &&
                !datos.getPassword().isBlank()) {

            usuario.setPassword(
                    generarHashPassword(datos.getPassword())
            );
        }

        usuario.setFechaNacimiento(datos.getFechaNacimiento());
        usuario.setTipoUsuario(datos.getTipoUsuario());
        usuario.setDireccion(datos.getDireccion());
        usuario.setComuna(datos.getComuna());

        Usuario actualizado = usuarioRepository.save(usuario);

        return usuarioValidaciones.convertirADTO(actualizado);
    }

    // Validar contraseña y generar hash BCrypt
    private String generarHashPassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres."
            );
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "La contraseña supera el límite permitido por BCrypt."
            );
        }

        return passwordEncoder.encode(password);
    }

    // Buscar entidad por ID
    private Usuario buscarUsuarioEntidad(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el usuario con ID: " + id));
    }

    // Eliminar usuario
    public void eliminarUsuario(Integer id) {
        Usuario usuario = buscarUsuarioEntidad(id);
        usuarioRepository.delete(usuario);
    }
}
