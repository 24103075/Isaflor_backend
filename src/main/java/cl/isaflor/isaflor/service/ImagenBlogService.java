package cl.isaflor.isaflor.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.ImagenBlogResponseDTO;
import cl.isaflor.isaflor.model.Blog;
import cl.isaflor.isaflor.model.ImagenBlog;
import cl.isaflor.isaflor.repository.BlogRepository;
import cl.isaflor.isaflor.repository.ImagenBlogRepository;

@Service
@Transactional
public class ImagenBlogService {

    private final ImagenBlogRepository imagenBlogRepository;
    private final BlogRepository blogRepository;
    private final ImgBBService imgBBService;

    public ImagenBlogService(
            ImagenBlogRepository imagenBlogRepository,
            BlogRepository blogRepository,
            ImgBBService imgBBService) {
        this.imagenBlogRepository = imagenBlogRepository;
        this.blogRepository = blogRepository;
        this.imgBBService = imgBBService;
    }

    @Transactional(readOnly = true)
    public List<ImagenBlogResponseDTO> listarImagenes(boolean administrador) {
        List<ImagenBlog> imagenes = administrador
                ? imagenBlogRepository.findAllByOrderByIdImagenBlogAsc()
                : imagenBlogRepository.findByBlog_EstadoOrderByIdImagenBlogAsc("PUBLICADO");

        return imagenes.stream().map(this::convertirADTO).toList();
    }

    @Transactional(readOnly = true)
    public ImagenBlogResponseDTO buscarPorId(Integer id, boolean administrador) {
        ImagenBlog imagen = buscarImagenEntidad(id);
        validarVisibilidad(imagen, administrador);
        return convertirADTO(imagen);
    }

    public ImagenBlogResponseDTO guardarImagen(
            Integer idBlog,
            MultipartFile archivo,
            String descripcion,
            boolean administrador) {
        validarAdministrador(administrador);

        if (idBlog == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el blog");
        }
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar una imagen");
        }
        if (archivo.getOriginalFilename() == null
                || archivo.getOriginalFilename().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El nombre del archivo es obligatorio");
        }
        if (descripcion != null && descripcion.length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La descripcion no puede superar los 255 caracteres");
        }

        Blog blog = blogRepository.findById(idBlog)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontro el blog con ID: " + idBlog));

        String url = imgBBService.subirImagen(archivo);

        ImagenBlog imagen = new ImagenBlog();
        imagen.setBlog(blog);
        imagen.setNombre(archivo.getOriginalFilename());
        imagen.setUrlImagen(url);
        imagen.setDescripcion(descripcion == null || descripcion.isBlank()
                ? null
                : descripcion.trim());

        return convertirADTO(imagenBlogRepository.save(imagen));
    }

    public String eliminarImagen(Integer id, boolean administrador) {
        validarAdministrador(administrador);
        ImagenBlog imagen = buscarImagenEntidad(id);
        imagenBlogRepository.delete(imagen);
        return "Imagen del blog eliminada correctamente";
    }

    private ImagenBlog buscarImagenEntidad(Integer id) {
        return imagenBlogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro la imagen del blog con ID: " + id));
    }

    private void validarAdministrador(boolean administrador) {
        if (!administrador) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Solo el administrador puede gestionar imagenes de blogs");
        }
    }

    private void validarVisibilidad(ImagenBlog imagen, boolean administrador) {
        if (!administrador && (imagen.getBlog() == null
                || !"PUBLICADO".equals(imagen.getBlog().getEstado()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen no encontrada");
        }
    }

    private ImagenBlogResponseDTO convertirADTO(ImagenBlog imagen) {
        ImagenBlogResponseDTO dto = new ImagenBlogResponseDTO();
        dto.setIdImagenBlog(imagen.getIdImagenBlog());
        dto.setNombre(imagen.getNombre());
        dto.setUrlImagen(imagen.getUrlImagen());
        dto.setDescripcion(imagen.getDescripcion());
        dto.setIdBlog(imagen.getBlog().getIdBlog());
        return dto;
    }
}
