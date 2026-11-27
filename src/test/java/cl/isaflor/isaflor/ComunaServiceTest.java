
package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import cl.isaflor.isaflor.model.Comuna;
import cl.isaflor.isaflor.model.Region;
import cl.isaflor.isaflor.repository.ComunaRepository;
import cl.isaflor.isaflor.repository.RegionRepository;
import cl.isaflor.isaflor.service.ComunaService;
import cl.isaflor.isaflor.service.ComunaValidaciones;

@SpringBootTest
public class ComunaServiceTest {

    @Autowired
    private ComunaService comunaService;

    @MockitoBean
    private ComunaRepository comunaRepository;

    @MockitoBean
    private RegionRepository regionRepository;

    @MockitoBean
    private ComunaValidaciones comunaValidaciones;

    // CREAR REGION PARA LAS PRUEBAS
    private Region crearRegion() {
        Region region = new Region();
        region.setIdRegion(1);
        region.setNombreRegion("Region Metropolitana");
        return region;
    }

    // CREAR COMUNA PARA LAS PRUEBAS
    private Comuna crearComuna() {
        Comuna comuna = new Comuna();
        comuna.setIdComuna(1);
        comuna.setNombreComuna("Santiago");
        comuna.setRegion(crearRegion());
        return comuna;
    }

    // PRUEBA PARA LISTAR COMUNAS
    @Test
    void testListarComunas() {
        List<Comuna> comunas = List.of(
                crearComuna(),
                new Comuna(2, "Maipu", crearRegion()));

        when(comunaRepository.findAll()).thenReturn(comunas);

        List<Comuna> resultado = comunaService.listarComunas();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Santiago", resultado.get(0).getNombreComuna());
        assertEquals("Maipu", resultado.get(1).getNombreComuna());

        verify(comunaRepository, times(1)).findAll();
    }

    // PRUEBA PARA BUSCAR COMUNA POR ID
    @Test
    void testBuscarPorId() {
        Comuna comuna = crearComuna();

        when(comunaRepository.findById(1))
                .thenReturn(Optional.of(comuna));

        Comuna resultado = comunaService.buscarPorId(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdComuna());
        assertEquals("Santiago", resultado.getNombreComuna());
        assertEquals("Region Metropolitana",
                resultado.getRegion().getNombreRegion());

        verify(comunaRepository, times(1)).findById(1);
    }

    // PRUEBA PARA GUARDAR COMUNA
    @Test
    void testGuardarComuna() {
        Comuna comuna = crearComuna();
        Region region = crearRegion();

        when(regionRepository.findById(1)).thenReturn(Optional.of(region));

        when(comunaRepository.save(any(Comuna.class)))
                .thenReturn(comuna);

        Comuna resultado = comunaService.guardarComuna(comuna);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdComuna());
        assertEquals("Santiago", resultado.getNombreComuna());
        assertEquals("Region Metropolitana",
                resultado.getRegion().getNombreRegion());

        verify(regionRepository, times(1)).findById(1);
        verify(comunaRepository, times(1)).save(comuna);
        verify(comunaValidaciones, times(1)).validarComuna(comuna);
    }

    // PRUEBA PARA ACTUALIZAR COMUNA
    @Test
    void testActualizarComuna() {
        Comuna comunaExistente = crearComuna();

        Comuna datosNuevos = new Comuna();
        datosNuevos.setNombreComuna("Maipu");
        datosNuevos.setRegion(crearRegion());

        when(comunaRepository.findById(1))
                .thenReturn(Optional.of(comunaExistente));

        when(regionRepository.findById(1))
                .thenReturn(Optional.of(crearRegion()));

        when(comunaRepository.save(any(Comuna.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Comuna resultado = comunaService.actualizarComuna(1, datosNuevos);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdComuna());
        assertEquals("Maipu", resultado.getNombreComuna());
        assertEquals("Region Metropolitana",
                resultado.getRegion().getNombreRegion());

        verify(comunaRepository, times(1)).findById(1);
        verify(regionRepository, times(1)).findById(1);
        verify(comunaRepository, times(1)).save(comunaExistente);
        verify(comunaValidaciones, times(1)).validarComuna(datosNuevos);
    }

    // PRUEBA PARA ELIMINAR COMUNA
    @Test
    void testEliminarComuna() {
        Comuna comuna = crearComuna();

        when(comunaRepository.findById(1))
                .thenReturn(Optional.of(comuna));

        String resultado = comunaService.eliminarComuna(1);

        assertNotNull(resultado);
        assertEquals("Comuna eliminada correctamente", resultado);

        verify(comunaRepository, times(1)).findById(1);
        verify(comunaRepository, times(1)).delete(comuna);
    }
}
