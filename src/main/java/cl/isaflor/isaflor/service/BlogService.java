package cl.isaflor.isaflor.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.BlogEstadoRequestDTO;
import cl.isaflor.isaflor.dto.BlogRequestDTO;
import cl.isaflor.isaflor.dto.BlogResponseDTO;
import cl.isaflor.isaflor.model.Blog;
import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.repository.BlogRepository;
import cl.isaflor.isaflor.repository.ImagenBlogRepository;
import cl.isaflor.isaflor.repository.UsuarioRepository;
import cl.isaflor.isaflor.repository.VideoRepository;

@Service
@Transactional
public class BlogService {

    private final BlogRepository blogRepository;
    private final UsuarioRepository usuarioRepository;
    private final ImagenBlogRepository imagenBlogRepository;
    private final VideoRepository videoRepository;

    public BlogService(
            BlogRepository blogRepository,
            UsuarioRepository usuarioRepository,
            ImagenBlogRepository imagenBlogRepository,
            VideoRepository videoRepository) {
        this.blogRepository = blogRepository;
        this.usuarioRepository = usuarioRepository;
        this.imagenBlogRepository = imagenBlogRepository;
        this.videoRepository = videoRepository;
    }

    @Transactional(readOnly = true)
    public List<BlogResponseDTO> listarBlogs(boolean administrador) {
        List<Blog> blogs = administrador
                ? blogRepository.findAllByOrderByFechaPublicacionDesc()
                : blogRepository.findByEstadoOrderByFechaPublicacionDesc("PUBLICADO");

        return blogs.stream().map(this::convertirADTO).toList();
    }

    @Transactional(readOnly = true)
    public BlogResponseDTO buscarPorId(Integer id, boolean administrador) {
        Blog blog = buscarEntidad(id);

        if (!administrador && !"PUBLICADO".equals(blog.getEstado())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog no encontrado");
        }

        return convertirADTO(blog);
    }

    public BlogResponseDTO crear(
            BlogRequestDTO datos,
            String correoAutenticado,
            boolean administrador) {
        validarAdministrador(administrador);

        Usuario autor = usuarioRepository.findByCorreoIgnoreCase(correoAutenticado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "El usuario autenticado no existe"));

        Blog blog = new Blog();
        aplicarDatos(blog, datos);
        blog.setFechaPublicacion(null);
        blog.setEstado("BORRADOR");
        blog.setUsuario(autor);

        return convertirADTO(blogRepository.save(blog));
    }

    public BlogResponseDTO actualizar(
            Integer id,
            BlogRequestDTO datos,
            boolean administrador) {
        validarAdministrador(administrador);

        Blog blog = buscarEntidad(id);
        aplicarDatos(blog, datos);

        return convertirADTO(blogRepository.save(blog));
    }

   public BlogResponseDTO cambiarEstado(
        Integer id,
        BlogEstadoRequestDTO datos,
        boolean administrador) {

    validarAdministrador(administrador);

    Blog blog = buscarEntidad(id);
    String estado = datos.getEstado().trim().toUpperCase(Locale.ROOT);

    if (!List.of("BORRADOR", "PUBLICADO").contains(estado)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El estado debe ser BORRADOR o PUBLICADO");
    }

    if ("PUBLICADO".equals(estado)) {
        if (!"PUBLICADO".equals(blog.getEstado())) {
            blog.setFechaPublicacion(LocalDateTime.now());
        }
    } else {
        blog.setFechaPublicacion(null);
    }

    blog.setEstado(estado);

    return convertirADTO(blogRepository.save(blog));
}
    public void eliminar(Integer id, boolean administrador) {
        validarAdministrador(administrador);

        Blog blog = buscarEntidad(id);
        if (imagenBlogRepository.existsByBlog_IdBlog(id)
                || videoRepository.existsByBlog_IdBlog(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el blog porque tiene imagenes o videos asociados");
        }

        blogRepository.delete(blog);
    }

    private Blog buscarEntidad(Integer id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontro el blog con ID: " + id));
    }

    private void aplicarDatos(Blog blog, BlogRequestDTO datos) {
        blog.setTitulo(datos.getTitulo().trim());
        blog.setContenido(datos.getContenido().trim());
    }

    private void validarAdministrador(boolean administrador) {
        if (!administrador) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Solo el administrador puede modificar blogs");
        }
    }

    private BlogResponseDTO convertirADTO(Blog blog) {
        BlogResponseDTO dto = new BlogResponseDTO();
        dto.setIdBlog(blog.getIdBlog());
        dto.setTitulo(blog.getTitulo());
        dto.setContenido(blog.getContenido());
        dto.setFechaPublicacion(blog.getFechaPublicacion());
        dto.setEstado(blog.getEstado());
        return dto;
    }
}
