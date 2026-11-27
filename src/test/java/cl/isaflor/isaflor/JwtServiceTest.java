package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.service.JwtService;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final String TEST_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void generarYValidarToken_ConservaCorreoYRol() {
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000);
        Usuario usuario = new Usuario();
        usuario.setCorreo("admin@example.cl");
        usuario.setTipoUsuario("administrador");

        String token = jwtService.generarToken(usuario);

        assertEquals("admin@example.cl", jwtService.validarToken(token).getSubject());
        assertEquals("ADMINISTRADOR", jwtService.validarToken(token).get("role"));
        assertTrue(jwtService.validarToken(token).getExpiration().getTime()
                > System.currentTimeMillis());
    }

    @Test
    void validarToken_RechazaFirmaAlterada() {
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000);
        Usuario usuario = new Usuario();
        usuario.setCorreo("admin@example.cl");
        usuario.setTipoUsuario("administrador");
        String token = jwtService.generarToken(usuario);
        int firmaInicio = token.lastIndexOf('.') + 1;
        char primerCaracter = token.charAt(firmaInicio);
        char caracterAlterado = primerCaracter == 'a' ? 'b' : 'a';
        String tokenAlterado = token.substring(0, firmaInicio)
                + caracterAlterado + token.substring(firmaInicio + 1);

        assertThrows(JwtException.class, () -> jwtService.validarToken(tokenAlterado));
    }

    @Test
    void constructor_RechazaClaveDemasiadoCorta() {
        assertThrows(IllegalStateException.class,
                () -> new JwtService("c2hvcnQ=", 60_000));
    }
}
