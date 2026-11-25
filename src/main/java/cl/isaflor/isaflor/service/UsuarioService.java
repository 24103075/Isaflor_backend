
package cl.isaflor.isaflor.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.isaflor.isaflor.dto.UsuarioDTO;
import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.repository.UsuarioRepository;
import cl.isaflor.isaflor.repository.ComunaRepository;

@Service
public class UsuarioService {

    private final UsuarioValidaciones usuarioValidaciones;
    private final UsuarioRepository usuarioRepository;
    private final ComunaRepository comunaRepository;
    public UsuarioService(
            UsuarioValidaciones usuarioValidaciones,
            UsuarioRepository usuarioRepository,
            ComunaRepository comunaRepository) {

        this.usuarioValidaciones = usuarioValidaciones;
        this.usuarioRepository = usuarioRepository;
        this.comunaRepository = comunaRepository;
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
        Usuario nuevoUsuario = usuarioRepository.save(usuario);
        return usuarioValidaciones.convertirADTO(nuevoUsuario);
    }

    // Actualizar usuario
    public UsuarioDTO actualizarUsuario(Integer id, Usuario datos) {

        Usuario usuario = buscarUsuarioEntidad(id);

        usuario.setRun(datos.getRun());
        usuario.setNombre(datos.getNombre());
        usuario.setApellidoPaterno(datos.getApellidoPaterno());
        usuario.setApellidoMaterno(datos.getApellidoMaterno());
        usuario.setCorreo(datos.getCorreo());
        usuario.setPassword(datos.getPassword());
        usuario.setFechaNacimiento(datos.getFechaNacimiento());
        usuario.setTipoUsuario(datos.getTipoUsuario());
        usuario.setDireccion(datos.getDireccion());
        usuario.setComuna(datos.getComuna());

        Usuario actualizado = usuarioRepository.save(usuario);

        return usuarioValidaciones.convertirADTO(actualizado);
    }

    // Buscar entidad por ID
    private Usuario buscarUsuarioEntidad(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontro el usuario con ID: " + id));
    }

    // Eliminar usuario
    public void eliminarUsuario(Integer id) {
        Usuario usuario = buscarUsuarioEntidad(id);
        usuarioRepository.delete(usuario);
    }
}
