package cl.isaflor.isaflor.service;

import org.springframework.stereotype.Service;

import cl.isaflor.isaflor.model.MetodoPago;

@Service
public class MetodoPagoValidaciones {

    public void validar(MetodoPago metodoPago) {
        if (metodoPago == null) {
            throw new IllegalArgumentException("El metodo de pago es obligatorio");
        }
        if (metodoPago.getNombre() == null || metodoPago.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del metodo de pago es obligatorio");
        }
        if (metodoPago.getNombre().trim().length() > 50) {
            throw new IllegalArgumentException(
                    "El nombre del metodo de pago no puede superar los 50 caracteres");
        }
        if (metodoPago.getDescripcion() != null
                && metodoPago.getDescripcion().length() > 150) {
            throw new IllegalArgumentException(
                    "La descripcion no puede superar los 150 caracteres");
        }
        if (metodoPago.getEstado() == null) {
            throw new IllegalArgumentException("El estado del metodo de pago es obligatorio");
        }
    }
}
