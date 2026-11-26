package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import cl.isaflor.isaflor.dto.ProductoDTO;
import cl.isaflor.isaflor.model.Categoria;
import cl.isaflor.isaflor.model.Producto;
import cl.isaflor.isaflor.repository.CategoriaRepository;
import cl.isaflor.isaflor.repository.ProductoRepository;
import cl.isaflor.isaflor.service.ProductoService;
import cl.isaflor.isaflor.service.ProductoValidaciones;

@SpringBootTest 
public class ProductoServiceTest {
    
    
    @Autowired
    private ProductoService productoService;

    @MockitoBean
    private ProductoRepository productoRepository;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

    @MockitoBean
    private ProductoValidaciones productoValidaciones;

    // METODO PARA CREAR UN PRODUCTO DE PRUEBA
    private Producto crearProducto() {

        Categoria categoria = new Categoria();
        categoria.setIdCategoria(1);
        categoria.setNombreCategoria("Flores");

        Producto producto = new Producto();
        producto.setIdProducto(1);
        producto.setCodigoProducto("FLOR001");
        producto.setNombreProducto("Ramo de rosas");
        producto.setDescripcion("Ramo de 12 rosas rojas");
        producto.setPrecioProducto(new BigDecimal("25000"));
        producto.setStockProducto(10);
        producto.setStockCritico(2);
        producto.setPesoProducto(new BigDecimal("1.50"));
        producto.setPesoVolumetrico(new BigDecimal("2.00"));
        producto.setCategoria(categoria);

        return producto;
    }

    // METODO PARA CREAR UN DTO DE PRUEBA
    private ProductoDTO crearProductoDTO() {

        ProductoDTO dto = new ProductoDTO();

        dto.setIdProducto(1);
        dto.setCodigoProducto("FLOR001");
        dto.setNombreProducto("Ramo de rosas");
        dto.setDescripcion("Ramo de 12 rosas rojas");
        dto.setPrecioProducto(new BigDecimal("25000"));
        dto.setStockProducto(10);
        dto.setStockCritico(2);
        dto.setPesoProducto(new BigDecimal("1.50"));
        dto.setPesoVolumetrico(new BigDecimal("2.00"));
        dto.setIdCategoria(1);
        dto.setNombreCategoria("Flores");

        return dto;
    }

    // PRUEBA PARA LISTAR PRODUCTOS
    @Test
    void testListarProductos() {

        // GIVEN PREPARAR LOS DATOS
        Producto producto = crearProducto();
        ProductoDTO dto = crearProductoDTO();

        when(productoRepository.findAll())
                .thenReturn(List.of(producto));

        when(productoValidaciones.convertirADTO(producto))
                .thenReturn(dto);

        // WHEN EJECUTAR EL METODO
        List<ProductoDTO> resultado = productoService.listarProductos();

        // THEN COMPROBAR EL RESULTADO
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Ramo de rosas", resultado.get(0).getNombreProducto());

        verify(productoRepository, times(1)).findAll();
    }

    // PRUEBA PARA LISTAR SIN PRODUCTOS
    @Test
    void testListarProductos_Vacio() {

        when(productoRepository.findAll()).thenReturn(List.of());

        List<ProductoDTO> resultado = productoService.listarProductos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(productoRepository, times(1)).findAll();
    }

    // PRUEBA DE BUSCAR PRODUCTO POR ID
    @Test
    void testBuscarPorId_Exitoso() {

        // GIVEN
        Producto producto = crearProducto();
        ProductoDTO dto = crearProductoDTO();

        when(productoRepository.findById(1))
                .thenReturn(Optional.of(producto));

        when(productoValidaciones.convertirADTO(producto))
                .thenReturn(dto);

        // WHEN
        ProductoDTO resultado = productoService.buscarPorId(1);

        // THEN
        assertNotNull(resultado);
        assertEquals(1, resultado.getIdProducto());
        assertEquals("Ramo de rosas", resultado.getNombreProducto());
    }

    // PRUEBA DE  BUSCAR PRODUCTO INEXISTENTE
    @Test
    void testBuscarPorId_NoEncontrado() {
    
        when(productoRepository.findById(2))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productoService.buscarPorId(2));

