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
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.ActualizarEstadoPagoRequest;
import cl.isaflor.isaflor.dto.CrearPagoRequest;
import cl.isaflor.isaflor.dto.PagoDTO;
import cl.isaflor.isaflor.dto.PagoEstadoHistorialDTO;
import cl.isaflor.isaflor.model.MetodoPago;
import cl.isaflor.isaflor.model.Pago;
import cl.isaflor.isaflor.model.PagoEstadoHistorial;
import cl.isaflor.isaflor.model.Pedido;
import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.repository.PagoEstadoHistorialRepository;
import cl.isaflor.isaflor.repository.PagoRepository;
import cl.isaflor.isaflor.repository.PedidoRepository;
import cl.isaflor.isaflor.repository.UsuarioRepository;
import cl.isaflor.isaflor.service.MetodoPagoService;
import cl.isaflor.isaflor.service.PagoService;
import cl.isaflor.isaflor.service.PagoValidaciones;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PagoEstadoHistorialRepository pagoEstadoHistorialRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MetodoPagoService metodoPagoService;

    @Mock
    private PagoValidaciones pagoValidaciones;

    @InjectMocks
    private PagoService pagoService;

    private Usuario cliente;
    private Usuario administrador;
    private Pedido pedido;
    private MetodoPago metodoPago;

    @BeforeEach
    void setUp() {
        cliente = crearUsuario("cliente@isaflor.cl");
        administrador = crearUsuario("admin@isaflor.cl");

        pedido = new Pedido();
        pedido.setIdPedido(20);
        pedido.setEstado("PENDIENTE");
        pedido.setTotal(new BigDecimal("45000.00"));
        pedido.setUsuario(cliente);

        metodoPago = new MetodoPago();
        metodoPago.setIdMetodoPago(3);
        metodoPago.setNombre("Transferencia");
        metodoPago.setEstado(true);
    }

    @Test
    void listarPagosEntregaSoloLosDelClienteYTodosAlAdministrador() {
        Pago pago = crearPago(1, pedido);
        when(pagoRepository.findByPedido_Usuario_CorreoIgnoreCaseOrderByFechaPagoDesc(
                "cliente@isaflor.cl")).thenReturn(List.of(pago));
        when(pagoRepository.findAllByOrderByFechaPagoDesc()).thenReturn(List.of(pago));

        List<PagoDTO> pagosCliente = pagoService.listarPagos(
                "cliente@isaflor.cl", false);
        List<PagoDTO> pagosAdministrador = pagoService.listarPagos(
                "admin@isaflor.cl", true);

        assertEquals(1, pagosCliente.size());
        assertEquals(20, pagosCliente.get(0).getIdPedido());
        assertEquals(1, pagosAdministrador.size());
        verify(pagoRepository)
                .findByPedido_Usuario_CorreoIgnoreCaseOrderByFechaPagoDesc(
                        "cliente@isaflor.cl");
        verify(pagoRepository).findAllByOrderByFechaPagoDesc();
    }

    @Test
    void buscarPorIdValidaPropiedadDelPago() {
        Pago pago = crearPago(1, pedido);
        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago));

        PagoDTO resultado = pagoService.buscarPorId(
                1, "cliente@isaflor.cl", false);

        assertEquals(1, resultado.getIdPago());
        assertEquals("PENDIENTE", resultado.getEstadoPago());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pagoService.buscarPorId(1, "otro@isaflor.cl", false));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void crearPagoDerivaDatosDelPedidoYRegistraEstadoInicial() {
        CrearPagoRequest request = new CrearPagoRequest();
        request.setIdPedido(20);
        request.setIdMetodoPago(3);
        AtomicReference<Pago> pagoGuardado = new AtomicReference<>();
        AtomicReference<PagoEstadoHistorial> historialGuardado =
                new AtomicReference<>();
        when(pedidoRepository.findById(20)).thenReturn(Optional.of(pedido));
        when(pagoRepository.existsByPedido_IdPedido(20)).thenReturn(false);
        when(metodoPagoService.buscarPorId(3, false)).thenReturn(metodoPago);
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> {
            Pago pago = invocation.getArgument(0);
            pago.setIdPago(9);
            pagoGuardado.set(pago);
            return pago;
        });
        when(pagoEstadoHistorialRepository.save(any(PagoEstadoHistorial.class)))
                .thenAnswer(invocation -> {
                    PagoEstadoHistorial historial = invocation.getArgument(0);
                    historialGuardado.set(historial);
                    return historial;
                });

        PagoDTO resultado = pagoService.crearPago(request, "cliente@isaflor.cl");

        assertEquals(new BigDecimal("45000.00"), resultado.getMonto());
        assertEquals("PENDIENTE", resultado.getEstadoPago());
        assertNotNull(resultado.getFechaPago());
        assertEquals(pedido, pagoGuardado.get().getPedido());
        assertEquals(metodoPago, pagoGuardado.get().getMetodoPago());
        assertEquals(cliente, historialGuardado.get().getUsuario());
        assertEquals(null, historialGuardado.get().getEstadoAnterior());
        assertEquals("PENDIENTE", historialGuardado.get().getEstadoNuevo());
        verify(pagoValidaciones).validarCreacion(request);
    }

    @Test
    void crearPagoRechazaPedidoQueYaTienePago() {
        CrearPagoRequest request = new CrearPagoRequest();
        request.setIdPedido(20);
        request.setIdMetodoPago(3);
        when(pedidoRepository.findById(20)).thenReturn(Optional.of(pedido));
        when(pagoRepository.existsByPedido_IdPedido(20)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pagoService.crearPago(request, "cliente@isaflor.cl"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(pagoRepository, never()).save(any(Pago.class));
        verify(metodoPagoService, never()).buscarPorId(3, false);
    }

    @Test
    void cambiarEstadoRegistraActorYTransicion() {
        Pago pago = crearPago(1, pedido);
        ActualizarEstadoPagoRequest request = new ActualizarEstadoPagoRequest();
        request.setEstadoPago("pagado");
        when(pagoValidaciones.validarEstado(request)).thenReturn("PAGADO");
        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago));
        when(usuarioRepository.findByCorreoIgnoreCase("admin@isaflor.cl"))
                .thenReturn(Optional.of(administrador));
        when(pagoRepository.save(pago)).thenReturn(pago);

        PagoDTO resultado = pagoService.cambiarEstado(
                1, request, "admin@isaflor.cl", true);

        assertEquals("PAGADO", resultado.getEstadoPago());
        assertEquals("PENDIENTE", pedido.getEstado());
        verify(pagoValidaciones).validarTransicion("PENDIENTE", "PAGADO");
        verify(pagoEstadoHistorialRepository).save(any(PagoEstadoHistorial.class));
    }

    @Test
    void cambiarEstadoRechazaAClientes() {
        ActualizarEstadoPagoRequest request = new ActualizarEstadoPagoRequest();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pagoService.cambiarEstado(
                        1, request, "cliente@isaflor.cl", false));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(pagoRepository, never()).findById(1);
        verify(pagoValidaciones, never()).validarEstado(request);
    }

    @Test
    void listarHistorialValidaPropiedadYConvierteLosDatos() {
        Pago pago = crearPago(1, pedido);
        PagoEstadoHistorial historial = new PagoEstadoHistorial();
        historial.setIdHistorialPago(12);
        historial.setPago(pago);
        historial.setEstadoAnterior(null);
        historial.setEstadoNuevo("PENDIENTE");
        historial.setUsuario(cliente);
        historial.setFechaCambio(LocalDateTime.now());
        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago));
        when(pagoEstadoHistorialRepository.findByPago_IdPagoOrderByFechaCambioAsc(1))
                .thenReturn(List.of(historial));

        List<PagoEstadoHistorialDTO> resultado = pagoService.listarHistorial(
                1, "cliente@isaflor.cl", false);

        assertEquals(1, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstadoNuevo());
        assertEquals("cliente@isaflor.cl", resultado.get(0).getCorreoActor());
    }

    private Usuario crearUsuario(String correo) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        return usuario;
    }

    private Pago crearPago(Integer id, Pedido pedidoAsociado) {
        Pago pago = new Pago();
        pago.setIdPago(id);
        pago.setFechaPago(LocalDateTime.now());
        pago.setMonto(new BigDecimal("45000.00"));
        pago.setEstadoPago("PENDIENTE");
        pago.setPedido(pedidoAsociado);
        pago.setMetodoPago(metodoPago);
        return pago;
    }
}
