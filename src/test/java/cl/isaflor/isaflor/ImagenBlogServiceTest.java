
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.ImagenBlogResponseDTO;
import cl.isaflor.isaflor.model.Blog;
import cl.isaflor.isaflor.model.ImagenBlog;
import cl.isaflor.isaflor.repository.BlogRepository;
import cl.isaflor.isaflor.repository.ImagenBlogRepository;
import cl.isaflor.isaflor.service.ImgBBService;
import cl.isaflor.isaflor.service.ImagenBlogService;

@ExtendWith(MockitoExtension.class)
class ImagenBlogServiceTest {

    @Mock
    private ImagenBlogRepository imagenBlogRepository;

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private ImgBBService imgBBService;

    @InjectMocks
    private ImagenBlogService imagenBlogService;

    // PRUEBA 1: LISTAR IMAGENES
    @Test
    void listarPublicamenteSoloConsultaImagenesDeBlogsPublicados() {

        when(imagenBlogRepository.findByBlog_EstadoOrderByIdImagenBlogAsc("PUBLICADO"))
                .thenReturn(List.of(crearImagen(3, "PUBLICADO")));

        List<ImagenBlogResponseDTO> resultado =
                imagenBlogService.listarImagenes(false);

        assertEquals(1, resultado.size());
        assertEquals(3, resultado.get(0).getIdBlog());

        verify(imagenBlogRepository)
                .findByBlog_EstadoOrderByIdImagenBlogAsc("PUBLICADO");

        verify(imagenBlogRepository, never())
                .findAllByOrderByIdImagenBlogAsc();
    }

    // PRUEBA 2: BUSCAR IMAGEN POR ID
    @Test
    void visitanteNoPuedeConsultarImagenDeBorrador() {

        when(imagenBlogRepository.findById(5))
                .thenReturn(Optional.of(crearImagen(3, "BORRADOR")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> imagenBlogService.buscarPorId(5, false));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // PRUEBA 3: GUARDAR IMAGEN
    @Test
    void subirImagenLaAsociaAlBlogYValidaPermisos() {

        Blog blog = crearBlog(3, "BORRADOR");

        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "portada.jpg",
                "image/jpeg",
                new byte[] { 1, 2, 3 });

        when(blogRepository.findById(3))
                .thenReturn(Optional.of(blog));

        when(imgBBService.subirImagen(archivo))
                .thenReturn("https://i.ibb.co/example/portada.jpg");

        when(imagenBlogRepository.save(any(ImagenBlog.class)))
                .thenAnswer(invocation -> {
                    ImagenBlog imagen = invocation.getArgument(0);
                    imagen.setIdImagenBlog(5);
                    return imagen;
                });

        ImagenBlogResponseDTO resultado = imagenBlogService.guardarImagen(
                3, archivo, "Portada del articulo", true);

        assertEquals(5, resultado.getIdImagenBlog());
        assertEquals(3, resultado.getIdBlog());
        assertEquals("https://i.ibb.co/example/portada.jpg",
                resultado.getUrlImagen());
        assertEquals("Portada del articulo", resultado.getDescripcion());

        verify(imgBBService).subirImagen(archivo);
        verify(imagenBlogRepository).save(any(ImagenBlog.class));

        // Comprobar que un usuario sin permisos no puede subir imagenes.
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> imagenBlogService.guardarImagen(
                        3, archivo, null, false));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(blogRepository).findById(3);
    }

    // PRUEBA 4: ELIMINAR IMAGEN
    @Test
    void eliminarImagenBorraSuRegistro() {

        ImagenBlog imagen = crearImagen(3, "PUBLICADO");

        when(imagenBlogRepository.findById(5))
                .thenReturn(Optional.of(imagen));

        String resultado = imagenBlogService.eliminarImagen(5, true);

        assertEquals(
                "Imagen del blog eliminada correctamente",
                resultado);

        verify(imagenBlogRepository).delete(imagen);
    }

    // METODO AUXILIAR PARA CREAR IMAGENES
    private ImagenBlog crearImagen(Integer idBlog, String estado) {

        ImagenBlog imagen = new ImagenBlog();

        imagen.setIdImagenBlog(5);
        imagen.setNombre("portada.jpg");
        imagen.setUrlImagen("https://i.ibb.co/example/portada.jpg");
        imagen.setBlog(crearBlog(idBlog, estado));

        return imagen;
    }

    // METODO AUXILIAR PARA CREAR BLOGS
    private Blog crearBlog(Integer id, String estado) {

        Blog blog = new Blog();

        blog.setIdBlog(id);
        blog.setEstado(estado);
        blog.setTitulo("Cuidados de flores");
        blog.setContenido("Consejos");

        return blog;
    }
}
