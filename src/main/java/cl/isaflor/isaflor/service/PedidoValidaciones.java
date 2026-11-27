package cl.isaflor.isaflor.service;

import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.ActualizarEstadoPedidoRequest;
import cl.isaflor.isaflor.dto.CrearPedidoRequest;

@Service
public class PedidoValidaciones {

    public static final String ESTADO_INICIAL = "PENDIENTE";

    private static final Set<String> ESTADOS_PERMITIDOS = Set.of(
            "PENDIENTE",
            "CONFIRMADO",
            "EN_PREPARACION",
            "ENVIADO",
            "ENTREGADO",
            "CANCELADO");

    private static final Map<String, Set<String>> TRANSICIONES_PERMITIDAS = Map.of(
            "PENDIENTE", Set.of("CONFIRMADO", "CANCELADO"),
            "CONFIRMADO", Set.of("EN_PREPARACION", "CANCELADO"),
            "EN_PREPARACION", Set.of("ENVIADO"),
            "ENVIADO", Set.of("ENTREGADO"));

    public void validarCreacion(CrearPedidoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos del pedido son obligatorios");
        }

        if (request.getIdMetodoEnvio() == null || request.getIdMetodoEnvio() <= 0) {
            throw new IllegalArgumentException("El metodo de envio debe ser valido");
        }

        if (request.getIdTipoDocumento() == null || request.getIdTipoDocumento() <= 0) {
            throw new IllegalArgumentException("El tipo de documento debe ser valido");
        }
    }

    public String validarEstado(ActualizarEstadoPedidoRequest request) {
        if (request == null || request.getEstado() == null
                || request.getEstado().isBlank()) {
            throw new IllegalArgumentException("El estado del pedido es obligatorio");
        }

        String estado = request.getEstado().trim().toUpperCase(java.util.Locale.ROOT);
        if (estado.length() > 30) {
            throw new IllegalArgumentException(
                    "El estado del pedido no puede superar los 30 caracteres");
        }
        if (!ESTADOS_PERMITIDOS.contains(estado)) {
            throw new IllegalArgumentException("El estado del pedido no esta permitido");
        }
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

    public void validarModificacionDetalles(String estadoPedido) {
        String estadoNormalizado = normalizarEstadoActual(estadoPedido);
        if (!ESTADO_INICIAL.equals(estadoNormalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden modificar detalles de pedidos pendientes");
        }
    }

    private String normalizarEstadoActual(String estado) {
        String normalizado = estado == null
                ? ""
                : estado.trim().toUpperCase(java.util.Locale.ROOT);
        if (!ESTADOS_PERMITIDOS.contains(normalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El pedido tiene un estado actual no valido");
        }
        return normalizado;
    }
}
