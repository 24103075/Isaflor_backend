
package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.model.MetodoEnvio;
import cl.isaflor.isaflor.repository.MetodoEnvioRepository;
import cl.isaflor.isaflor.repository.PedidoRepository;
import cl.isaflor.isaflor.service.MetodoEnvioService;
import cl.isaflor.isaflor.service.MetodoEnvioValidaciones;

@ExtendWith(MockitoExtension.class)
class MetodoEnvioServiceTest {

    @Mock
    private MetodoEnvioRepository metodoEnvioRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private MetodoEnvioValidaciones metodoEnvioValidaciones;

    @InjectMocks
    private MetodoEnvioService metodoEnvioService;

    // PRUEBA 1: LISTAR METODOS DE ENVIO
    @Test
    void listarMetodosEnvioDevuelveLosRegistrados() {

        List<MetodoEnvio> metodos = List.of(crearMetodoEnvio());

        when(metodoEnvioRepository.findAll()).thenReturn(metodos);

        List<MetodoEnvio> resultado =
                metodoEnvioService.listarMetodosEnvio();

        assertSame(metodos, resultado);

        verify(metodoEnvioRepository).findAll();
    }

    // PRUEBA 2: BUSCAR METODO POR ID
    @Test
    void buscarPorIdDevuelveMetodoExistente() {

        MetodoEnvio metodo = crearMetodoEnvio();

        when(metodoEnvioRepository.findById(1))
                .thenReturn(Optional.of(metodo));

        MetodoEnvio resultado = metodoEnvioService.buscarPorId(1);

        assertSame(metodo, resultado);

        verify(metodoEnvioRepository).findById(1);
    }

    // PRUEBA 3: GUARDAR METODO DE ENVIO
    @Test
    void guardarMetodoValidaNormalizaYPersiste() {

        MetodoEnvio metodo = crearMetodoEnvio();

        metodo.setNombre("  Despacho express  ");
        metodo.setTiempoEntrega("  24 horas  ");

        when(metodoEnvioRepository.save(metodo))
                .thenReturn(metodo);

        MetodoEnvio resultado =
                metodoEnvioService.guardarMetodoEnvio(metodo);

        assertSame(metodo, resultado);
        assertEquals("Despacho express", resultado.getNombre());
        assertEquals("24 horas", resultado.getTiempoEntrega());

        verify(metodoEnvioValidaciones).validar(metodo);
        verify(metodoEnvioRepository).save(metodo);
    }

    // PRUEBA 4: ACTUALIZAR METODO DE ENVIO
    @Test
    void actualizarMetodoModificaDatosDelMetodoExistente() {

        MetodoEnvio existente = crearMetodoEnvio();
        MetodoEnvio datos = crearMetodoEnvio();

        datos.setNombre("Retiro en tienda");
        datos.setDescripcion("Retiro en local");
        datos.setCosto(new BigDecimal("0.00"));
        datos.setTiempoEntrega("Inmediato");

        when(metodoEnvioRepository.findById(1))
                .thenReturn(Optional.of(existente));

        when(metodoEnvioRepository.save(existente))
                .thenReturn(existente);

        MetodoEnvio resultado =
                metodoEnvioService.actualizarMetodoEnvio(1, datos);

        assertSame(existente, resultado);
        assertEquals("Retiro en tienda", resultado.getNombre());
        assertEquals("Retiro en local", resultado.getDescripcion());
        assertEquals(new BigDecimal("0.00"), resultado.getCosto());
        assertEquals("Inmediato", resultado.getTiempoEntrega());

        verify(metodoEnvioValidaciones).validar(datos);
        verify(metodoEnvioRepository).save(existente);
    }

    // PRUEBA 5: ELIMINAR METODO DE ENVIO
    @Test
    void eliminarMetodoVerificaPedidosAntesDeEliminar() {

        MetodoEnvio metodoSinPedidos = crearMetodoEnvio();
        MetodoEnvio metodoConPedidos = crearMetodoEnvio();
        metodoConPedidos.setIdMetodoEnvio(2);

        when(metodoEnvioRepository.findById(1))
                .thenReturn(Optional.of(metodoSinPedidos));

        when(metodoEnvioRepository.findById(2))
                .thenReturn(Optional.of(metodoConPedidos));

        when(pedidoRepository.existsByMetodoEnvio_IdMetodoEnvio(1))
                .thenReturn(false);

        when(pedidoRepository.existsByMetodoEnvio_IdMetodoEnvio(2))
                .thenReturn(true);

        // Primer escenario: permite eliminar.
        String resultado = metodoEnvioService.eliminarMetodoEnvio(1);

        assertEquals(
                "Metodo de envio eliminado correctamente",
                resultado);

        verify(metodoEnvioRepository).delete(metodoSinPedidos);

        // Segundo escenario: bloquea la eliminacion.
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> metodoEnvioService.eliminarMetodoEnvio(2));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        // Comprueba que solamente se elimino el metodo sin pedidos.
        verify(metodoEnvioRepository, org.mockito.Mockito.times(1))
                .delete(any(MetodoEnvio.class));
    }
    // METODO AUXILIAR PARA CREAR UN METODO DE ENVIO
    private MetodoEnvio crearMetodoEnvio() {

        MetodoEnvio metodo = new MetodoEnvio();

        metodo.setIdMetodoEnvio(1);
        metodo.setNombre("Despacho normal");
        metodo.setDescripcion("Entrega a domicilio");
        metodo.setCosto(new BigDecimal("3500.00"));
        metodo.setTiempoEntrega("3 a 5 dias");

        return metodo;
    }
}
