package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.RegistroUsuarioDTO;
import cl.isaflor.isaflor.dto.UsuarioDTO;
import cl.isaflor.isaflor.model.Comuna;
import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.repository.ComunaRepository;
import cl.isaflor.isaflor.repository.UsuarioRepository;
import cl.isaflor.isaflor.service.UsuarioService;
import cl.isaflor.isaflor.service.UsuarioValidaciones;

@SpringBootTest
public class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private ComunaRepository comunaRepository;

    @MockitoBean
    private UsuarioValidaciones usuarioValidaciones;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private Usuario crearUsuario() {
        Comuna comuna = new Comuna();
        comuna.setIdComuna(1);
        comuna.setNombreComuna("Santiago");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setRun("12345678-5");
        usuario.setNombre("Ana");
        usuario.setApellidoPaterno("Perez");
        usuario.setApellidoMaterno("Rojas");
        usuario.setCorreo("ana@example.cl");
        usuario.setPassword("Clave123!");
        usuario.setFechaNacimiento(LocalDate.of(1995, 5, 10));
        usuario.setTipoUsuario("cliente");
        usuario.setDireccion("Av. Siempre Viva 123");
        usuario.setComuna(comuna);

        return usuario;
    }

    private UsuarioDTO crearUsuarioDTO() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(1);
        dto.setRun("12345678-5");
        dto.setNombre("Ana");
        dto.setApellidoPaterno("Perez");
        dto.setApellidoMaterno("Rojas");
        dto.setCorreo("ana@example.cl");
        dto.setFechaNacimiento(LocalDate.of(1995, 5, 10));
        dto.setTipoUsuario("cliente");
        dto.setDireccion("Av. Siempre Viva 123");
        dto.setIdComuna(1);
        return dto;
    }

    // PRUEBA PARA LISTAR USUARIOS
    @Test
    void testListarUsuarios() {
        Usuario usuario = crearUsuario();
        UsuarioDTO dto = crearUsuarioDTO();

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(usuarioValidaciones.convertirADTO(usuario)).thenReturn(dto);

        List<UsuarioDTO> resultado = usuarioService.listarUsuarios();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Ana", resultado.get(0).getNombre());
        verify(usuarioRepository, times(1)).findAll();
    }

    // PRUEBA PARA LISTAR SIN USUARIOS
    @Test
    void testListarUsuarios_Vacio() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        List<UsuarioDTO> resultado = usuarioService.listarUsuarios();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(usuarioRepository, times(1)).findAll();
    }

    // PRUEBA DE BUSCAR USUARIO POR ID
    @Test
    void testBuscarPorId_Exitoso() {
        Usuario usuario = crearUsuario();
        UsuarioDTO dto = crearUsuarioDTO();

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioValidaciones.convertirADTO(usuario)).thenReturn(dto);

        UsuarioDTO resultado = usuarioService.buscarPorId(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdUsuario());
        assertEquals("Ana", resultado.getNombre());
    }

    // PRUEBA DE BUSCAR USUARIO INEXISTENTE
    @Test
    void testBuscarPorId_NoEncontrado() {
        when(usuarioRepository.findById(2)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> usuarioService.buscarPorId(2));

        assertEquals("No se encontro el usuario con ID: 2", exception.getMessage());
    }

    // PRUEBA DE GUARDAR USUARIO
    @Test
    void testGuardarUsuario_Exitoso() {
        Usuario usuario = crearUsuario();
        UsuarioDTO dto = crearUsuarioDTO();

        when(passwordEncoder.encode("Clave123!")).thenReturn("$2a$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioValidaciones.convertirADTO(usuario)).thenReturn(dto);

        UsuarioDTO resultado = usuarioService.guardarUsuario(usuario);

        assertNotNull(resultado);
        assertEquals("12345678-5", resultado.getRun());
        assertEquals("$2a$hash", usuario.getPassword());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    // PRUEBA DE GUARDAR USUARIO SIN CONTRASENA
    @Test
    void testGuardarUsuario_SinContrasena() {
        Usuario usuario = crearUsuario();
        usuario.setPassword("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.guardarUsuario(usuario));

        assertEquals("La contraseña es obligatoria.", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // PRUEBA DE REGISTRAR CLIENTE
    @Test
    void testRegistrarCliente_Exitoso() {
        RegistroUsuarioDTO datos = crearRegistroDTO();
        Usuario usuario = crearUsuario();
        UsuarioDTO dto = crearUsuarioDTO();

        when(usuarioRepository.existsByRun("12345678-5")).thenReturn(false);
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@example.cl")).thenReturn(false);
        when(comunaRepository.findById(1)).thenReturn(Optional.of(usuario.getComuna()));
        when(passwordEncoder.encode("Clave123!")).thenReturn("$2a$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioValidaciones.convertirADTO(any(Usuario.class))).thenReturn(dto);

        UsuarioDTO resultado = usuarioService.registrarCliente(datos);

        assertNotNull(resultado);
        assertEquals("cliente", resultado.getTipoUsuario());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    // PRUEBA DE REGISTRAR CLIENTE CON RUN DUPLICADO
    @Test
    void testRegistrarCliente_RunDuplicado() {
        RegistroUsuarioDTO datos = crearRegistroDTO();

        when(usuarioRepository.existsByRun("12345678-5")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.registrarCliente(datos));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("El RUN ya se encuentra registrado", exception.getReason());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // PRUEBA DE REGISTRAR CLIENTE CON COMUNA INEXISTENTE
    @Test
    void testRegistrarCliente_ComunaInexistente() {
        RegistroUsuarioDTO datos = crearRegistroDTO();

        when(usuarioRepository.existsByRun("12345678-5")).thenReturn(false);
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@example.cl")).thenReturn(false);
        when(comunaRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.registrarCliente(datos));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("La comuna seleccionada no existe", exception.getReason());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // PRUEBA DE ACTUALIZAR USUARIO
    @Test
    void testActualizarUsuario_Exitoso() {
        Usuario usuarioExistente = crearUsuario();
        Usuario datos = crearUsuario();
        UsuarioDTO dto = crearUsuarioDTO();
        datos.setNombre("Carla");
        datos.setPassword(null);
        dto.setNombre("Carla");

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(usuarioExistente)).thenReturn(usuarioExistente);
        when(usuarioValidaciones.convertirADTO(usuarioExistente)).thenReturn(dto);

        UsuarioDTO resultado = usuarioService.actualizarUsuario(1, datos);

        assertNotNull(resultado);
        assertEquals("Carla", resultado.getNombre());
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(usuarioRepository, times(1)).save(usuarioExistente);
    }

    // PRUEBA DE ACTUALIZAR USUARIO INEXISTENTE
    @Test
    void testActualizarUsuario_NoEncontrado() {
        Usuario datos = crearUsuario();

        when(usuarioRepository.findById(2)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> usuarioService.actualizarUsuario(2, datos));

        assertEquals("No se encontro el usuario con ID: 2", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // PRUEBA DE ELIMINAR USUARIO
    @Test
    void testEliminarUsuario_Exitoso() {
        Usuario usuario = crearUsuario();

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        usuarioService.eliminarUsuario(1);

        verify(usuarioRepository, times(1)).delete(usuario);
    }

    // PRUEBA DE ELIMINAR USUARIO INEXISTENTE
    @Test
    void testEliminarUsuario_NoEncontrado() {
        when(usuarioRepository.findById(2)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> usuarioService.eliminarUsuario(2));

        assertEquals("No se encontro el usuario con ID: 2", exception.getMessage());
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    private RegistroUsuarioDTO crearRegistroDTO() {
        RegistroUsuarioDTO datos = new RegistroUsuarioDTO();
        datos.setRun("12345678-5");
        datos.setNombre("Ana");
        datos.setApellidoPaterno("Perez");
        datos.setApellidoMaterno("Rojas");
        datos.setCorreo("Ana@Example.cl");
        datos.setPassword("Clave123!");
        datos.setFechaNacimiento(LocalDate.of(1995, 5, 10));
        datos.setDireccion("Av. Siempre Viva 123");
        datos.setIdComuna(1);
        return datos;
    }
}
