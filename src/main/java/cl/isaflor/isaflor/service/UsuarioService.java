
package cl.isaflor.isaflor.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.RegistroUsuarioDTO;
import cl.isaflor.isaflor.dto.UsuarioDTO;
import cl.isaflor.isaflor.model.Comuna;
import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.repository.ComunaRepository;
import cl.isaflor.isaflor.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ComunaRepository comunaRepository;
    private final UsuarioValidaciones usuarioValidaciones;
    private final PasswordEncoder passwordEncoder;

    // Constructor
    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ComunaRepository comunaRepository,
            UsuarioValidaciones usuarioValidaciones,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.comunaRepository = comunaRepository;
        this.usuarioValidaciones = usuarioValidaciones;
        this.passwordEncoder = passwordEncoder;
    }

    // LISTAR USUARIOS
    public List<UsuarioDTO> listarUsuarios() {

        try {
            return usuarioRepository.findAll().stream().map(usuarioValidaciones::convertirADTO)
                    .toList();

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Error al listar los usuarios: " + e.getMessage(), e);
        }
    }

    // BUSCAR USUARIO POR ID
    public UsuarioDTO buscarPorId(Integer id) {

        try {
            Usuario usuario = buscarUsuarioEntidad(id);

            return usuarioValidaciones.convertirADTO(usuario);

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Error al buscar el usuario: " + e.getMessage(), e);
        }
    }

    // GUARDAR USUARIO
    public UsuarioDTO guardarUsuario(Usuario usuario) {

        try {
            usuario.setPassword(
                    generarHashPassword(usuario.getPassword()));

            Usuario nuevoUsuario = usuarioRepository.save(usuario);

            return usuarioValidaciones.convertirADTO(nuevoUsuario);

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el usuario: " + e.getMessage(), e);
        }
    }

    // REGISTRO SEGURO DE CLIENTES
    public UsuarioDTO registrarCliente(RegistroUsuarioDTO datos) {

        try {
            String run = datos.getRun().trim();

            String correo = datos.getCorreo().trim().toLowerCase(Locale.ROOT);

            // Validar RUN duplicado
            if (usuarioRepository.existsByRun(run)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,"El RUN ya se encuentra registrado");
            }

            // Validar correo duplicado
            if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,"El correo ya se encuentra registrado");
            }

            // Comprobar que la comuna exista
            Comuna comuna = comunaRepository
                    .findById(datos.getIdComuna())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"La comuna seleccionada no existe"));

            // Crear usuario
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

            // El tipo de usuario se asigna desde el backend
            usuario.setTipoUsuario("cliente");

            return guardarUsuario(usuario);

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Error al registrar el cliente: " + e.getMessage(), e);
        }
    }

    // ACTUALIZAR USUARIO
    public UsuarioDTO actualizarUsuario(Integer id, Usuario datos) {

        try {
            Usuario usuario = buscarUsuarioEntidad(id);

            usuario.setRun(datos.getRun());
            usuario.setNombre(datos.getNombre());
            usuario.setApellidoPaterno(datos.getApellidoPaterno());
            usuario.setApellidoMaterno(datos.getApellidoMaterno());
            usuario.setCorreo(datos.getCorreo());

            // Actualizar contraseña solamente si se proporciona una nueva
            if (datos.getPassword() != null && !datos.getPassword().isBlank()) {

                usuario.setPassword(generarHashPassword(datos.getPassword()));
            }

            usuario.setFechaNacimiento(datos.getFechaNacimiento());
            usuario.setTipoUsuario(datos.getTipoUsuario());
            usuario.setDireccion(datos.getDireccion());
            usuario.setComuna(datos.getComuna());

            Usuario actualizado = usuarioRepository.save(usuario);

            return usuarioValidaciones.convertirADTO(actualizado);

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el usuario: " + e.getMessage(), e);
        }
    }

    // ELIMINAR USUARIO
    public void eliminarUsuario(Integer id) {

        try {
            Usuario usuario = buscarUsuarioEntidad(id);

            usuarioRepository.delete(usuario);

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el usuario: " + e.getMessage(), e);
        }
    }

    // BUSCAR ENTIDAD POR ID
    private Usuario buscarUsuarioEntidad(Integer id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontro el usuario con ID: " + id));
    }

    // VALIDAR CONTRASEÑA Y GENERAR HASH BCRYPT
    private String generarHashPassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña supera el limite permitido");
        }

        return passwordEncoder.encode(password);
    }
}
