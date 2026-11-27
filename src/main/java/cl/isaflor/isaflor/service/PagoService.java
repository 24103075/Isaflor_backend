package cl.isaflor.isaflor.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PagoEstadoHistorialRepository pagoEstadoHistorialRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MetodoPagoService metodoPagoService;
    private final PagoValidaciones pagoValidaciones;

    public PagoService(
            PagoRepository pagoRepository,
            PagoEstadoHistorialRepository pagoEstadoHistorialRepository,
            PedidoRepository pedidoRepository,
            UsuarioRepository usuarioRepository,
            MetodoPagoService metodoPagoService,
            PagoValidaciones pagoValidaciones) {
        this.pagoRepository = pagoRepository;
        this.pagoEstadoHistorialRepository = pagoEstadoHistorialRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.metodoPagoService = metodoPagoService;
        this.pagoValidaciones = pagoValidaciones;
    }

    @Transactional(readOnly = true)
    public List<PagoDTO> listarPagos(String correoUsuario, boolean administrador) {
        List<Pago> pagos;
        if (administrador) {
            pagos = pagoRepository.findAllByOrderByFechaPagoDesc();
        } else {
            validarCorreoAutenticado(correoUsuario);
            pagos = pagoRepository
                    .findByPedido_Usuario_CorreoIgnoreCaseOrderByFechaPagoDesc(
                            correoUsuario);
        }
        return pagos.stream().map(this::convertirADTO).toList();
    }

    @Transactional(readOnly = true)
    public PagoDTO buscarPorId(
            Integer id,
            String correoUsuario,
            boolean administrador) {
        Pago pago = buscarPagoEntidad(id);
        validarAcceso(pago, correoUsuario, administrador);
        return convertirADTO(pago);
    }

    public PagoDTO crearPago(CrearPagoRequest request, String correoUsuario) {
        pagoValidaciones.validarCreacion(request);
        validarCorreoAutenticado(correoUsuario);

        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro el pedido con ID: " + request.getIdPedido()));
        validarAcceso(pedido, correoUsuario, false);

        if (pagoRepository.existsByPedido_IdPedido(pedido.getIdPedido())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El pedido ya tiene un pago registrado");
        }

        BigDecimal monto = pedido.getTotal();
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El pedido no tiene un total valido para pagar");
        }

        MetodoPago metodoPago = metodoPagoService.buscarPorId(
                request.getIdMetodoPago(), false);
        LocalDateTime fechaCreacion = LocalDateTime.now();

        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodoPago(metodoPago);
        pago.setMonto(monto);
        pago.setFechaPago(fechaCreacion);
        pago.setEstadoPago(PagoValidaciones.ESTADO_INICIAL);

        Pago guardado = pagoRepository.save(pago);
        guardarHistorial(
                guardado,
                null,
                PagoValidaciones.ESTADO_INICIAL,
                pedido.getUsuario(),
                fechaCreacion,
                null);

        return convertirADTO(guardado);
    }

    public PagoDTO cambiarEstado(
            Integer id,
            ActualizarEstadoPagoRequest request,
            String correoAdministrador,
            boolean administrador) {
        if (!administrador) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo el administrador puede cambiar el estado del pago");
        }

        String nuevoEstado = pagoValidaciones.validarEstado(request);
        Pago pago = buscarPagoEntidad(id);
        String estadoAnterior = pago.getEstadoPago();
        pagoValidaciones.validarTransicion(estadoAnterior, nuevoEstado);

        validarCorreoAutenticado(correoAdministrador);
        Usuario actor = usuarioRepository.findByCorreoIgnoreCase(correoAdministrador)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "El administrador autenticado no existe"));

        LocalDateTime fechaCambio = LocalDateTime.now();
        String motivo = pagoValidaciones.validarMotivo(request.getMotivo());
        pago.setEstadoPago(nuevoEstado);
        Pago actualizado = pagoRepository.save(pago);

        guardarHistorial(
                actualizado,
                estadoAnterior,
                nuevoEstado,
                actor,
                fechaCambio,
                motivo);
        return convertirADTO(actualizado);
    }

    @Transactional(readOnly = true)
    public List<PagoEstadoHistorialDTO> listarHistorial(
            Integer idPago,
            String correoUsuario,
            boolean administrador) {
        Pago pago = buscarPagoEntidad(idPago);
        validarAcceso(pago, correoUsuario, administrador);
        return pagoEstadoHistorialRepository
                .findByPago_IdPagoOrderByFechaCambioAsc(idPago)
                .stream()
                .map(this::convertirHistorialADTO)
                .toList();
    }

    private void guardarHistorial(
            Pago pago,
            String estadoAnterior,
            String estadoNuevo,
            Usuario actor,
            LocalDateTime fechaCambio,
            String motivo) {
        PagoEstadoHistorial historial = new PagoEstadoHistorial();
        historial.setPago(pago);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(estadoNuevo);
        historial.setUsuario(actor);
        historial.setFechaCambio(fechaCambio);
        historial.setMotivo(motivo);
        pagoEstadoHistorialRepository.save(historial);
    }

    private Pago buscarPagoEntidad(Integer id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del pago debe ser valido");
        }
        return pagoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro el pago con ID: " + id));
    }

    private void validarAcceso(
            Pago pago,
            String correoUsuario,
            boolean administrador) {
        validarAcceso(pago.getPedido(), correoUsuario, administrador);
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
                    "No tienes permiso para acceder a este pago");
        }
    }

    private void validarCorreoAutenticado(String correoUsuario) {
        if (correoUsuario == null || correoUsuario.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El usuario autenticado no es valido");
        }
    }

    private PagoDTO convertirADTO(Pago pago) {
        PagoDTO dto = new PagoDTO();
        dto.setIdPago(pago.getIdPago());
        dto.setFechaPago(pago.getFechaPago());
        dto.setMonto(pago.getMonto());
        dto.setEstadoPago(pago.getEstadoPago());
        dto.setIdPedido(pago.getPedido().getIdPedido());
        dto.setIdMetodoPago(pago.getMetodoPago().getIdMetodoPago());
        return dto;
    }

    private PagoEstadoHistorialDTO convertirHistorialADTO(
            PagoEstadoHistorial historial) {
        PagoEstadoHistorialDTO dto = new PagoEstadoHistorialDTO();
        dto.setIdHistorialPago(historial.getIdHistorialPago());
        dto.setEstadoAnterior(historial.getEstadoAnterior());
        dto.setEstadoNuevo(historial.getEstadoNuevo());
        dto.setCorreoActor(historial.getUsuario().getCorreo());
        dto.setFechaCambio(historial.getFechaCambio());
        dto.setMotivo(historial.getMotivo());
        return dto;
    }
}
