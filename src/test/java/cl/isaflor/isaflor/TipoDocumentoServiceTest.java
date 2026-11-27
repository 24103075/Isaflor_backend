package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import cl.isaflor.isaflor.model.TipoDocumento;
import cl.isaflor.isaflor.repository.PedidoRepository;
import cl.isaflor.isaflor.repository.TipoDocumentoRepository;
import cl.isaflor.isaflor.service.TipoDocumentoService;
import cl.isaflor.isaflor.service.TipoDocumentoValidaciones;

@ExtendWith(MockitoExtension.class)
class TipoDocumentoServiceTest {

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private TipoDocumentoValidaciones tipoDocumentoValidaciones;

    @InjectMocks
    private TipoDocumentoService tipoDocumentoService;

    @Test
    void listarTiposDocumentoConvierteEntidadesADto() {
        when(tipoDocumentoRepository.findAll())
                .thenReturn(List.of(crearTipoDocumento(1, "RUT")));

        var resultado = tipoDocumentoService.listarTiposDocumento();

        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdTipoDocumento());
        assertEquals("RUT", resultado.get(0).getNombreDocumento());
    }

    @Test
    void buscarPorIdDevuelveTipoDocumentoExistente() {
        when(tipoDocumentoRepository.findById(1))
                .thenReturn(Optional.of(crearTipoDocumento(1, "RUT")));

        var resultado = tipoDocumentoService.buscarPorId(1);

        assertEquals(1, resultado.getIdTipoDocumento());
        assertEquals("RUT", resultado.getNombreDocumento());
    }

    @Test
    void guardarTipoDocumentoValidaNormalizaYPersiste() {
        TipoDocumento datos = crearTipoDocumento(null, "  Pasaporte  ");
        datos.setDescripcion(" Documento extranjero ");
        when(tipoDocumentoRepository.existsByNombreDocumentoIgnoreCase("Pasaporte"))
                .thenReturn(false);
        when(tipoDocumentoRepository.save(any(TipoDocumento.class)))
                .thenAnswer(invocation -> {
                    TipoDocumento guardado = invocation.getArgument(0);
                    guardado.setIdTipoDocumento(1);
                    return guardado;
                });

        var resultado = tipoDocumentoService.guardarTipoDocumento(datos);

        assertEquals(1, resultado.getIdTipoDocumento());
        assertEquals("Pasaporte", resultado.getNombreDocumento());
        assertEquals("Documento extranjero", resultado.getDescripcion());
        verify(tipoDocumentoValidaciones).validar(datos);
        verify(tipoDocumentoRepository).save(any(TipoDocumento.class));
    }

    @Test
    void actualizarTipoDocumentoModificaDatosExistentes() {
        TipoDocumento existente = crearTipoDocumento(1, "RUT");
        TipoDocumento datos = crearTipoDocumento(null, "  Pasaporte  ");
        datos.setDescripcion(" Documento extranjero ");
        when(tipoDocumentoRepository.findById(1))
                .thenReturn(Optional.of(existente));
        when(tipoDocumentoRepository
                .existsByNombreDocumentoIgnoreCaseAndIdTipoDocumentoNot(
                        "Pasaporte", 1))
                .thenReturn(false);
        when(tipoDocumentoRepository.save(existente)).thenReturn(existente);

        var resultado = tipoDocumentoService.actualizarTipoDocumento(1, datos);

        assertEquals(1, resultado.getIdTipoDocumento());
        assertEquals("Pasaporte", resultado.getNombreDocumento());
        assertEquals("Documento extranjero", resultado.getDescripcion());
        verify(tipoDocumentoValidaciones).validar(datos);
        verify(tipoDocumentoRepository).save(existente);
    }

    @Test
    void eliminarTipoDocumentoSinPedidosAsociados() {
        TipoDocumento tipoDocumento = crearTipoDocumento(1, "RUT");
        when(tipoDocumentoRepository.findById(1))
                .thenReturn(Optional.of(tipoDocumento));
        when(pedidoRepository.existsByTipoDocumento_IdTipoDocumento(1))
                .thenReturn(false);

        String resultado = tipoDocumentoService.eliminarTipoDocumento(1);

        assertEquals("Tipo de documento eliminado correctamente", resultado);
        verify(tipoDocumentoRepository).delete(tipoDocumento);
    }

    @Test
    void guardarTipoDocumentoRechazaNombreDuplicado() {
        TipoDocumento datos = crearTipoDocumento(null, "RUT");
        when(tipoDocumentoRepository.existsByNombreDocumentoIgnoreCase("RUT"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tipoDocumentoService.guardarTipoDocumento(datos));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(tipoDocumentoRepository, never()).save(any(TipoDocumento.class));
    }

    @Test
    void actualizarTipoDocumentoRechazaNombreDuplicado() {
        TipoDocumento existente = crearTipoDocumento(1, "RUT");
        TipoDocumento datos = crearTipoDocumento(null, "Pasaporte");
        when(tipoDocumentoRepository.findById(1))
                .thenReturn(Optional.of(existente));
        when(tipoDocumentoRepository
                .existsByNombreDocumentoIgnoreCaseAndIdTipoDocumentoNot(
                        "Pasaporte", 1))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tipoDocumentoService.actualizarTipoDocumento(1, datos));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(tipoDocumentoRepository, never()).save(any(TipoDocumento.class));
    }

    @Test
    void eliminarTipoDocumentoConPedidosAsociadosDevuelveConflict() {
        TipoDocumento tipoDocumento = crearTipoDocumento(1, "RUT");
        when(tipoDocumentoRepository.findById(1))
                .thenReturn(Optional.of(tipoDocumento));
        when(pedidoRepository.existsByTipoDocumento_IdTipoDocumento(1))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tipoDocumentoService.eliminarTipoDocumento(1));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(tipoDocumentoRepository, never()).delete(any(TipoDocumento.class));
    }

    @Test
    void buscarPorIdInexistenteDevuelveNotFound() {
        when(tipoDocumentoRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tipoDocumentoService.buscarPorId(99));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private TipoDocumento crearTipoDocumento(Integer id, String nombre) {
        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setIdTipoDocumento(id);
        tipoDocumento.setNombreDocumento(nombre);
        tipoDocumento.setDescripcion("Documento de prueba");
        return tipoDocumento;
    }
}
