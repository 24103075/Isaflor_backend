package cl.isaflor.isaflor.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.VideoRequestDTO;
import cl.isaflor.isaflor.dto.VideoResponseDTO;
import cl.isaflor.isaflor.model.Blog;
import cl.isaflor.isaflor.model.Video;
import cl.isaflor.isaflor.repository.BlogRepository;
import cl.isaflor.isaflor.repository.VideoRepository;

@Service
@Transactional
public class VideoService {

    private final VideoRepository videoRepository;
    private final BlogRepository blogRepository;

    public VideoService(VideoRepository videoRepository, BlogRepository blogRepository) {
        this.videoRepository = videoRepository;
        this.blogRepository = blogRepository;
    }

    @Transactional(readOnly = true)
    public List<VideoResponseDTO> listarVideos(boolean administrador) {
        List<Video> videos = administrador
                ? videoRepository.findAllByOrderByIdVideoAsc()
                : videoRepository.findByBlog_EstadoOrderByIdVideoAsc("PUBLICADO");

        return videos.stream().map(this::convertirADTO).toList();
    }

    @Transactional(readOnly = true)
    public VideoResponseDTO buscarPorId(Integer id, boolean administrador) {
        Video video = buscarVideoEntidad(id);
        validarVisibilidad(video, administrador);
        return convertirADTO(video);
    }

    public VideoResponseDTO crearVideo(VideoRequestDTO datos, boolean administrador) {
        validarAdministrador(administrador);
        validarDatos(datos);

        Blog blog = buscarBlog(datos.getIdBlog());
        Video video = new Video();
        aplicarDatos(video, datos);
        video.setBlog(blog);

        return convertirADTO(videoRepository.save(video));
    }

    public VideoResponseDTO actualizarVideo(
            Integer id,
            VideoRequestDTO datos,
            boolean administrador) {
        validarAdministrador(administrador);
        validarDatos(datos);

        Video video = buscarVideoEntidad(id);
        Blog blog = buscarBlog(datos.getIdBlog());
        aplicarDatos(video, datos);
        video.setBlog(blog);

        return convertirADTO(videoRepository.save(video));
    }

    public String eliminarVideo(Integer id, boolean administrador) {
        validarAdministrador(administrador);
        Video video = buscarVideoEntidad(id);
        videoRepository.delete(video);
        return "Video eliminado correctamente";
    }

    private Video buscarVideoEntidad(Integer id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del video debe ser valido");
        }

        return videoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro el video con ID: " + id));
    }

    private Blog buscarBlog(Integer idBlog) {
        if (idBlog == null || idBlog <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del blog debe ser positivo");
        }

        return blogRepository.findById(idBlog)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro el blog con ID: " + idBlog));
    }

    private void validarDatos(VideoRequestDTO datos) {
        if (datos == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los datos del video son obligatorios");
        }
        if (datos.getIdBlog() == null || datos.getIdBlog() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del blog debe ser positivo");
        }
        if (datos.getTitulo() == null || datos.getTitulo().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El titulo del video es obligatorio");
        }
        if (datos.getTitulo().trim().length() > 150) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El titulo no puede superar los 150 caracteres");
        }
        if (datos.getUrlVideo() == null || datos.getUrlVideo().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La URL del video es obligatoria");
        }
        if (datos.getUrlVideo().trim().length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La URL no puede superar los 255 caracteres");
        }
        if (datos.getDescripcion() != null
                && datos.getDescripcion().trim().length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La descripcion no puede superar los 255 caracteres");
        }
    }

    private void validarAdministrador(boolean administrador) {
        if (!administrador) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo el administrador puede gestionar videos");
        }
    }

    private void validarVisibilidad(Video video, boolean administrador) {
        if (!administrador && (video.getBlog() == null
                || !"PUBLICADO".equals(video.getBlog().getEstado()))) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Video no encontrado");
        }
    }

    private void aplicarDatos(Video video, VideoRequestDTO datos) {
        video.setTitulo(datos.getTitulo().trim());
        video.setUrlVideo(datos.getUrlVideo().trim());
        video.setDescripcion(datos.getDescripcion() == null
                || datos.getDescripcion().isBlank()
                ? null
                : datos.getDescripcion().trim());
    }

    private VideoResponseDTO convertirADTO(Video video) {
        VideoResponseDTO dto = new VideoResponseDTO();
        dto.setIdVideo(video.getIdVideo());
        dto.setIdBlog(video.getBlog().getIdBlog());
        dto.setTitulo(video.getTitulo());
        dto.setUrlVideo(video.getUrlVideo());
        dto.setDescripcion(video.getDescripcion());
        return dto;
    }
}
