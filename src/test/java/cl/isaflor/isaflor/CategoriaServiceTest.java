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

import cl.isaflor.isaflor.model.Categoria;
import cl.isaflor.isaflor.repository.CategoriaRepository;
import cl.isaflor.isaflor.repository.ProductoRepository;
import cl.isaflor.isaflor.service.CategoriaService;
import cl.isaflor.isaflor.service.CategoriaValidaciones;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaValidaciones categoriaValidaciones;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void listarCategoriasConvierteLasEntidadesADto() {
        when(categoriaRepository.findAll()).thenReturn(
                List.of(crearCategoria(1, "Flores")));

        var resultado = categoriaService.listarCategorias();

        assertEquals(1, resultado.size());
        assertEquals("Flores", resultado.get(0).getNombreCategoria());
        verify(categoriaRepository).findAll();
    }

    @Test
    void buscarPorIdDevuelveCategoriaExistente() {
        Categoria categoria = crearCategoria(1, "Flores");
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));

        var resultado = categoriaService.buscarPorId(1);

        assertEquals(1, resultado.getIdCategoria());
        assertEquals("Flores", resultado.getNombreCategoria());
    }

    @Test
    void guardarCategoriaValidaNormalizaYPersiste() {
        Categoria datos = crearCategoria(null, "  Flores  ");
        when(categoriaRepository.existsByNombreCategoriaIgnoreCase("Flores"))
                .thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocation -> {
                    Categoria guardada = invocation.getArgument(0);
                    guardada.setIdCategoria(1);
                    return guardada;
                });

        var resultado = categoriaService.guardarCategoria(datos);

        assertEquals(1, resultado.getIdCategoria());
        assertEquals("Flores", resultado.getNombreCategoria());
        verify(categoriaValidaciones).validar(datos);
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void actualizarCategoriaModificaDatosDeLaExistente() {
        Categoria existente = crearCategoria(1, "Flores");
        Categoria datos = crearCategoria(null, "  Plantas  ");
        datos.setDescripcion(" Ornamentales ");
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(existente));
        when(categoriaRepository.existsByNombreCategoriaIgnoreCaseAndIdCategoriaNot(
                "Plantas", 1)).thenReturn(false);
        when(categoriaRepository.save(existente)).thenReturn(existente);

        var resultado = categoriaService.actualizarCategoria(1, datos);

        assertEquals(1, resultado.getIdCategoria());
        assertEquals("Plantas", resultado.getNombreCategoria());
        assertEquals("Ornamentales", resultado.getDescripcion());
        verify(categoriaValidaciones).validar(datos);
        verify(categoriaRepository).save(existente);
    }

    @Test
    void eliminarCategoriaSinProductosAsociados() {
        Categoria categoria = crearCategoria(1, "Flores");
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(productoRepository.existsByCategoria_IdCategoria(1)).thenReturn(false);

        String resultado = categoriaService.eliminarCategoria(1);

        assertEquals("Categoria eliminada correctamente", resultado);
        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void guardarCategoriaRechazaNombreDuplicado() {
        Categoria datos = crearCategoria(null, "Flores");
        when(categoriaRepository.existsByNombreCategoriaIgnoreCase("Flores"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoriaService.guardarCategoria(datos));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void eliminarCategoriaConProductosAsociadosDevuelveConflict() {
        Categoria categoria = crearCategoria(1, "Flores");
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(productoRepository.existsByCategoria_IdCategoria(1)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoriaService.eliminarCategoria(1));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(categoriaRepository, never()).delete(any(Categoria.class));
    }

    private Categoria crearCategoria(Integer id, String nombre) {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(id);
        categoria.setNombreCategoria(nombre);
        categoria.setDescripcion("Categoria de prueba");
        return categoria;
    }
}
