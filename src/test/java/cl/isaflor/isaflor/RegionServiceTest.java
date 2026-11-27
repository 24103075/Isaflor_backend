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

import cl.isaflor.isaflor.model.Region;
import cl.isaflor.isaflor.repository.ComunaRepository;
import cl.isaflor.isaflor.repository.RegionRepository;
import cl.isaflor.isaflor.service.RegionService;
import cl.isaflor.isaflor.service.RegionValidaciones;

@ExtendWith(MockitoExtension.class)
class RegionServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private ComunaRepository comunaRepository;

    @Mock
    private RegionValidaciones regionValidaciones;

    @InjectMocks
    private RegionService regionService;

    @Test
    void listarRegionesConvierteEntidadesADto() {
        when(regionRepository.findAll())
                .thenReturn(List.of(crearRegion(1, "Metropolitana")));

        var resultado = regionService.listarRegiones();

        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdRegion());
        assertEquals("Metropolitana", resultado.get(0).getNombreRegion());
    }

    @Test
    void buscarPorIdDevuelveRegionExistente() {
        when(regionRepository.findById(1))
                .thenReturn(Optional.of(crearRegion(1, "Metropolitana")));

        var resultado = regionService.buscarPorId(1);

        assertEquals(1, resultado.getIdRegion());
        assertEquals("Metropolitana", resultado.getNombreRegion());
    }

    @Test
    void guardarRegionValidaNormalizaYPersiste() {
        Region datos = crearRegion(null, "  Valparaiso  ");
        when(regionRepository.existsByNombreRegionIgnoreCase("Valparaiso"))
                .thenReturn(false);
        when(regionRepository.save(any(Region.class)))
                .thenAnswer(invocation -> {
                    Region guardada = invocation.getArgument(0);
                    guardada.setIdRegion(1);
                    return guardada;
                });

        var resultado = regionService.guardarRegion(datos);

        assertEquals(1, resultado.getIdRegion());
        assertEquals("Valparaiso", resultado.getNombreRegion());
        verify(regionValidaciones).validar(datos);
        verify(regionRepository).save(any(Region.class));
    }

    @Test
    void actualizarRegionModificaNombreExistente() {
        Region existente = crearRegion(1, "Metropolitana");
        Region datos = crearRegion(null, "  Valparaiso  ");
        when(regionRepository.findById(1)).thenReturn(Optional.of(existente));
        when(regionRepository.existsByNombreRegionIgnoreCaseAndIdRegionNot(
                "Valparaiso", 1)).thenReturn(false);
        when(regionRepository.save(existente)).thenReturn(existente);

        var resultado = regionService.actualizarRegion(1, datos);

        assertEquals(1, resultado.getIdRegion());
        assertEquals("Valparaiso", resultado.getNombreRegion());
        verify(regionValidaciones).validar(datos);
        verify(regionRepository).save(existente);
    }

    @Test
    void eliminarRegionSinComunasAsociadas() {
        Region region = crearRegion(1, "Metropolitana");
        when(regionRepository.findById(1)).thenReturn(Optional.of(region));
        when(comunaRepository.existsByRegion_IdRegion(1)).thenReturn(false);

        String resultado = regionService.eliminarRegion(1);

        assertEquals("Region eliminada correctamente", resultado);
        verify(regionRepository).delete(region);
    }

    @Test
    void guardarRegionRechazaNombreDuplicado() {
        Region datos = crearRegion(null, "Metropolitana");
        when(regionRepository.existsByNombreRegionIgnoreCase("Metropolitana"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> regionService.guardarRegion(datos));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(regionRepository, never()).save(any(Region.class));
    }

    @Test
    void actualizarRegionRechazaNombreDuplicado() {
        Region existente = crearRegion(1, "Metropolitana");
        Region datos = crearRegion(null, "Valparaiso");
        when(regionRepository.findById(1)).thenReturn(Optional.of(existente));
        when(regionRepository.existsByNombreRegionIgnoreCaseAndIdRegionNot(
                "Valparaiso", 1)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> regionService.actualizarRegion(1, datos));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(regionRepository, never()).save(any(Region.class));
    }

    @Test
    void eliminarRegionConComunasAsociadasDevuelveConflict() {
        Region region = crearRegion(1, "Metropolitana");
        when(regionRepository.findById(1)).thenReturn(Optional.of(region));
        when(comunaRepository.existsByRegion_IdRegion(1)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> regionService.eliminarRegion(1));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(regionRepository, never()).delete(any(Region.class));
    }

    @Test
    void buscarPorIdInexistenteDevuelveNotFound() {
        when(regionRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> regionService.buscarPorId(99));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private Region crearRegion(Integer id, String nombre) {
        Region region = new Region();
        region.setIdRegion(id);
        region.setNombreRegion(nombre);
        return region;
    }
}
