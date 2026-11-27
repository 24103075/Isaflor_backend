package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import cl.isaflor.isaflor.config.JwtAuthenticationFilter;
import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.service.JwtService;

class JwtAuthenticationFilterTest {

    private static final String TEST_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void filtro_AutenticaTokenDeAdministradorConRolCorrecto() throws Exception {
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        Usuario usuario = new Usuario();
        usuario.setCorreo("admin@example.cl");
        usuario.setTipoUsuario("administrador");
        String token = jwtService.generarToken(usuario);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("ROLE_ADMINISTRADOR", SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().iterator().next().getAuthority());
        assertEquals(200, response.getStatus());
    }

    @Test
    void filtro_RechazaTokenAlterado() throws Exception {
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer not-a-valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