        assertEquals("No se encontro el producto con ID: 2",
                exception.getMessage());
    }

    // PRUEBA DE GUARDAR PRODUCTO
    @Test
    void testGuardarProducto_Exitoso() {

        Producto producto = crearProducto();
        ProductoDTO dto = crearProductoDTO();

        when(productoRepository.existsByCodigoProductoIgnoreCase("FLOR001"))
                .thenReturn(false);

        when(categoriaRepository.findById(1))
                .thenReturn(Optional.of(producto.getCategoria()));

        when(productoRepository.save(any(Producto.class)))
                .thenReturn(producto);

        when(productoValidaciones.validarNullVacio(producto))
                .thenReturn(true);

        when(productoValidaciones.convertirADTO(producto))
                .thenReturn(dto);

        ProductoDTO resultado = productoService.guardarProducto(producto);

        assertNotNull(resultado);
        assertEquals("FLOR001", resultado.getCodigoProducto());
        assertEquals("Ramo de rosas", resultado.getNombreProducto());

        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    // PRUEBA GUARDAR PRODUCTO CON CODIGO DUPLICADO
    @Test
    void testGuardarProducto_CodigoDuplicado() {

        Producto producto = crearProducto();

        when(productoValidaciones.validarNullVacio(producto))
                .thenReturn(true);

        when(productoRepository.existsByCodigoProductoIgnoreCase("FLOR001"))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productoService.guardarProducto(producto));

        assertEquals("Ya existe un producto con ese codigo",exception.getMessage());

        verify(productoRepository, never()).save(any(Producto.class));
    }

    // PRUEBA GUARDAR SIN CATEGORÍA
    @Test
    void testGuardarProducto_SinCategoria() {

        Producto producto = crearProducto();
        producto.setCategoria(null);

        when(productoValidaciones.validarNullVacio(producto))
                .thenReturn(true);

        when(productoRepository.existsByCodigoProductoIgnoreCase("FLOR001"))
                .thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productoService.guardarProducto(producto));

        assertEquals("Debe seleccionar una categoria",
                exception.getMessage());

        verify(productoRepository, never()).save(any(Producto.class));
    }

    // PRUEBA CATEGORÍA INEXISTENTE
    @Test
    void testGuardarProducto_CategoriaInexistente() {

        Producto producto = crearProducto();

        when(productoValidaciones.validarNullVacio(producto))
                .thenReturn(true);

        when(productoRepository.existsByCodigoProductoIgnoreCase("FLOR001"))
                .thenReturn(false);

        when(categoriaRepository.findById(1))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productoService.guardarProducto(producto));

        assertEquals("La categoria no existe", exception.getMessage());

        verify(productoRepository, never()).save(any(Producto.class));
    }

    // PRUEBA ACTUALIZAR PRODUCTO
    @Test
    void testActualizarProducto_Exitoso() {

        Producto productoExistente = crearProducto();
        Producto productoNuevo = crearProducto();
        ProductoDTO dto = crearProductoDTO();

        productoNuevo.setNombreProducto("Ramo de tulipanes");
        productoNuevo.setDescripcion("Ramo de 10 tulipanes");

        dto.setNombreProducto("Ramo de tulipanes");
        dto.setDescripcion("Ramo de 10 tulipanes");

        when(productoRepository.findById(1))
                .thenReturn(Optional.of(productoExistente));

        when(productoValidaciones.validarNullVacio(productoNuevo))
                .thenReturn(true);

        when(productoRepository.existsByCodigoProductoIgnoreCase("FLOR001"))
                .thenReturn(false);

        when(categoriaRepository.findById(1))
                .thenReturn(Optional.of(productoExistente.getCategoria()));

        when(productoRepository.save(any(Producto.class)))
                .thenReturn(productoExistente);

        when(productoValidaciones.convertirADTO(productoExistente))
                .thenReturn(dto);

        ProductoDTO resultado =
                productoService.actualizarProducto(1, productoNuevo);

        assertNotNull(resultado);
        assertEquals("Ramo de tulipanes", resultado.getNombreProducto());
        assertEquals("Ramo de 10 tulipanes", resultado.getDescripcion());

        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    // PRUEBA ACTUALIZAR PRODUCTO INEXISTENTE
    @Test
    void testActualizarProducto_NoEncontrado() {

        Producto productoNuevo = crearProducto();

        when(productoRepository.findById(2))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productoService.actualizarProducto(2, productoNuevo));

        assertEquals("No se encontro el producto con ID: 2",
                exception.getMessage());

        verify(productoRepository, never()).save(any(Producto.class));
    }

    // PRUEBA ELIMINAR PRODUCTO
    @Test
    void testEliminarProducto_Exitoso() {

        Producto producto = crearProducto();

        when(productoRepository.findById(1))
                .thenReturn(Optional.of(producto));

        String resultado = productoService.eliminarProducto(1);

        assertNotNull(resultado);
        assertEquals("Producto eliminado correctamente", resultado);

        verify(productoRepository, times(1)).delete(producto);
    }

    // PRUEBA ELIMINAR PRODUCTO INEXISTENTE
    @Test
    void testEliminarProducto_NoEncontrado() {

        when(productoRepository.findById(2))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productoService.eliminarProducto(2));


        assertEquals("No se encontro el producto con ID: 2",
                exception.getMessage());

        verify(productoRepository, never()).delete(any(Producto.class));
    }
}
