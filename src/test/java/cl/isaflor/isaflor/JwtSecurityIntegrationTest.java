package cl.isaflor.isaflor;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
class JwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private Usuario administrador;
    private Usuario cliente;

    @BeforeEach
    void prepararUsuarios() {
        administrador = crearUsuario("admin@example.cl", "administrador");
        cliente = crearUsuario("cliente@example.cl", "cliente");

        when(usuarioRepository.findByCorreoIgnoreCase("admin@example.cl"))
                .thenReturn(Optional.of(administrador));
        when(usuarioRepository.findByCorreoIgnoreCase("cliente@example.cl"))
                .thenReturn(Optional.of(cliente));
    }

    @Test
    void loginValido_EntregaTokenJwt() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "admin@example.cl",
                                  "password": "ClaveAdmin123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode respuesta = objectMapper.readTree(
                resultado.getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertEquals("Bearer",
                respuesta.get("tokenType").asText());
        org.junit.jupiter.api.Assertions.assertFalse(
                respuesta.get("accessToken").asText().isBlank());
    }

    @Test
    void operacionesAdministrativas_RequierenRolAdministrador() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());

        String tokenCliente = iniciarSesion("cliente@example.cl");
        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden());

        String tokenAdministrador = iniciarSesion("admin@example.cl");
        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenAdministrador))
                .andExpect(status().isNoContent());
    }

    private String iniciarSesion(String correo) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginDatos(correo, "ClaveAdmin123!"))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(resultado.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private Usuario crearUsuario(String correo, String tipoUsuario) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode("ClaveAdmin123!"));
        usuario.setTipoUsuario(tipoUsuario);
        usuario.setNombre("Usuario");
        return usuario;
    }

    private record LoginDatos(String correo, String password) {
    }
}
