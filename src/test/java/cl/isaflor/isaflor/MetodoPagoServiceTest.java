
package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.model.MetodoPago;
import cl.isaflor.isaflor.repository.MetodoPagoRepository;
import cl.isaflor.isaflor.repository.PagoRepository;
import cl.isaflor.isaflor.service.MetodoPagoService;
import cl.isaflor.isaflor.service.MetodoPagoValidaciones;

@ExtendWith(MockitoExtension.class)
class MetodoPagoServiceTest {

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private MetodoPagoValidaciones metodoPagoValidaciones;

    @InjectMocks
    private MetodoPagoService metodoPagoService;

    // PRUEBA 1: LISTAR METODOS DE PAGO
    @Test
    void listarMetodosPagoDistingueVisitantesYAdministradores() {

        List<MetodoPago> activos = List.of(crearMetodoPago(true));

        List<MetodoPago> todos = List.of(
                crearMetodoPago(true),
                crearMetodoPago(false));

        when(metodoPagoRepository.findByEstadoTrue()).thenReturn(activos);
        when(metodoPagoRepository.findAll()).thenReturn(todos);

        // Visitante: solamente metodos activos.
        List<MetodoPago> resultadoVisitante =
                metodoPagoService.listarMetodosPago(false);

        assertSame(activos, resultadoVisitante);

        // Administrador: todos los metodos.
        List<MetodoPago> resultadoAdministrador =
                metodoPagoService.listarMetodosPago(true);

        assertSame(todos, resultadoAdministrador);

        verify(metodoPagoRepository).findByEstadoTrue();
        verify(metodoPagoRepository).findAll();
    }

    // PRUEBA 2: BUSCAR METODO POR ID
    @Test
    void buscarPorIdControlaLaVisibilidadSegunElEstado() {

        MetodoPago activo = crearMetodoPago(true);
        MetodoPago inactivo = crearMetodoPago(false);

        when(metodoPagoRepository.findById(1))
                .thenReturn(Optional.of(activo));

        when(metodoPagoRepository.findById(2))
                .thenReturn(Optional.of(inactivo));

        // El visitante puede consultar un metodo activo.
        MetodoPago resultado = metodoPagoService.buscarPorId(1, false);

        assertSame(activo, resultado);

        // El visitante no puede consultar un metodo inactivo.
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> metodoPagoService.buscarPorId(2, false));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        // El administrador puede consultar el metodo inactivo.
        MetodoPago resultadoAdministrador =
                metodoPagoService.buscarPorId(2, true);

        assertSame(inactivo, resultadoAdministrador);
    }

    // PRUEBA 3: GUARDAR METODO DE PAGO
    @Test
    void guardarMetodoValidaNormalizaYPersiste() {

        MetodoPago metodo = crearMetodoPago(true);
        metodo.setNombre("  Transferencia  ");

        when(metodoPagoRepository.save(metodo))
                .thenReturn(metodo);

        MetodoPago resultado =
                metodoPagoService.guardarMetodoPago(metodo);

        assertSame(metodo, resultado);
        assertEquals("Transferencia", resultado.getNombre());

        verify(metodoPagoValidaciones).validar(metodo);
        verify(metodoPagoRepository).save(metodo);
    }

    // PRUEBA 4: ACTUALIZAR METODO DE PAGO
    @Test
    void actualizarMetodoModificaSusDatos() {

        MetodoPago existente = crearMetodoPago(false);
        MetodoPago datos = crearMetodoPago(true);

        datos.setNombre("  Tarjeta de credito  ");
        datos.setDescripcion("  Pago online  ");

        when(metodoPagoRepository.findById(1))
                .thenReturn(Optional.of(existente));

        when(metodoPagoRepository.save(existente))
                .thenReturn(existente);

        MetodoPago resultado =
                metodoPagoService.actualizarMetodoPago(1, datos);

        assertSame(existente, resultado);
        assertEquals("Tarjeta de credito", resultado.getNombre());
        assertEquals("Pago online", resultado.getDescripcion());
        assertEquals(true, resultado.getEstado());

        verify(metodoPagoValidaciones).validar(datos);
        verify(metodoPagoRepository).save(existente);
    }

    // PRUEBA 5: ELIMINAR METODO DE PAGO
    @Test
    void eliminarMetodoVerificaPagosAntesDeEliminar() {

        MetodoPago metodoSinPagos = crearMetodoPago(true);
        MetodoPago metodoConPagos = crearMetodoPago(true);

        when(metodoPagoRepository.findById(1))
                .thenReturn(Optional.of(metodoSinPagos));

        when(metodoPagoRepository.findById(2))
                .thenReturn(Optional.of(metodoConPagos));

        when(pagoRepository.existsByMetodoPago_IdMetodoPago(1))
                .thenReturn(false);

        when(pagoRepository.existsByMetodoPago_IdMetodoPago(2))
                .thenReturn(true);

        // Primer escenario: permite eliminar.
        String resultado = metodoPagoService.eliminarMetodoPago(1);

        assertEquals(
                "Metodo de pago eliminado correctamente",
                resultado);

        verify(metodoPagoRepository).delete(metodoSinPagos);

        // Segundo escenario: bloquea la eliminacion.
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> metodoPagoService.eliminarMetodoPago(2));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(metodoPagoRepository, times(1))
                .delete(any(MetodoPago.class));
    }

    // METODO AUXILIAR PARA CREAR METODOS DE PAGO
    private MetodoPago crearMetodoPago(boolean estado) {

        MetodoPago metodo = new MetodoPago();

        metodo.setIdMetodoPago(1);
        metodo.setNombre("Transferencia bancaria");
        metodo.setDescripcion("Transferencia electronica");
        metodo.setEstado(estado);

        return metodo;
    }
}
