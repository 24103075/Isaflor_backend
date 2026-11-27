
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
import org.springframework.mock.web.MockMultipartFile;

import cl.isaflor.isaflor.model.Imagen;
import cl.isaflor.isaflor.model.Producto;
import cl.isaflor.isaflor.repository.ImagenRepository;
import cl.isaflor.isaflor.repository.ProductoRepository;
import cl.isaflor.isaflor.service.ImagenService;
import cl.isaflor.isaflor.service.ImgBBService;

@SpringBootTest
public class ImagenServiceTest {

    @Autowired
    private ImagenService imagenService;

    @MockitoBean
    private ImagenRepository imagenRepository;

    @MockitoBean
    private ProductoRepository productoRepository;

    @MockitoBean
    private ImgBBService imgBBService;

    // CREAR PRODUCTO PARA LAS PRUEBAS
    private Producto crearProducto() {

        Producto producto = new Producto();
        producto.setIdProducto(1);
        producto.setNombreProducto("Ramo Amanecer");

        return producto;
    }

    // CREAR IMAGEN PARA LAS PRUEBAS
    private Imagen crearImagen() {

        Imagen imagen = new Imagen();
        imagen.setIdImagen(1);
        imagen.setProducto(crearProducto());
        imagen.setNombre("Ramo Amanecer");
        imagen.setUrlImagen("https://i.ibb.co/ejemplo/ramo.jpg");
        imagen.setDescripcion("Ramo de flores");

        return imagen;
    }

    // PRUEBA PARA LISTAR IMAGENES
    @Test
    void testListarImagenes() {

        List<Imagen> imagenes = List.of(
                crearImagen(),
                crearImagen()
        );

        when(imagenRepository.findAll()).thenReturn(imagenes);

        List<Imagen> resultado = imagenService.listarImagenes();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Ramo Amanecer",
                resultado.get(0).getNombre());

        verify(imagenRepository, times(1)).findAll();
    }

    // PRUEBA PARA BUSCAR IMAGEN POR ID
    @Test
    void testBuscarPorId() {

        Imagen imagen = crearImagen();

        when(imagenRepository.findById(1))
                .thenReturn(Optional.of(imagen));

        Imagen resultado = imagenService.buscarPorId(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdImagen());
        assertEquals("Ramo Amanecer", resultado.getNombre());

        verify(imagenRepository, times(1)).findById(1);
    }

    // PRUEBA PARA GUARDAR IMAGEN
    @Test
    void testGuardarImagen() throws Exception {

        Producto producto = crearProducto();
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "ramo.jpg",
                "image/jpeg",
                "imagen de prueba".getBytes()
        );

        String url = "https://i.ibb.co/ejemplo/ramo.jpg";

        when(productoRepository.findById(1))
                .thenReturn(Optional.of(producto));

        when(imgBBService.subirImagen(archivo))
                .thenReturn(url);

        when(imagenRepository.save(any(Imagen.class)))
                .thenAnswer(invocation -> {
                    Imagen imagen = invocation.getArgument(0);
                    imagen.setIdImagen(1);
                    return imagen;
                });

        Imagen resultado = imagenService.guardarImagen(
                1, archivo, "Ramo de flores");

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdImagen());
        assertEquals("Ramo Amanecer", resultado.getProducto().getNombreProducto());
        assertEquals(url, resultado.getUrlImagen());

        verify(productoRepository, times(1)).findById(1);
        verify(imgBBService, times(1)).subirImagen(archivo);
        verify(imagenRepository, times(1)).save(any(Imagen.class));
    }

    // PRUEBA PARA ELIMINAR IMAGEN
    @Test
    void testEliminarImagen() {

        Imagen imagen = crearImagen();

        when(imagenRepository.findById(1))
                .thenReturn(Optional.of(imagen));

        String resultado = imagenService.eliminarImagen(1);

        assertNotNull(resultado);
        assertEquals("Imagen eliminada correctamente", resultado);

        verify(imagenRepository, times(1)).findById(1);
        verify(imagenRepository, times(1)).delete(imagen);
    }
}
