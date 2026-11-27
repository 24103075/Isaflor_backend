
package cl.isaflor.isaflor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import cl.isaflor.isaflor.dto.AuthResponse;
import cl.isaflor.isaflor.dto.LoginRequest;
import cl.isaflor.isaflor.dto.RegistroUsuarioDTO;
import cl.isaflor.isaflor.dto.UsuarioDTO;
import cl.isaflor.isaflor.service.AuthService;
import cl.isaflor.isaflor.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    public AuthController(UsuarioService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",description = "Autentica al usuario y devuelve un token JWT")
    @ApiResponse(responseCode = "200", description = "Sesión iniciada")
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest datos) {

        return ResponseEntity.ok(authService.login(datos));
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
