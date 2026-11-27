package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.ActualizarEstadoPedidoRequest;
import cl.isaflor.isaflor.service.PedidoValidaciones;

class PedidoValidacionesTest {

    private final PedidoValidaciones pedidoValidaciones = new PedidoValidaciones();

    @ParameterizedTest
    @ValueSource(strings = {
            "PENDIENTE",
            "CONFIRMADO",
            "EN_PREPARACION",
            "ENVIADO",
            "ENTREGADO",
            "CANCELADO"
    })
    void validarEstadoNormalizaEstadosPermitidos(String estado) {
        ActualizarEstadoPedidoRequest request = new ActualizarEstadoPedidoRequest();
        request.setEstado("  " + estado.toLowerCase(java.util.Locale.ROOT) + "  ");

        assertEquals(estado, pedidoValidaciones.validarEstado(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DESPACHADO", "PAGADO", "BORRADOR"})
    void validarEstadoRechazaEstadosNoPermitidos(String estado) {
        ActualizarEstadoPedidoRequest request = new ActualizarEstadoPedidoRequest();
        request.setEstado(estado);

        assertThrows(IllegalArgumentException.class,
                () -> pedidoValidaciones.validarEstado(request));
    }

    @ParameterizedTest
    @CsvSource({
            "PENDIENTE, CONFIRMADO",
            "PENDIENTE, CANCELADO",
            "CONFIRMADO, EN_PREPARACION",
            "CONFIRMADO, CANCELADO",
            "EN_PREPARACION, ENVIADO",
            "ENVIADO, ENTREGADO"
    })
    void validarTransicionAceptaSoloFlujoDefinido(
            String estadoActual,
            String nuevoEstado) {
        pedidoValidaciones.validarTransicion(estadoActual, nuevoEstado);
    }

    @ParameterizedTest
    @CsvSource({
            "PENDIENTE, ENVIADO",
            "CONFIRMADO, PENDIENTE",
            "EN_PREPARACION, CANCELADO",
            "ENVIADO, CANCELADO",
            "ENTREGADO, CANCELADO",
            "CANCELADO, CONFIRMADO"
    })
    void validarTransicionRechazaCambiosNoPermitidos(
            String estadoActual,
            String nuevoEstado) {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pedidoValidaciones.validarTransicion(estadoActual, nuevoEstado));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CONFIRMADO",
            "EN_PREPARACION",
            "ENVIADO",
            "ENTREGADO",
            "CANCELADO"
    })
    void validarModificacionDetallesRechazaPedidosNoPendientes(String estado) {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pedidoValidaciones.validarModificacionDetalles(estado));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDIENTE", " pendiente "})
    void validarModificacionDetallesPermiteSoloPedidoPendiente(String estado) {
        pedidoValidaciones.validarModificacionDetalles(estado);
    }
}
