package cl.isaflor.isaflor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import cl.isaflor.isaflor.dto.VideoRequestDTO;
import cl.isaflor.isaflor.dto.VideoResponseDTO;
import cl.isaflor.isaflor.model.Blog;
import cl.isaflor.isaflor.model.Video;
import cl.isaflor.isaflor.repository.BlogRepository;
import cl.isaflor.isaflor.repository.VideoRepository;
import cl.isaflor.isaflor.service.VideoService;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private BlogRepository blogRepository;

    @InjectMocks
    private VideoService videoService;

    @Test
    void listarVideosFiltraBorradoresParaVisitantes() {
        Video videoPublicado = crearVideo(1, crearBlog(10, "PUBLICADO"));
        Video videoBorrador = crearVideo(2, crearBlog(11, "BORRADOR"));
        when(videoRepository.findByBlog_EstadoOrderByIdVideoAsc("PUBLICADO"))
                .thenReturn(List.of(videoPublicado));
        when(videoRepository.findAllByOrderByIdVideoAsc())
                .thenReturn(List.of(videoPublicado, videoBorrador));

        List<VideoResponseDTO> publicos = videoService.listarVideos(false);
        List<VideoResponseDTO> todos = videoService.listarVideos(true);

        assertEquals(1, publicos.size());
        assertEquals(1, publicos.get(0).getIdVideo());
        assertEquals(2, todos.size());
        verify(videoRepository)
                .findByBlog_EstadoOrderByIdVideoAsc("PUBLICADO");
        verify(videoRepository).findAllByOrderByIdVideoAsc();
    }

    @Test
    void buscarPorIdOcultaVideosDeBlogsNoPublicados() {
        when(videoRepository.findById(1))
                .thenReturn(Optional.of(crearVideo(1, crearBlog(10, "PUBLICADO"))));
        when(videoRepository.findById(2))
                .thenReturn(Optional.of(crearVideo(2, crearBlog(11, "BORRADOR"))));

        VideoResponseDTO publico = videoService.buscarPorId(1, false);
        assertEquals(1, publico.getIdVideo());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> videoService.buscarPorId(2, false));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        assertEquals(2, videoService.buscarPorId(2, true).getIdVideo());
    }

    @Test
    void crearVideoValidaAdminYAsociaBlog() {
        VideoRequestDTO request = crearSolicitud(10);
        request.setTitulo("  Tutorial de cuidado  ");
        request.setUrlVideo("  https://videos.example/cl/cuidado  ");
        request.setDescripcion("  Consejos  ");
        Blog blog = crearBlog(10, "BORRADOR");
        when(blogRepository.findById(10)).thenReturn(Optional.of(blog));
        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> {
            Video video = invocation.getArgument(0);
            video.setIdVideo(3);
            return video;
        });

        VideoResponseDTO resultado = videoService.crearVideo(request, true);

        assertEquals(3, resultado.getIdVideo());
        assertEquals(10, resultado.getIdBlog());
        assertEquals("Tutorial de cuidado", resultado.getTitulo());
        assertEquals("https://videos.example/cl/cuidado", resultado.getUrlVideo());
        assertEquals("Consejos", resultado.getDescripcion());
        verify(videoRepository).save(any(Video.class));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> videoService.crearVideo(request, false));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void actualizarVideoActualizaDatosYBlogAsociado() {
        Blog blogActual = crearBlog(10, "BORRADOR");
        Blog blogNuevo = crearBlog(12, "PUBLICADO");
        Video existente = crearVideo(3, blogActual);
        VideoRequestDTO request = crearSolicitud(12);
        request.setTitulo("Video actualizado");
        request.setUrlVideo("https://videos.example/cl/nuevo");
        request.setDescripcion(null);
        when(videoRepository.findById(3)).thenReturn(Optional.of(existente));
        when(blogRepository.findById(12)).thenReturn(Optional.of(blogNuevo));
        when(videoRepository.save(existente)).thenReturn(existente);

        VideoResponseDTO resultado = videoService.actualizarVideo(3, request, true);

        assertEquals(12, resultado.getIdBlog());
        assertEquals("Video actualizado", resultado.getTitulo());
        assertEquals("https://videos.example/cl/nuevo", resultado.getUrlVideo());
        assertEquals(null, resultado.getDescripcion());
        verify(videoRepository).save(existente);
    }

    @Test
    void eliminarVideoSoloPermiteAlAdministrador() {
        Video video = crearVideo(3, crearBlog(10, "PUBLICADO"));
        when(videoRepository.findById(3)).thenReturn(Optional.of(video));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> videoService.eliminarVideo(3, false));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        assertEquals("Video eliminado correctamente",
                videoService.eliminarVideo(3, true));
        verify(videoRepository).delete(video);
    }

    private VideoRequestDTO crearSolicitud(Integer idBlog) {
        VideoRequestDTO request = new VideoRequestDTO();
        request.setIdBlog(idBlog);
        request.setTitulo("Cuidado de flores");
        request.setUrlVideo("https://videos.example/cl/flores");
        return request;
    }

    private Video crearVideo(Integer id, Blog blog) {
        Video video = new Video();
        video.setIdVideo(id);
        video.setBlog(blog);
        video.setTitulo("Cuidado de flores");
        video.setUrlVideo("https://videos.example/cl/flores");
        video.setDescripcion("Consejos de cuidado");
        return video;
    }

    private Blog crearBlog(Integer id, String estado) {
        Blog blog = new Blog();
        blog.setIdBlog(id);
        blog.setEstado(estado);
        return blog;
    }
}
