package cl.isaflor.isaflor;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import cl.isaflor.isaflor.dto.ActualizarEstadoPagoRequest;
import cl.isaflor.isaflor.dto.ActualizarEstadoPedidoRequest;
import cl.isaflor.isaflor.dto.CrearPagoRequest;
import cl.isaflor.isaflor.dto.CrearPedidoRequest;
import cl.isaflor.isaflor.dto.PagoDTO;
import cl.isaflor.isaflor.dto.PedidoDTO;
import cl.isaflor.isaflor.dto.VideoRequestDTO;
import cl.isaflor.isaflor.dto.VideoResponseDTO;
import cl.isaflor.isaflor.service.PagoService;
import cl.isaflor.isaflor.service.PedidoService;
import cl.isaflor.isaflor.service.VideoService;
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

    @MockitoBean
    private PedidoService pedidoService;

    @MockitoBean
    private PagoService pagoService;

    @MockitoBean
    private VideoService videoService;

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

    @Test
    void tiposDocumentoSonPublicosParaLecturaYRestringidosParaEscritura()
            throws Exception {
        mockMvc.perform(get("/api/v1/tipos-documento"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/tipos-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreDocumento": "RUT",
                                  "descripcion": "Documento nacional"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        String tokenCliente = iniciarSesion("cliente@example.cl");
        mockMvc.perform(post("/api/v1/tipos-documento")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreDocumento": "RUT",
                                  "descripcion": "Documento nacional"
                                }
                                """))
                .andExpect(status().isForbidden());

        String tokenAdministrador = iniciarSesion("admin@example.cl");
        mockMvc.perform(post("/api/v1/tipos-documento")
                        .header("Authorization", "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreDocumento": "RUT",
                                  "descripcion": "Documento nacional"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void regionesSonPublicasParaLecturaYRestringidasParaEscritura()
            throws Exception {
        mockMvc.perform(get("/api/v1/regiones"))
                .andExpect(status().isNoContent());

        String jsonRegion = """
                {
                  "nombreRegion": "Region de prueba"
                }
                """;
        mockMvc.perform(post("/api/v1/regiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegion))
                .andExpect(status().isUnauthorized());

        String tokenCliente = iniciarSesion("cliente@example.cl");
        mockMvc.perform(post("/api/v1/regiones")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegion))
                .andExpect(status().isForbidden());

        String tokenAdministrador = iniciarSesion("admin@example.cl");
        mockMvc.perform(post("/api/v1/regiones")
                        .header("Authorization", "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegion))
                .andExpect(status().isCreated());
    }

    @Test
    void pedidosRespetanRolesDeClienteYAdministrador() throws Exception {
        when(pedidoService.listarPedidos("cliente@example.cl", false))
                .thenReturn(java.util.List.of());
        when(pedidoService.listarPedidos("admin@example.cl", true))
                .thenReturn(java.util.List.of());
        when(pedidoService.crearPedido(
                any(CrearPedidoRequest.class), eq("cliente@example.cl")))
                .thenReturn(new PedidoDTO());
        when(pedidoService.cambiarEstado(
                eq(1), any(ActualizarEstadoPedidoRequest.class), eq(true)))
                .thenReturn(new PedidoDTO());

        mockMvc.perform(get("/api/v1/pedidos"))
                .andExpect(status().isUnauthorized());

        String tokenCliente = iniciarSesion("cliente@example.cl");
        mockMvc.perform(get("/api/v1/pedidos")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isNoContent());

        String tokenAdministrador = iniciarSesion("admin@example.cl");
        mockMvc.perform(get("/api/v1/pedidos")
                        .header("Authorization", "Bearer " + tokenAdministrador))
                .andExpect(status().isNoContent());

        String jsonPedido = """
                {
                  "idMetodoEnvio": 1,
                  "idTipoDocumento": 1,
                  "total": 999999,
                  "estado": "PAGADO",
                  "idUsuario": 99
                }
                """;
        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPedido))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/pedidos")
                        .header("Authorization", "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPedido))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/pedidos")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPedido))
                .andExpect(status().isCreated());

        String jsonEstado = """
                {
                  "estado": "CONFIRMADO"
                }
                """;
        mockMvc.perform(patch("/api/v1/pedidos/1/estado")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEstado))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/pedidos/1/estado")
                        .header("Authorization", "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEstado))
                .andExpect(status().isOk());
    }

    @Test
    void pagosRespetanRolesYNoPermitenEliminacion() throws Exception {
        when(pagoService.listarPagos("cliente@example.cl", false))
                .thenReturn(java.util.List.of());
        when(pagoService.listarPagos("admin@example.cl", true))
                .thenReturn(java.util.List.of());
        when(pagoService.crearPago(
                any(CrearPagoRequest.class), eq("cliente@example.cl")))
                .thenReturn(new PagoDTO());
        when(pagoService.cambiarEstado(
                eq(1), any(ActualizarEstadoPagoRequest.class),
                eq("admin@example.cl"), eq(true)))
                .thenReturn(new PagoDTO());

        mockMvc.perform(get("/api/v1/pagos"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idPedido": 1,
                                  "idMetodoPago": 1
                                }
                                """))
                .andExpect(status().isUnauthorized());

        String tokenCliente = iniciarSesion("cliente@example.cl");
        mockMvc.perform(get("/api/v1/pagos")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/pagos")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idPedido": 1,
                                  "idMetodoPago": 1
                                }
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(patch("/api/v1/pagos/1/estado")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estadoPago": "PAGADO"
                                }
                                """))
                .andExpect(status().isForbidden());

        String tokenAdministrador = iniciarSesion("admin@example.cl");
        mockMvc.perform(get("/api/v1/pagos")
                        .header("Authorization", "Bearer " + tokenAdministrador))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/pagos")
                        .header("Authorization", "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idPedido": 1,
                                  "idMetodoPago": 1
                                }
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/pagos/1/estado")
                        .header("Authorization", "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estadoPago": "PAGADO"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/pagos/1"))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/v1/pagos"))
                .andExpect(status().isConflict());
    }

    @Test
    void videosSonPublicosEnLecturaYAdministrativosEnEscritura() throws Exception {
        when(videoService.listarVideos(false)).thenReturn(java.util.List.of());
        when(videoService.listarVideos(true)).thenReturn(java.util.List.of());
        when(videoService.crearVideo(
                any(VideoRequestDTO.class), eq(true)))
                .thenReturn(new VideoResponseDTO());
        when(videoService.actualizarVideo(
                eq(1), any(VideoRequestDTO.class), eq(true)))
                .thenReturn(new VideoResponseDTO());
        when(videoService.eliminarVideo(1, true))
                .thenReturn("Video eliminado correctamente");

        mockMvc.perform(get("/api/v1/videos"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idBlog": 1,
                                  "titulo": "Video de prueba",
                                  "urlVideo": "https://videos.example/test"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        String tokenCliente = iniciarSesion("cliente@example.cl");
        mockMvc.perform(post("/api/v1/videos")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idBlog": 1,
                                  "titulo": "Video de prueba",
                                  "urlVideo": "https://videos.example/test"
                                }
                                """))
                .andExpect(status().isForbidden());

        String tokenAdministrador = iniciarSesion("admin@example.cl");
        mockMvc.perform(get("/api/v1/videos")
                        .header("Authorization", "Bearer " + tokenAdministrador))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/videos")
                        .header("Authorization", "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idBlog": 1,
                                  "titulo": "Video de prueba",
                                  "urlVideo": "https://videos.example/test"
                                }
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/v1/videos/1")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idBlog": 1,
                                  "titulo": "Video actualizado",
                                  "urlVideo": "https://videos.example/test"
                                }
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/videos/1")
                        .header("Authorization", "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idBlog": 1,
                                  "titulo": "Video actualizado",
                                  "urlVideo": "https://videos.example/test"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/videos/1")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/videos/1")
                        .header("Authorization", "Bearer " + tokenAdministrador))
                .andExpect(status().isOk());
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
