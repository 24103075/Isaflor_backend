package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.AuthResponse;
import cl.isaflor.isaflor.dto.LoginRequest;
import cl.isaflor.isaflor.dto.UsuarioDTO;
import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.repository.UsuarioRepository;
import cl.isaflor.isaflor.service.AuthService;
import cl.isaflor.isaflor.service.JwtService;
import cl.isaflor.isaflor.service.UsuarioValidaciones;

class AuthServiceTest {

    @Test
    void login_ValidoDevuelveTokenYUsuario() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        UsuarioValidaciones usuarioValidaciones = mock(UsuarioValidaciones.class);
        AuthService authService = new AuthService(
                usuarioRepository, passwordEncoder, jwtService, usuarioValidaciones);

        Usuario usuario = new Usuario();
        usuario.setCorreo("ana@example.cl");
        usuario.setPassword("$2a$hash");
        usuario.setTipoUsuario("cliente");
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setCorreo("ana@example.cl");

        when(usuarioRepository.findByCorreoIgnoreCase("ana@example.cl"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Clave123!", "$2a$hash")).thenReturn(true);
        when(jwtService.generarToken(usuario)).thenReturn("signed.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(3_600_000L);
        when(usuarioValidaciones.convertirADTO(usuario)).thenReturn(usuarioDTO);

        AuthResponse response = authService.login(
                new LoginRequest(" Ana@Example.cl ", "Clave123!"));

        assertEquals("signed.jwt.token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600, response.expiresIn());
        assertEquals("ana@example.cl", response.usuario().getCorreo());
        verify(usuarioRepository).findByCorreoIgnoreCase("ana@example.cl");
    }

    @Test
    void login_ContrasenaIncorrectaRespondeUnauthorized() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        UsuarioValidaciones usuarioValidaciones = mock(UsuarioValidaciones.class);
        AuthService authService = new AuthService(
                usuarioRepository, passwordEncoder, jwtService, usuarioValidaciones);
        Usuario usuario = new Usuario();
        usuario.setCorreo("ana@example.cl");
        usuario.setPassword("$2a$hash");

        when(usuarioRepository.findByCorreoIgnoreCase("ana@example.cl"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "$2a$hash")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(new LoginRequest("ana@example.cl", "incorrecta")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Correo o contraseña incorrectos", exception.getReason());
    }

    @Test
    void login_CorreoNoRegistradoRespondeUnauthorized() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        AuthService authService = new AuthService(
                usuarioRepository,
                mock(PasswordEncoder.class),
                mock(JwtService.class),
                mock(UsuarioValidaciones.class));

        when(usuarioRepository.findByCorreoIgnoreCase("desconocido@example.cl"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(
                        new LoginRequest("desconocido@example.cl", "Clave123!")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}
