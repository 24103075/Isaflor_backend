
package cl.isaflor.isaflor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import cl.isaflor.isaflor.dto.RegistroUsuarioDTO;
import cl.isaflor.isaflor.dto.UsuarioDTO;
import cl.isaflor.isaflor.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Registro de nuevos clientes
    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> registrarCliente(
            @Valid @RequestBody RegistroUsuarioDTO datos) {

        UsuarioDTO usuario = usuarioService.registrarCliente(datos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuario);
    }
}
