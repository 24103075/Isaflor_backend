package cl.isaflor.isaflor.service;

import org.springframework.stereotype.Service;

import cl.isaflor.isaflor.model.MetodoEnvio;

@Service
public class MetodoEnvioValidaciones {

    public void validar(MetodoEnvio metodoEnvio) {
        if (metodoEnvio == null) {
            throw new IllegalArgumentException("El metodo de envio es obligatorio");
        }
        if (metodoEnvio.getNombre() == null || metodoEnvio.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del metodo de envio es obligatorio");
        }
        if (metodoEnvio.getNombre().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre del metodo de envio no puede superar los 100 caracteres");
        }
        if (metodoEnvio.getDescripcion() != null
                && metodoEnvio.getDescripcion().length() > 255) {
            throw new IllegalArgumentException(
                    "La descripcion no puede superar los 255 caracteres");
        }
        if (metodoEnvio.getCosto() == null) {
            throw new IllegalArgumentException("El costo del envio es obligatorio");
        }
        if (metodoEnvio.getCosto().signum() < 0) {
            throw new IllegalArgumentException("El costo del envio no puede ser negativo");
        }
        if (metodoEnvio.getTiempoEntrega() == null
                || metodoEnvio.getTiempoEntrega().isBlank()) {
            throw new IllegalArgumentException("El tiempo de entrega es obligatorio");
        }
        if (metodoEnvio.getTiempoEntrega().trim().length() > 50) {
            throw new IllegalArgumentException(
                    "El tiempo de entrega no puede superar los 50 caracteres");
        }
    }
}
