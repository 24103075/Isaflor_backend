package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.ActualizarEstadoPedidoRequest;
import cl.isaflor.isaflor.dto.CrearPedidoRequest;
import cl.isaflor.isaflor.model.MetodoEnvio;
import cl.isaflor.isaflor.model.Pedido;
import cl.isaflor.isaflor.model.TipoDocumento;
import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.repository.DetallePedidoRepository;
import cl.isaflor.isaflor.repository.MetodoEnvioRepository;
import cl.isaflor.isaflor.repository.PagoRepository;
import cl.isaflor.isaflor.repository.PedidoRepository;
import cl.isaflor.isaflor.repository.TipoDocumentoRepository;
import cl.isaflor.isaflor.repository.UsuarioRepository;
import cl.isaflor.isaflor.service.PedidoService;
import cl.isaflor.isaflor.service.PedidoValidaciones;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MetodoEnvioRepository metodoEnvioRepository;

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PedidoValidaciones pedidoValidaciones;

    @InjectMocks
    private PedidoService pedidoService;

    private Usuario usuario;
    private MetodoEnvio metodoEnvio;
    private TipoDocumento tipoDocumento;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(7);
        usuario.setCorreo("cliente@isaflor.cl");

        metodoEnvio = new MetodoEnvio();
        metodoEnvio.setIdMetodoEnvio(2);

        tipoDocumento = new TipoDocumento();
        tipoDocumento.setIdTipoDocumento(3);

        pedido = crearPedido(11, usuario);
    }

    @Test
    void listarPedidosDeClienteFiltraPorCorreo() {
        when(pedidoRepository
                .findByUsuario_CorreoIgnoreCaseOrderByFechaPedidoDesc(
                        "cliente@isaflor.cl"))
                .thenReturn(List.of(pedido));

        var resultado = pedidoService.listarPedidos("cliente@isaflor.cl", false);

        assertEquals(1, resultado.size());
        assertEquals(11, resultado.get(0).getIdPedido());
        assertEquals(7, resultado.get(0).getIdUsuario());
        verify(pedidoRepository)
                .findByUsuario_CorreoIgnoreCaseOrderByFechaPedidoDesc(
                        "cliente@isaflor.cl");
        verify(pedidoRepository, never()).findAllByOrderByFechaPedidoDesc();
    }

    @Test
    void listarPedidosDeAdministradorDevuelveTodos() {
        when(pedidoRepository.findAllByOrderByFechaPedidoDesc())
                .thenReturn(List.of(pedido));

        var resultado = pedidoService.listarPedidos("admin@isaflor.cl", true);

        assertEquals(1, resultado.size());
        verify(pedidoRepository).findAllByOrderByFechaPedidoDesc();
        verify(pedidoRepository, never())
                .findByUsuario_CorreoIgnoreCaseOrderByFechaPedidoDesc(
                        "admin@isaflor.cl");
    }

    @Test
    void buscarPorIdPermiteAlPropietarioConsultarSuPedido() {
        when(pedidoRepository.findById(11)).thenReturn(Optional.of(pedido));

        var resultado = pedidoService.buscarPorId(
                11, "cliente@isaflor.cl", false);

        assertEquals(11, resultado.getIdPedido());
        assertEquals("PENDIENTE", resultado.getEstado());
    }

    @Test
    void buscarPorIdImpideConsultarPedidoDeOtroCliente() {
        when(pedidoRepository.findById(11)).thenReturn(Optional.of(pedido));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pedidoService.buscarPorId(
                        11, "otro@isaflor.cl", false));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void crearPedidoAsignaDatosDelServidorYAsociaReferencias() {
        CrearPedidoRequest request = crearSolicitud();
        when(usuarioRepository.findByCorreoIgnoreCase("cliente@isaflor.cl"))
                .thenReturn(Optional.of(usuario));
        when(metodoEnvioRepository.findById(2))
                .thenReturn(Optional.of(metodoEnvio));
        when(tipoDocumentoRepository.findById(3))
                .thenReturn(Optional.of(tipoDocumento));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido guardado = invocation.getArgument(0);
            guardado.setIdPedido(11);
            return guardado;
        });

        var resultado = pedidoService.crearPedido(
                request, "cliente@isaflor.cl");

        assertEquals(11, resultado.getIdPedido());
        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals(BigDecimal.ZERO, resultado.getTotal());
        assertNotNull(resultado.getFechaPedido());
        assertEquals(7, resultado.getIdUsuario());
        assertEquals(2, resultado.getIdMetodoEnvio());
        assertEquals(3, resultado.getIdTipoDocumento());

        ArgumentCaptor<Pedido> pedidoGuardado =
                ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(pedidoGuardado.capture());
        assertEquals(usuario, pedidoGuardado.getValue().getUsuario());
        assertEquals(metodoEnvio, pedidoGuardado.getValue().getMetodoEnvio());
        assertEquals(tipoDocumento, pedidoGuardado.getValue().getTipoDocumento());
        verify(pedidoValidaciones).validarCreacion(request);
    }

    @Test
    void cambiarEstadoActualizaPedidoComoAdministrador() {
        ActualizarEstadoPedidoRequest request = new ActualizarEstadoPedidoRequest();
        request.setEstado("  CONFIRMADO ");
        when(pedidoValidaciones.validarEstado(request)).thenReturn("CONFIRMADO");
        when(pedidoRepository.findById(11)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        var resultado = pedidoService.cambiarEstado(11, request, true);

        assertEquals("CONFIRMADO", resultado.getEstado());
        verify(pedidoValidaciones).validarTransicion("PENDIENTE", "CONFIRMADO");
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void cambiarEstadoNoGuardaTransicionNoPermitida() {
        ActualizarEstadoPedidoRequest request = new ActualizarEstadoPedidoRequest();
        request.setEstado("ENVIADO");
        when(pedidoValidaciones.validarEstado(request)).thenReturn("ENVIADO");
        when(pedidoRepository.findById(11)).thenReturn(Optional.of(pedido));
        org.mockito.Mockito.doThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT, "Transicion no permitida"))
                .when(pedidoValidaciones)
                .validarTransicion("PENDIENTE", "ENVIADO");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pedidoService.cambiarEstado(11, request, true));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(pedidoRepository, never()).save(pedido);
    }

    @Test
    void cambiarEstadoRechazaAlCliente() {
        ActualizarEstadoPedidoRequest request = new ActualizarEstadoPedidoRequest();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pedidoService.cambiarEstado(11, request, false));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(pedidoRepository, never()).findById(11);
    }

    @Test
    void eliminarPedidoSinDetallesNiPagosAsociados() {
        when(pedidoRepository.findById(11)).thenReturn(Optional.of(pedido));
        when(detallePedidoRepository.existsByPedido_IdPedido(11)).thenReturn(false);
        when(pagoRepository.existsByPedido_IdPedido(11)).thenReturn(false);

        String resultado = pedidoService.eliminarPedido(
                11, "cliente@isaflor.cl", false);

        assertEquals("Pedido eliminado correctamente", resultado);
        verify(pedidoRepository).delete(pedido);
    }

    @Test
    void eliminarPedidoConDetallesAsociadosDevuelveConflict() {
        when(pedidoRepository.findById(11)).thenReturn(Optional.of(pedido));
        when(detallePedidoRepository.existsByPedido_IdPedido(11)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pedidoService.eliminarPedido(
                        11, "cliente@isaflor.cl", false));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(pedidoRepository, never()).delete(any(Pedido.class));
    }

    @Test
    void eliminarPedidoConPagoAsociadoDevuelveConflict() {
        when(pedidoRepository.findById(11)).thenReturn(Optional.of(pedido));
        when(detallePedidoRepository.existsByPedido_IdPedido(11)).thenReturn(false);
        when(pagoRepository.existsByPedido_IdPedido(11)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pedidoService.eliminarPedido(
                        11, "cliente@isaflor.cl", false));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(pedidoRepository, never()).delete(any(Pedido.class));
    }

    private CrearPedidoRequest crearSolicitud() {
        CrearPedidoRequest request = new CrearPedidoRequest();
        request.setIdMetodoEnvio(2);
        request.setIdTipoDocumento(3);
        return request;
    }

    private Pedido crearPedido(Integer id, Usuario propietario) {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(id);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado("PENDIENTE");
        pedido.setTotal(BigDecimal.ZERO);
        pedido.setUsuario(propietario);
        pedido.setMetodoEnvio(metodoEnvio);
        pedido.setTipoDocumento(tipoDocumento);
        return pedido;
    }
}
