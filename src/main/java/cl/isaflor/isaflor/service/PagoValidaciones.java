package cl.isaflor.isaflor.service;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.ActualizarEstadoPagoRequest;
import cl.isaflor.isaflor.dto.CrearPagoRequest;

@Service
public class PagoValidaciones {

    public static final String ESTADO_INICIAL = "PENDIENTE";

    private static final Set<String> ESTADOS_PERMITIDOS = Set.of(
            "PENDIENTE",
            "PAGADO",
            "RECHAZADO",
            "ANULADO");

    private static final Map<String, Set<String>> TRANSICIONES_PERMITIDAS = Map.of(
            "PENDIENTE", Set.of("PAGADO", "RECHAZADO", "ANULADO"),
            "RECHAZADO", Set.of("PENDIENTE"));

    public void validarCreacion(CrearPagoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos del pago son obligatorios");
        }
        if (request.getIdPedido() == null || request.getIdPedido() <= 0) {
            throw new IllegalArgumentException("El pedido debe ser valido");
        }
        if (request.getIdMetodoPago() == null || request.getIdMetodoPago() <= 0) {
            throw new IllegalArgumentException("El metodo de pago debe ser valido");
        }
    }

    public String validarEstado(ActualizarEstadoPagoRequest request) {
        if (request == null || request.getEstadoPago() == null
                || request.getEstadoPago().isBlank()) {
            throw new IllegalArgumentException("El estado del pago es obligatorio");
        }

        String estado = request.getEstadoPago().trim().toUpperCase(Locale.ROOT);
        if (estado.length() > 30) {
            throw new IllegalArgumentException(
                    "El estado del pago no puede superar los 30 caracteres");
        }
        if (!ESTADOS_PERMITIDOS.contains(estado)) {
            throw new IllegalArgumentException("El estado del pago no esta permitido");
        }
        if ("ANULADO".equals(estado)
                && (request.getMotivo() == null || request.getMotivo().isBlank())) {
            throw new IllegalArgumentException(
                    "El motivo es obligatorio para anular un pago");
        }
        validarMotivo(request.getMotivo());
        return estado;
    }

    public void validarTransicion(String estadoActual, String nuevoEstado) {
        String estadoNormalizado = normalizarEstadoActual(estadoActual);
        if (!TRANSICIONES_PERMITIDAS
                .getOrDefault(estadoNormalizado, Set.of())
                .contains(nuevoEstado)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La transicion de " + estadoNormalizado + " a "
                            + nuevoEstado + " no esta permitida");
        }
    }

    public String validarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            return null;
        }
        String normalizado = motivo.trim();
        if (normalizado.length() > 255) {
            throw new IllegalArgumentException(
                    "El motivo no puede superar los 255 caracteres");
        }
        return normalizado;
    }

    private String normalizarEstadoActual(String estado) {
        String normalizado = estado == null
                ? ""
                : estado.trim().toUpperCase(Locale.ROOT);
        if (!ESTADOS_PERMITIDOS.contains(normalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El pago tiene un estado actual no valido");
        }
        return normalizado;
    }
}
