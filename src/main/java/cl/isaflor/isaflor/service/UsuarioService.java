package cl.isaflor.isaflor.service;

import java.util.List;

import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.repository.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Listar todos los usuarios
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    // Buscar usuario por ID
    public Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el usuario con ID: " + id));
    }

    // Registrar usuario
    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Actualizar usuario
    public Usuario actualizarUsuario(Integer id, Usuario datos) {
        Usuario usuario = buscarPorId(id);

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

        return usuarioRepository.save(usuario);
    }

    // Eliminar usuario
    public void eliminarUsuario(Integer id) {
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
    }
    
}
