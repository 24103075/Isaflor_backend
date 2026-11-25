package cl.isaflor.isaflor.service;

import org.springframework.stereotype.Service;
import cl.isaflor.isaflor.dto.UsuarioDTO;
import cl.isaflor.isaflor.model.Usuario;

@Service 
public class UsuarioValidaciones {


    public Boolean validarNullVacio (Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()){
            return false;
        }
        return true;
    }

    
    public UsuarioDTO convertirADTO (Usuario usuario) {
        
        UsuarioDTO usuarioDTO = new UsuarioDTO();

        usuarioDTO.setIdUsuario(usuario.getIdUsuario());
        usuarioDTO.setRun(usuario.getRun());
        usuarioDTO.setNombre(usuario.getNombre());
        usuarioDTO.setApellidoPaterno(usuario.getApellidoPaterno());
        usuarioDTO.setApellidoMaterno(usuario.getApellidoMaterno());
        usuarioDTO.setCorreo(usuario.getCorreo());
        usuarioDTO.setFechaNacimiento(usuario.getFechaNacimiento());
        usuarioDTO.setTipoUsuario(usuario.getTipoUsuario());
        usuarioDTO.setDireccion(usuario.getDireccion());

        if (usuario.getComuna() != null) {
            usuarioDTO.setIdComuna(usuario.getComuna().getIdComuna());
        }

        return usuarioDTO;
    }
    
}
