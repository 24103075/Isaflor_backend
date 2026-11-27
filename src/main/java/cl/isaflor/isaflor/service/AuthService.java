package cl.isaflor.isaflor.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.AuthResponse;
import cl.isaflor.isaflor.dto.LoginRequest;
import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioValidaciones usuarioValidaciones;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UsuarioValidaciones usuarioValidaciones) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.usuarioValidaciones = usuarioValidaciones;
    }

    public AuthResponse login(LoginRequest datos) {
        String correo = datos.correo().trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(this::credencialesInvalidas);

        if (!passwordEncoder.matches(datos.password(), usuario.getPassword())) {
            throw credencialesInvalidas();
        }

        String token = jwtService.generarToken(usuario);
        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs() / 1000,
                usuarioValidaciones.convertirADTO(usuario));
    }

    private ResponseStatusException credencialesInvalidas() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Correo o contraseña incorrectos");
    }
}
