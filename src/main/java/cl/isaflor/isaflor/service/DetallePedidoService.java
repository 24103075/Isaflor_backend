package cl.isaflor.isaflor.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
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
import cl.isaflor.isaflor.repository.PagoRepository;
import cl.isaflor.isaflor.repository.PedidoRepository;
import cl.isaflor.isaflor.repository.ProductoRepository;

@Service
@Transactional
public class DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final PagoRepository pagoRepository;
    private final PedidoValidaciones pedidoValidaciones;

    public DetallePedidoService(
            DetallePedidoRepository detallePedidoRepository,
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            PagoRepository pagoRepository,
            PedidoValidaciones pedidoValidaciones) {
        this.detallePedidoRepository = detallePedidoRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.pagoRepository = pagoRepository;
        this.pedidoValidaciones = pedidoValidaciones;
    }

    public List<DetallePedidoDTO> listarDetalles(String correoUsuario, boolean administrador) {
        if (!administrador && (correoUsuario == null || correoUsuario.isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El correo del usuario autenticado no es valido");
        }

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
        validarSolicitudCreacion(request);

        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontro el pedido con ID: " + request.getIdPedido()));
        validarAcceso(pedido, correoUsuario, administrador);
        validarPedidoPermiteModificarDetalles(pedido);

        Producto producto = productoRepository.findById(request.getIdProducto())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontro el producto con ID: " + request.getIdProducto()));

        BigDecimal precioUnitario = determinarPrecioUnitario(producto);
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
        validarSolicitudActualizacion(request);

        DetallePedido detalle = buscarDetalleEntidad(id);
        Pedido pedido = detalle.getPedido();
        validarAcceso(pedido, correoUsuario, administrador);
        validarPedidoPermiteModificarDetalles(pedido);

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
        validarPedidoPermiteModificarDetalles(pedido);

        detallePedidoRepository.delete(detalle);
        actualizarTotalPedido(pedido);
        return "Detalle del pedido eliminado correctamente";
    }

    private BigDecimal determinarPrecioUnitario(Producto producto) {
        BigDecimal precioNormal = producto.getPrecioProducto();
        if (precioNormal == null || precioNormal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El producto no tiene un precio normal valido");
        }

        Boolean enOferta = producto.getEnOferta();
        if (enOferta == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El producto no tiene un estado de oferta valido");
        }
        if (!enOferta) {
            return precioNormal;
        }

        BigDecimal precioOferta = producto.getPrecioOferta();
        if (precioOferta == null || precioOferta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El producto en oferta debe tener un precio de oferta mayor a cero");
        }
        if (precioOferta.compareTo(precioNormal) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El precio de oferta no puede superar el precio normal");
        }

        return precioOferta;
    }

    private DetallePedido buscarDetalleEntidad(Integer id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del detalle de pedido debe ser positivo");
        }

        return detallePedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontro el detalle de pedido con ID: " + id));
    }

    private void validarSolicitudCreacion(CrearDetallePedidoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los datos del detalle son obligatorios");
        }
        if (request.getIdPedido() == null || request.getIdPedido() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del pedido debe ser positivo");
        }
        if (request.getIdProducto() == null || request.getIdProducto() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del producto debe ser positivo");
        }
        validarCantidad(request.getCantidad());
    }

    private void validarSolicitudActualizacion(
            ActualizarDetallePedidoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los datos de actualizacion son obligatorios");
        }
        validarCantidad(request.getCantidad());
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad debe ser un numero positivo");
        }
    }

    private void validarPedidoPermiteModificarDetalles(Pedido pedido) {
        pedidoValidaciones.validarModificacionDetalles(pedido.getEstado());
        if (pagoRepository.existsByPedido_IdPedidoAndEstadoPago(
                pedido.getIdPedido(), "PAGADO")) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pueden modificar los detalles de un pedido pagado");
        }
    }

    private void validarAcceso(Pedido pedido, String correoUsuario, boolean administrador) {
        if (!administrador && (correoUsuario == null
                || pedido.getUsuario() == null
                || pedido.getUsuario().getCorreo() == null
                || !pedido.getUsuario().getCorreo().equalsIgnoreCase(correoUsuario))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para acceder a este pedido");
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
