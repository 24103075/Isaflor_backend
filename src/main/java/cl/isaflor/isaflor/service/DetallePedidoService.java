package cl.isaflor.isaflor.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.ActualizarDetallePedidoRequest;
import cl.isaflor.isaflor.dto.CrearDetallePedidoRequest;
import cl.isaflor.isaflor.dto.DetallePedidoDTO;
import cl.isaflor.isaflor.model.DetallePedido;
import cl.isaflor.isaflor.model.Pedido;
import cl.isaflor.isaflor.model.Producto;
import cl.isaflor.isaflor.repository.DetallePedidoRepository;
import cl.isaflor.isaflor.repository.PedidoRepository;
import cl.isaflor.isaflor.repository.ProductoRepository;

@Service
@Transactional
public class DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public DetallePedidoService(
            DetallePedidoRepository detallePedidoRepository,
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository) {
        this.detallePedidoRepository = detallePedidoRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    public List<DetallePedidoDTO> listarDetalles(String correoUsuario, boolean administrador) {
        List<DetallePedido> detalles = administrador
                ? detallePedidoRepository.findAll()
                : detallePedidoRepository.findByPedido_Usuario_CorreoIgnoreCase(correoUsuario);

        return detalles.stream().map(this::convertirADTO).toList();
    }

    public DetallePedidoDTO buscarPorId(
            Integer id,
            String correoUsuario,
            boolean administrador) {
        DetallePedido detalle = buscarDetalleEntidad(id);
        validarAcceso(detalle.getPedido(), correoUsuario, administrador);
        return convertirADTO(detalle);
    }

    public DetallePedidoDTO crearDetalle(
            CrearDetallePedidoRequest request,
            String correoUsuario,
            boolean administrador) {
        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontro el pedido con ID: " + request.getIdPedido()));
        validarAcceso(pedido, correoUsuario, administrador);

        Producto producto = productoRepository.findById(request.getIdProducto())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontro el producto con ID: " + request.getIdProducto()));

        BigDecimal precioUnitario = producto.getPrecioProducto();
        DetallePedido detalle = new DetallePedido();
        detalle.setCantidad(request.getCantidad());
        detalle.setPrecioUnitario(precioUnitario);
        detalle.setSubTotal(precioUnitario.multiply(BigDecimal.valueOf(request.getCantidad())));
        detalle.setPedido(pedido);
        detalle.setProducto(producto);

        DetallePedido guardado = detallePedidoRepository.save(detalle);
        actualizarTotalPedido(pedido);
        return convertirADTO(guardado);
    }

    public DetallePedidoDTO actualizarCantidad(
            Integer id,
            ActualizarDetallePedidoRequest request,
            String correoUsuario,
            boolean administrador) {
        DetallePedido detalle = buscarDetalleEntidad(id);
        Pedido pedido = detalle.getPedido();
        validarAcceso(pedido, correoUsuario, administrador);

        detalle.setCantidad(request.getCantidad());
        detalle.setSubTotal(detalle.getPrecioUnitario()
                .multiply(BigDecimal.valueOf(request.getCantidad())));

        DetallePedido actualizado = detallePedidoRepository.save(detalle);
        actualizarTotalPedido(pedido);
        return convertirADTO(actualizado);
    }

    public String eliminarDetalle(Integer id, String correoUsuario, boolean administrador) {
        DetallePedido detalle = buscarDetalleEntidad(id);
        Pedido pedido = detalle.getPedido();
        validarAcceso(pedido, correoUsuario, administrador);

        detallePedidoRepository.delete(detalle);
        actualizarTotalPedido(pedido);
        return "Detalle del pedido eliminado correctamente";
    }

    private DetallePedido buscarDetalleEntidad(Integer id) {
        return detallePedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontro el detalle de pedido con ID: " + id));
    }

    private void validarAcceso(Pedido pedido, String correoUsuario, boolean administrador) {
        if (!administrador && (correoUsuario == null
                || pedido.getUsuario() == null
                || pedido.getUsuario().getCorreo() == null
                || !pedido.getUsuario().getCorreo().equalsIgnoreCase(correoUsuario))) {
            throw new AccessDeniedException("No tienes permiso para acceder a este pedido");
        }
    }

    private void actualizarTotalPedido(Pedido pedido) {
        BigDecimal total = detallePedidoRepository.findByPedido_IdPedido(pedido.getIdPedido())
                .stream()
                .map(DetallePedido::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setTotal(total);
        pedidoRepository.save(pedido);
    }

    private DetallePedidoDTO convertirADTO(DetallePedido detalle) {
        DetallePedidoDTO dto = new DetallePedidoDTO();
        dto.setIdDetalle(detalle.getIdDetalle());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubTotal(detalle.getSubTotal());
        dto.setIdPedido(detalle.getPedido().getIdPedido());
        dto.setIdProducto(detalle.getProducto().getIdProducto());
        dto.setNombreProducto(detalle.getProducto().getNombreProducto());
        return dto;
    }
}
