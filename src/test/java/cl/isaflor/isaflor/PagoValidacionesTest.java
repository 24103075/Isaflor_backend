package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.ActualizarEstadoPagoRequest;
import cl.isaflor.isaflor.dto.CrearPagoRequest;
import cl.isaflor.isaflor.service.PagoValidaciones;

class PagoValidacionesTest {

    private final PagoValidaciones pagoValidaciones = new PagoValidaciones();

    @Test
    void validarCreacionRechazaIdsInvalidos() {
        CrearPagoRequest request = new CrearPagoRequest();
        request.setIdPedido(0);
        request.setIdMetodoPago(2);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pagoValidaciones.validarCreacion(request));

        assertEquals("El pedido debe ser valido", exception.getMessage());
    }

    @Test
    void validarEstadoNormalizaYRechazaEstadosInventados() {
        ActualizarEstadoPagoRequest request = new ActualizarEstadoPagoRequest();
        request.setEstadoPago(" pagado ");
        assertEquals("PAGADO", pagoValidaciones.validarEstado(request));

        request.setEstadoPago("EN_PROCESO");
        assertThrows(
                IllegalArgumentException.class,
                () -> pagoValidaciones.validarEstado(request));

        request.setEstadoPago("ANULADO");
        request.setMotivo("  ");
        assertThrows(
                IllegalArgumentException.class,
                () -> pagoValidaciones.validarEstado(request));
    }

    @Test
    void validarTransicionPermiteFlujoDefinidoYBloqueaEstadosFinales() {
        pagoValidaciones.validarTransicion("RECHAZADO", "PENDIENTE");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pagoValidaciones.validarTransicion("PAGADO", "PENDIENTE"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        ResponseStatusException anulacionFinal = assertThrows(
                ResponseStatusException.class,
                () -> pagoValidaciones.validarTransicion("ANULADO", "PENDIENTE"));
        assertEquals(HttpStatus.CONFLICT, anulacionFinal.getStatusCode());
    }

    @Test
    void validarMotivoNormalizaYLimitaLongitud() {
        assertEquals("Solicitud del cliente",
                pagoValidaciones.validarMotivo("  Solicitud del cliente  "));

        String motivoExtenso = "x".repeat(256);
        assertThrows(
                IllegalArgumentException.class,
                () -> pagoValidaciones.validarMotivo(motivoExtenso));
    }
}
