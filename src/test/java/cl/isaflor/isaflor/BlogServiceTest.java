
package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import cl.isaflor.isaflor.dto.BlogRequestDTO;
import cl.isaflor.isaflor.dto.BlogEstadoRequestDTO;
import cl.isaflor.isaflor.dto.BlogResponseDTO;
import cl.isaflor.isaflor.model.Blog;
import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.repository.BlogRepository;
import cl.isaflor.isaflor.repository.ImagenBlogRepository;
import cl.isaflor.isaflor.repository.UsuarioRepository;
import cl.isaflor.isaflor.repository.VideoRepository;
import cl.isaflor.isaflor.service.BlogService;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ImagenBlogRepository imagenBlogRepository;

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private BlogService blogService;

    // LISTAR BLOGS

    @Test
    void listarBlogsPublicosSoloConsultaPublicados() {
        Blog blog = crearBlog(1, "PUBLICADO");

        when(blogRepository.findByEstadoOrderByFechaPublicacionDesc("PUBLICADO"))
                .thenReturn(List.of(blog));

        List<BlogResponseDTO> resultado = blogService.listarBlogs(false);

        assertEquals(1, resultado.size());
        assertEquals("PUBLICADO", resultado.get(0).getEstado());

        verify(blogRepository)
                .findByEstadoOrderByFechaPublicacionDesc("PUBLICADO");

        verify(blogRepository, never())
                .findAllByOrderByFechaPublicacionDesc();
    }

    @Test
    void administradorPuedeListarBorradoresYPublicados() {
        when(blogRepository.findAllByOrderByFechaPublicacionDesc())
                .thenReturn(List.of(
                        crearBlog(1, "BORRADOR"),
                        crearBlog(2, "PUBLICADO")));

        List<BlogResponseDTO> resultado = blogService.listarBlogs(true);

        assertEquals(2, resultado.size());
        verify(blogRepository).findAllByOrderByFechaPublicacionDesc();
    }

    // BUSCAR BLOG

    @Test
    void visitanteNoPuedeConsultarBorradorPorId() {
        when(blogRepository.findById(1))
                .thenReturn(Optional.of(crearBlog(1, "BORRADOR")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> blogService.buscarPorId(1, false));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void buscarBlogInexistenteDevuelveNotFound() {
        when(blogRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> blogService.buscarPorId(99, true));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // CREAR BLOG

    @Test
    void crearObtieneAutorDesdeElUsuarioAutenticado() {
        Usuario autor = new Usuario();
        autor.setCorreo("admin@isaflor.cl");
        autor.setNombre("Admin");
        autor.setApellidoPaterno("Isaflor");

        when(usuarioRepository.findByCorreoIgnoreCase("admin@isaflor.cl"))
                .thenReturn(Optional.of(autor));

        when(blogRepository.save(any(Blog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BlogResponseDTO resultado = blogService.crear(
                crearRequest(), "admin@isaflor.cl", true);

        ArgumentCaptor<Blog> blogCaptor = ArgumentCaptor.forClass(Blog.class);

        verify(blogRepository).save(blogCaptor.capture());

        assertSame(autor, blogCaptor.getValue().getUsuario());
        assertEquals("BORRADOR", resultado.getEstado());
        assertNull(blogCaptor.getValue().getFechaPublicacion());
    }

    @Test
    void clienteNoPuedeCrearBlogs() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> blogService.crear(
                        crearRequest(), "cliente@isaflor.cl", false));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(blogRepository, never()).save(any(Blog.class));
    }

    @Test
    void crearRechazaUsuarioAutenticadoInexistente() {
        when(usuarioRepository.findByCorreoIgnoreCase("admin@isaflor.cl"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> blogService.crear(
                        crearRequest(), "admin@isaflor.cl", true));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verify(blogRepository, never()).save(any(Blog.class));
    }

    // ACTUALIZAR BLOG

    @Test
    void actualizarModificaTituloYContenido() {
        Blog blog = crearBlog(1, "BORRADOR");

        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));
        when(blogRepository.save(blog)).thenReturn(blog);

        BlogRequestDTO request = crearRequest();
        request.setTitulo("Nuevo titulo");
        request.setContenido("Nuevo contenido");

        BlogResponseDTO resultado = blogService.actualizar(1, request, true);

        assertEquals("Nuevo titulo", resultado.getTitulo());
        assertEquals("Nuevo contenido", resultado.getContenido());
        assertEquals("BORRADOR", resultado.getEstado());
        assertNull(resultado.getFechaPublicacion());
    }

    @Test
    void clienteNoPuedeActualizarBlogs() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> blogService.actualizar(1, crearRequest(), false));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(blogRepository, never()).save(any(Blog.class));
    }

    // CAMBIAR ESTADO

    @Test
    void administradorPuedePublicarBlog() {
        Blog blog = crearBlog(1, "BORRADOR");

        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));
        when(blogRepository.save(blog)).thenReturn(blog);

        BlogEstadoRequestDTO request = crearEstadoRequest("PUBLICADO");

        BlogResponseDTO resultado = blogService.cambiarEstado(1, request, true);

        assertEquals("PUBLICADO", resultado.getEstado());
        assertNotNull(blog.getFechaPublicacion());
    }

    @Test
    void cambiarEstadoRechazaValorNoPermitido() {
        Blog blog = crearBlog(1, "BORRADOR");

        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));

        BlogEstadoRequestDTO request = crearEstadoRequest("ARCHIVADO");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> blogService.cambiarEstado(1, request, true));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(blogRepository, never()).save(any(Blog.class));
    }

    @Test
    void devolverBlogABorradorDejaFechaNula() {
        Blog blog = crearBlog(1, "PUBLICADO");

        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));
        when(blogRepository.save(blog)).thenReturn(blog);

        BlogEstadoRequestDTO request = crearEstadoRequest("BORRADOR");

        BlogResponseDTO resultado = blogService.cambiarEstado(1, request, true);

        assertEquals("BORRADOR", resultado.getEstado());
        assertNull(resultado.getFechaPublicacion());
        assertNull(blog.getFechaPublicacion());
    }

    @Test
    void publicarBlogYaPublicadoConservaFechaOriginal() {
        Blog blog = crearBlog(1, "PUBLICADO");

        LocalDateTime fechaOriginal = blog.getFechaPublicacion();

        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));
        when(blogRepository.save(blog)).thenReturn(blog);

        BlogEstadoRequestDTO request = crearEstadoRequest("PUBLICADO");

        BlogResponseDTO resultado = blogService.cambiarEstado(1, request, true);

        assertEquals(fechaOriginal, resultado.getFechaPublicacion());
        assertEquals(fechaOriginal, blog.getFechaPublicacion());
    }

    @Test
    void volverAPublicarUnBorradorAsignaNuevaFecha() {
        Blog blog = crearBlog(1, "BORRADOR");
        blog.setFechaPublicacion(null);

        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));
        when(blogRepository.save(blog)).thenReturn(blog);

        BlogEstadoRequestDTO request = crearEstadoRequest("PUBLICADO");

        BlogResponseDTO resultado = blogService.cambiarEstado(1, request, true);

        assertEquals("PUBLICADO", resultado.getEstado());
        assertNotNull(resultado.getFechaPublicacion());
    }

    @Test
    void clienteNoPuedeCambiarEstadoDelBlog() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> blogService.cambiarEstado(
                        1, crearEstadoRequest("PUBLICADO"), false));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(blogRepository, never()).save(any(Blog.class));
    }

    // ELIMINAR BLOG

    @Test
    void eliminarSeBloqueaSiHayVideosAsociados() {
        when(blogRepository.findById(1))
                .thenReturn(Optional.of(crearBlog(1, "PUBLICADO")));

        when(imagenBlogRepository.existsByBlog_IdBlog(1)).thenReturn(false);
        when(videoRepository.existsByBlog_IdBlog(1)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> blogService.eliminar(1, true));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(blogRepository, never()).delete(any(Blog.class));
    }

    @Test
    void eliminarSeBloqueaSiHayImagenesAsociadas() {
        when(blogRepository.findById(1))
                .thenReturn(Optional.of(crearBlog(1, "PUBLICADO")));

        when(imagenBlogRepository.existsByBlog_IdBlog(1)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> blogService.eliminar(1, true));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(blogRepository, never()).delete(any(Blog.class));
    }

    @Test
    void eliminarBlogSinContenidoAsociado() {
        Blog blog = crearBlog(1, "PUBLICADO");

        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));
        when(imagenBlogRepository.existsByBlog_IdBlog(1)).thenReturn(false);
        when(videoRepository.existsByBlog_IdBlog(1)).thenReturn(false);

        blogService.eliminar(1, true);

        verify(blogRepository).delete(blog);
    }

    @Test
    void clienteNoPuedeEliminarBlogs() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> blogService.eliminar(1, false));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(blogRepository, never()).delete(any(Blog.class));
    }

    // METODOS AUXILIARES

    private Blog crearBlog(Integer id, String estado) {
        Usuario autor = new Usuario();
        autor.setNombre("Admin");
        autor.setApellidoPaterno("Isaflor");

        Blog blog = new Blog();
        blog.setIdBlog(id);
        blog.setTitulo("Cuidados de flores");
        blog.setContenido("Consejos para cuidar flores");
        blog.setFechaPublicacion(
                "PUBLICADO".equals(estado)
                        ? LocalDateTime.of(2026, 1, 1, 10, 0)
                        : null);
        blog.setEstado(estado);
        blog.setUsuario(autor);

        return blog;
    }

    private BlogRequestDTO crearRequest() {
        BlogRequestDTO request = new BlogRequestDTO();
        request.setTitulo("Nueva publicacion");
        request.setContenido("Contenido de la publicacion");

        return request;
    }

    private BlogEstadoRequestDTO crearEstadoRequest(String estado) {
        BlogEstadoRequestDTO request = new BlogEstadoRequestDTO();
        request.setEstado(estado);

        return request;
    }
}
