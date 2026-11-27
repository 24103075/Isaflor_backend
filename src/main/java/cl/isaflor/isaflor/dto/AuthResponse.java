package cl.isaflor.isaflor.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UsuarioDTO usuario) {
}
