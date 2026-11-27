package cl.isaflor.isaflor.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.ActualizarEstadoPedidoRequest;
import cl.isaflor.isaflor.dto.CrearPedidoRequest;
import cl.isaflor.isaflor.dto.PedidoDTO;
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

@Service
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MetodoEnvioRepository metodoEnvioRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final PagoRepository pagoRepository;
    private final PedidoValidaciones pedidoValidaciones;

    public PedidoService(
            PedidoRepository pedidoRepository,
            UsuarioRepository usuarioRepository,
            MetodoEnvioRepository metodoEnvioRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            DetallePedidoRepository detallePedidoRepository,
            PagoRepository pagoRepository,
            PedidoValidaciones pedidoValidaciones) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.metodoEnvioRepository = metodoEnvioRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.pagoRepository = pagoRepository;
        this.pedidoValidaciones = pedidoValidaciones;
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPedidos(String correoUsuario, boolean administrador) {
        if (administrador) {
            return pedidoRepository.findAllByOrderByFechaPedidoDesc()
                    .stream()
                    .map(this::convertirADTO)
                    .toList();
        }

        validarCorreoAutenticado(correoUsuario);
        return pedidoRepository
                .findByUsuario_CorreoIgnoreCaseOrderByFechaPedidoDesc(correoUsuario)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoDTO buscarPorId(
            Integer id,
            String correoUsuario,
            boolean administrador) {
        Pedido pedido = buscarPedidoEntidad(id);
        validarAcceso(pedido, correoUsuario, administrador);
        return convertirADTO(pedido);
    }

    public PedidoDTO crearPedido(CrearPedidoRequest request, String correoUsuario) {
        pedidoValidaciones.validarCreacion(request);
        validarCorreoAutenticado(correoUsuario);

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correoUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "El usuario autenticado no existe"));

        MetodoEnvio metodoEnvio = metodoEnvioRepository.findById(
                        request.getIdMetodoEnvio())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro el metodo de envio con ID: "
                                + request.getIdMetodoEnvio()));

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(
                        request.getIdTipoDocumento())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro el tipo de documento con ID: "
                                + request.getIdTipoDocumento()));

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setMetodoEnvio(metodoEnvio);
        pedido.setTipoDocumento(tipoDocumento);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado(PedidoValidaciones.ESTADO_INICIAL);
        pedido.setTotal(BigDecimal.ZERO);

        return convertirADTO(pedidoRepository.save(pedido));
    }

    public PedidoDTO cambiarEstado(
            Integer id,
            ActualizarEstadoPedidoRequest request,
            boolean administrador) {
        if (!administrador) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo el administrador puede cambiar el estado del pedido");
        }

        String estado = pedidoValidaciones.validarEstado(request);
        Pedido pedido = buscarPedidoEntidad(id);
        pedidoValidaciones.validarTransicion(pedido.getEstado(), estado);
        pedido.setEstado(estado);
        return convertirADTO(pedidoRepository.save(pedido));
    }

    public String eliminarPedido(
            Integer id,
            String correoUsuario,
            boolean administrador) {
        Pedido pedido = buscarPedidoEntidad(id);
        validarAcceso(pedido, correoUsuario, administrador);

        if (detallePedidoRepository.existsByPedido_IdPedido(id)
                || pagoRepository.existsByPedido_IdPedido(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el pedido porque tiene detalles o pagos asociados");
        }

        pedidoRepository.delete(pedido);
        return "Pedido eliminado correctamente";
    }

    private Pedido buscarPedidoEntidad(Integer id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del pedido debe ser valido");
        }

        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro el pedido con ID: " + id));
    }

    private void validarAcceso(
            Pedido pedido,
            String correoUsuario,
            boolean administrador) {
        if (!administrador && (correoUsuario == null
                || pedido.getUsuario() == null
                || pedido.getUsuario().getCorreo() == null
                || !pedido.getUsuario().getCorreo().equalsIgnoreCase(correoUsuario))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para acceder a este pedido");
        }
    }

    private void validarCorreoAutenticado(String correoUsuario) {
        if (correoUsuario == null || correoUsuario.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El usuario autenticado no es valido");
        }
    }

    private PedidoDTO convertirADTO(Pedido pedido) {
        PedidoDTO dto = new PedidoDTO();
        dto.setIdPedido(pedido.getIdPedido());
        dto.setFechaPedido(pedido.getFechaPedido());
        dto.setEstado(pedido.getEstado());
        dto.setTotal(pedido.getTotal());
        dto.setIdUsuario(pedido.getUsuario().getIdUsuario());
        dto.setIdMetodoEnvio(pedido.getMetodoEnvio().getIdMetodoEnvio());
        dto.setIdTipoDocumento(pedido.getTipoDocumento().getIdTipoDocumento());
        return dto;
    }
}
