package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
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

import cl.isaflor.isaflor.dto.ActualizarDetallePedidoRequest;
import cl.isaflor.isaflor.dto.CrearDetallePedidoRequest;
import cl.isaflor.isaflor.dto.DetallePedidoDTO;
import cl.isaflor.isaflor.model.DetallePedido;
import cl.isaflor.isaflor.model.Pedido;
import cl.isaflor.isaflor.model.Producto;
import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.repository.DetallePedidoRepository;
import cl.isaflor.isaflor.repository.PagoRepository;
import cl.isaflor.isaflor.repository.PedidoRepository;
import cl.isaflor.isaflor.repository.ProductoRepository;
import cl.isaflor.isaflor.service.DetallePedidoService;
import cl.isaflor.isaflor.service.PedidoValidaciones;

@ExtendWith(MockitoExtension.class)
class DetallePedidoServiceTest {

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PedidoValidaciones pedidoValidaciones;

    @InjectMocks
    private DetallePedidoService detallePedidoService;

    private Pedido pedido;
    private Producto producto;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("cliente@isaflor.cl");

        pedido = new Pedido();
        pedido.setIdPedido(20);
        pedido.setEstado("PENDIENTE");
        pedido.setUsuario(usuario);

        producto = new Producto();
        producto.setIdProducto(5);
        producto.setNombreProducto("Ramo de rosas");
        producto.setPrecioProducto(new BigDecimal("12000.00"));
        producto.setEnOferta(false);
    }

    @Test
    void listarDetallesDeClienteFiltraPorCorreo() {
        DetallePedido detalle = crearDetalle(4, 2, new BigDecimal("12000.00"),
                new BigDecimal("24000.00"));
        when(detallePedidoRepository.findByPedido_Usuario_CorreoIgnoreCase("cliente@isaflor.cl"))
                .thenReturn(List.of(detalle));

        List<DetallePedidoDTO> resultado = detallePedidoService.listarDetalles(
                "cliente@isaflor.cl", false);

        assertEquals(1, resultado.size());
        assertEquals(20, resultado.get(0).getIdPedido());
        verify(detallePedidoRepository)
                .findByPedido_Usuario_CorreoIgnoreCase("cliente@isaflor.cl");
        verify(detallePedidoRepository, never()).findAll();
    }

    @Test
    void crearDetalleCalculaSubtotalYActualizaTotalSinCambiarStock() {
        CrearDetallePedidoRequest request = new CrearDetallePedidoRequest();
        request.setIdPedido(20);
        request.setIdProducto(5);
        request.setCantidad(3);

        AtomicReference<DetallePedido> guardado = new AtomicReference<>();
        when(pedidoRepository.findById(20)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(5)).thenReturn(Optional.of(producto));
        when(detallePedidoRepository.save(any(DetallePedido.class))).thenAnswer(invocation -> {
            DetallePedido detalle = invocation.getArgument(0);
            detalle.setIdDetalle(7);
            guardado.set(detalle);
            return detalle;
        });
        when(detallePedidoRepository.findByPedido_IdPedido(20))
                .thenAnswer(invocation -> List.of(guardado.get()));

        DetallePedidoDTO resultado = detallePedidoService.crearDetalle(
                request, "cliente@isaflor.cl", false);

        assertEquals(new BigDecimal("12000.00"), resultado.getPrecioUnitario());
        assertEquals(new BigDecimal("36000.00"), resultado.getSubTotal());
        assertEquals(new BigDecimal("36000.00"), pedido.getTotal());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void crearDetalleUsaPrecioOfertaYCalculaSubtotal() {
        producto.setEnOferta(true);
        producto.setPrecioOferta(new BigDecimal("9000.00"));
        CrearDetallePedidoRequest request = new CrearDetallePedidoRequest();
        request.setIdPedido(20);
        request.setIdProducto(5);
        request.setCantidad(3);

        AtomicReference<DetallePedido> guardado = new AtomicReference<>();
        when(pedidoRepository.findById(20)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(5)).thenReturn(Optional.of(producto));
        when(detallePedidoRepository.save(any(DetallePedido.class))).thenAnswer(invocation -> {
            DetallePedido detalle = invocation.getArgument(0);
            guardado.set(detalle);
            return detalle;
        });
        when(detallePedidoRepository.findByPedido_IdPedido(20))
                .thenAnswer(invocation -> List.of(guardado.get()));

        DetallePedidoDTO resultado = detallePedidoService.crearDetalle(
                request, "cliente@isaflor.cl", false);

        assertEquals(new BigDecimal("9000.00"), resultado.getPrecioUnitario());
        assertEquals(new BigDecimal("27000.00"), resultado.getSubTotal());
        assertEquals(new BigDecimal("27000.00"), pedido.getTotal());
    }

    @Test
    void crearDetallesNormalesYEnOfertaRecalculaTotalCombinado() {
        Producto productoEnOferta = new Producto();
        productoEnOferta.setIdProducto(6);
        productoEnOferta.setNombreProducto("Ramo de tulipanes");
        productoEnOferta.setPrecioProducto(new BigDecimal("15000.00"));
        productoEnOferta.setPrecioOferta(new BigDecimal("9000.00"));
        productoEnOferta.setEnOferta(true);

        List<DetallePedido> detallesGuardados = new ArrayList<>();
        when(pedidoRepository.findById(20)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(5)).thenReturn(Optional.of(producto));
        when(productoRepository.findById(6)).thenReturn(Optional.of(productoEnOferta));
        when(detallePedidoRepository.save(any(DetallePedido.class))).thenAnswer(invocation -> {
            DetallePedido detalle = invocation.getArgument(0);
            detallesGuardados.add(detalle);
            return detalle;
        });
        when(detallePedidoRepository.findByPedido_IdPedido(20))
                .thenAnswer(invocation -> new ArrayList<>(detallesGuardados));

        CrearDetallePedidoRequest requestNormal = new CrearDetallePedidoRequest();
        requestNormal.setIdPedido(20);
        requestNormal.setIdProducto(5);
        requestNormal.setCantidad(2);
        DetallePedidoDTO detalleNormal = detallePedidoService.crearDetalle(
                requestNormal, "cliente@isaflor.cl", false);

        CrearDetallePedidoRequest requestOferta = new CrearDetallePedidoRequest();
        requestOferta.setIdPedido(20);
        requestOferta.setIdProducto(6);
        requestOferta.setCantidad(3);
        DetallePedidoDTO detalleOferta = detallePedidoService.crearDetalle(
                requestOferta, "cliente@isaflor.cl", false);

        assertEquals(new BigDecimal("24000.00"), detalleNormal.getSubTotal());
        assertEquals(new BigDecimal("27000.00"), detalleOferta.getSubTotal());
        assertEquals(new BigDecimal("51000.00"), pedido.getTotal());
    }

    @Test
    void crearDetalleRechazaPrecioOfertaNuloCeroONegativo() {
        producto.setEnOferta(true);
        CrearDetallePedidoRequest request = new CrearDetallePedidoRequest();
        request.setIdPedido(20);
        request.setIdProducto(5);
        request.setCantidad(1);
        when(pedidoRepository.findById(20)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(5)).thenReturn(Optional.of(producto));

        for (BigDecimal precioOferta : new BigDecimal[] {
                null, BigDecimal.ZERO, new BigDecimal("-1.00") }) {
            producto.setPrecioOferta(precioOferta);
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> detallePedidoService.crearDetalle(
                            request, "cliente@isaflor.cl", false));
            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        }

        verify(detallePedidoRepository, never()).save(any(DetallePedido.class));
    }

    @Test
    void crearDetalleRechazaPrecioOfertaSuperiorAlPrecioNormal() {
        producto.setEnOferta(true);
        producto.setPrecioOferta(new BigDecimal("12000.01"));
        CrearDetallePedidoRequest request = new CrearDetallePedidoRequest();
        request.setIdPedido(20);
        request.setIdProducto(5);
        request.setCantidad(1);
        when(pedidoRepository.findById(20)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(5)).thenReturn(Optional.of(producto));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> detallePedidoService.crearDetalle(
                        request, "cliente@isaflor.cl", false));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(detallePedidoRepository, never()).save(any(DetallePedido.class));
    }

    @Test
    void crearDetalleRechazaPedidoDeOtroCliente() {
        CrearDetallePedidoRequest request = new CrearDetallePedidoRequest();
        request.setIdPedido(20);
        request.setIdProducto(5);
        request.setCantidad(1);
        when(pedidoRepository.findById(20)).thenReturn(Optional.of(pedido));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> detallePedidoService.crearDetalle(
                        request, "otro@isaflor.cl", false));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(productoRepository, never()).findById(5);
        verify(detallePedidoRepository, never()).save(any(DetallePedido.class));
    }

    @Test
    void crearDetalleRechazaCantidadNulaCeroONegativa() {
        for (Integer cantidad : new Integer[] {null, 0, -1}) {
            CrearDetallePedidoRequest request = new CrearDetallePedidoRequest();
            request.setIdPedido(20);
            request.setIdProducto(5);
            request.setCantidad(cantidad);

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> detallePedidoService.crearDetalle(
                            request, "cliente@isaflor.cl", false));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        }

        verify(pedidoRepository, never()).findById(20);
        verify(detallePedidoRepository, never()).save(any(DetallePedido.class));
    }

    @Test
    void crearDetalleRechazaPedidoNoPendienteInclusoParaAdministrador() {
        pedido.setEstado("CONFIRMADO");
        CrearDetallePedidoRequest request = new CrearDetallePedidoRequest();
        request.setIdPedido(20);
        request.setIdProducto(5);
        request.setCantidad(1);
        when(pedidoRepository.findById(20)).thenReturn(Optional.of(pedido));
        doThrow(new ResponseStatusException(
                HttpStatus.CONFLICT, "El pedido no permite modificar detalles"))
                .when(pedidoValidaciones)
                .validarModificacionDetalles("CONFIRMADO");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> detallePedidoService.crearDetalle(
                        request, "admin@isaflor.cl", true));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(productoRepository, never()).findById(5);
        verify(detallePedidoRepository, never()).save(any(DetallePedido.class));
    }

    @Test
    void crearDetalleRechazaPedidoConPagoPagado() {
        CrearDetallePedidoRequest request = new CrearDetallePedidoRequest();
        request.setIdPedido(20);
        request.setIdProducto(5);
        request.setCantidad(1);
        when(pedidoRepository.findById(20)).thenReturn(Optional.of(pedido));
        when(pagoRepository.existsByPedido_IdPedidoAndEstadoPago(20, "PAGADO"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> detallePedidoService.crearDetalle(
                        request, "cliente@isaflor.cl", false));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(productoRepository, never()).findById(5);
        verify(detallePedidoRepository, never()).save(any(DetallePedido.class));
    }

    @Test
    void actualizarCantidadRechazaCantidadNulaCeroONegativa() {
        for (Integer cantidad : new Integer[] {null, 0, -1}) {
            ActualizarDetallePedidoRequest request =
                    new ActualizarDetallePedidoRequest();
            request.setCantidad(cantidad);

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> detallePedidoService.actualizarCantidad(
                            7, request, "cliente@isaflor.cl", false));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        }

        verify(detallePedidoRepository, never()).findById(7);
        verify(detallePedidoRepository, never()).save(any(DetallePedido.class));
    }

    @Test
    void actualizarCantidadRechazaPedidoNoPendiente() {
        pedido.setEstado("EN_PREPARACION");
        DetallePedido detalle = crearDetalle(7, 2, new BigDecimal("10000.00"),
                new BigDecimal("20000.00"));
        ActualizarDetallePedidoRequest request =
                new ActualizarDetallePedidoRequest();
        request.setCantidad(4);
        when(detallePedidoRepository.findById(7)).thenReturn(Optional.of(detalle));
        doThrow(new ResponseStatusException(
                HttpStatus.CONFLICT, "El pedido no permite modificar detalles"))
                .when(pedidoValidaciones)
                .validarModificacionDetalles("EN_PREPARACION");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> detallePedidoService.actualizarCantidad(
                        7, request, "cliente@isaflor.cl", false));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(detallePedidoRepository, never()).save(detalle);
    }

    @Test
    void actualizarCantidadRechazaPedidoConPagoPagado() {
        DetallePedido detalle = crearDetalle(7, 2, new BigDecimal("10000.00"),
                new BigDecimal("20000.00"));
        ActualizarDetallePedidoRequest request = new ActualizarDetallePedidoRequest();
        request.setCantidad(4);
        when(detallePedidoRepository.findById(7)).thenReturn(Optional.of(detalle));
        when(pagoRepository.existsByPedido_IdPedidoAndEstadoPago(20, "PAGADO"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> detallePedidoService.actualizarCantidad(
                        7, request, "cliente@isaflor.cl", false));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(detallePedidoRepository, never()).save(detalle);
    }

    @Test
    void actualizarCantidadConservaPrecioHistoricoYRecalculaTotal() {
        DetallePedido detalle = crearDetalle(7, 2, new BigDecimal("10000.00"),
                new BigDecimal("20000.00"));
        producto.setPrecioProducto(new BigDecimal("5000.00"));
        ActualizarDetallePedidoRequest request = new ActualizarDetallePedidoRequest();
        request.setCantidad(4);
        when(detallePedidoRepository.findById(7)).thenReturn(Optional.of(detalle));
        when(detallePedidoRepository.save(detalle)).thenReturn(detalle);
        when(detallePedidoRepository.findByPedido_IdPedido(20)).thenReturn(List.of(detalle));

        DetallePedidoDTO resultado = detallePedidoService.actualizarCantidad(
                7, request, "cliente@isaflor.cl", false);

        assertEquals(new BigDecimal("10000.00"), resultado.getPrecioUnitario());
        assertEquals(new BigDecimal("40000.00"), resultado.getSubTotal());
        assertEquals(new BigDecimal("40000.00"), pedido.getTotal());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void eliminarDetalleRecalculaTotalDelPedido() {
        DetallePedido detalle = crearDetalle(7, 2, new BigDecimal("10000.00"),
                new BigDecimal("20000.00"));
        DetallePedido detalleRestante = crearDetalle(
                8, 3, new BigDecimal("4000.00"), new BigDecimal("12000.00"));
        when(detallePedidoRepository.findById(7)).thenReturn(Optional.of(detalle));
        when(detallePedidoRepository.findByPedido_IdPedido(20))
                .thenReturn(List.of(detalleRestante));

        String resultado = detallePedidoService.eliminarDetalle(
                7, "cliente@isaflor.cl", false);

        assertEquals("Detalle del pedido eliminado correctamente", resultado);
        assertEquals(new BigDecimal("12000.00"), pedido.getTotal());
        verify(detallePedidoRepository).delete(detalle);
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void eliminarDetalleRechazaPedidoNoPendiente() {
        pedido.setEstado("CANCELADO");
        DetallePedido detalle = crearDetalle(7, 2, new BigDecimal("10000.00"),
                new BigDecimal("20000.00"));
        when(detallePedidoRepository.findById(7)).thenReturn(Optional.of(detalle));
        doThrow(new ResponseStatusException(
                HttpStatus.CONFLICT, "El pedido no permite modificar detalles"))
                .when(pedidoValidaciones)
                .validarModificacionDetalles("CANCELADO");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> detallePedidoService.eliminarDetalle(
                        7, "cliente@isaflor.cl", false));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(detallePedidoRepository, never()).delete(detalle);
        verify(pedidoRepository, never()).save(pedido);
    }

    @Test
    void eliminarDetalleRechazaPedidoConPagoPagado() {
        DetallePedido detalle = crearDetalle(7, 2, new BigDecimal("10000.00"),
                new BigDecimal("20000.00"));
        when(detallePedidoRepository.findById(7)).thenReturn(Optional.of(detalle));
        when(pagoRepository.existsByPedido_IdPedidoAndEstadoPago(20, "PAGADO"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> detallePedidoService.eliminarDetalle(
                        7, "cliente@isaflor.cl", false));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(detallePedidoRepository, never()).delete(detalle);
        verify(pedidoRepository, never()).save(pedido);
    }

    @Test
    void buscarDetalleInexistenteDevuelveNotFound() {
        when(detallePedidoRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> detallePedidoService.buscarPorId(99, "cliente@isaflor.cl", false));

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("ID: 99"));
    }

    private DetallePedido crearDetalle(
            Integer id,
            Integer cantidad,
            BigDecimal precioUnitario,
            BigDecimal subTotal) {
        DetallePedido detalle = new DetallePedido();
        detalle.setIdDetalle(id);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precioUnitario);
        detalle.setSubTotal(subTotal);
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        return detalle;
    }
}
