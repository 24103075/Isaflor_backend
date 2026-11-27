package cl.isaflor.isaflor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClientException;

import cl.isaflor.isaflor.dto.ImagenBlogResponseDTO;
import cl.isaflor.isaflor.service.ImagenBlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/imagenes-blog")
@Tag(name = "Imagenes de blog", description = "Operaciones para gestionar imagenes de publicaciones")
public class ImagenBlogController {

    private final ImagenBlogService imagenBlogService;

    public ImagenBlogController(ImagenBlogService imagenBlogService) {
        this.imagenBlogService = imagenBlogService;
    }

    // LISTAR IMAGENES DE BLOG
    @GetMapping
    @Operation(summary = "Listar imagenes de blog",
            description = "Los visitantes ven imagenes de blogs publicados; el administrador ve todas")
    @ApiResponse(responseCode = "200", description = "Imagenes obtenidas correctamente")
    @ApiResponse(responseCode = "204", description = "No existen imagenes")
    public ResponseEntity<List<ImagenBlogResponseDTO>> listar(Authentication authentication) {
        List<ImagenBlogResponseDTO> imagenes =
                imagenBlogService.listarImagenes(esAdministrador(authentication));

        if (imagenes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(imagenes, HttpStatus.OK);
    }

    // BUSCAR IMAGEN DE BLOG POR ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar imagen de blog por ID")
    @ApiResponse(responseCode = "200", description = "Imagen encontrada")
    @ApiResponse(responseCode = "404", description = "Imagen no encontrada")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            ImagenBlogResponseDTO imagen =
                    imagenBlogService.buscarPorId(id, esAdministrador(authentication));
            return new ResponseEntity<>(imagen, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    // SUBIR IMAGEN DE BLOG
    @PostMapping(value = "/subir/{idBlog}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir imagen de blog",
            description = "Sube una imagen a ImgBB y la asocia a una publicacion")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Imagen subida correctamente")
    @ApiResponse(responseCode = "400", description = "Datos o archivo invalidos")
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede subir imagenes")
    @ApiResponse(responseCode = "404", description = "Blog no encontrado")
    @ApiResponse(responseCode = "502", description = "ImgBB no disponible")

    public ResponseEntity<?> subirImagen(
            @PathVariable Integer idBlog,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(required = false) String descripcion,
            Authentication authentication) {
        try {
            ImagenBlogResponseDTO imagen = imagenBlogService.guardarImagen(
                    idBlog, archivo, descripcion, esAdministrador(authentication));
            return new ResponseEntity<>(imagen, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RestClientException e) {
            return new ResponseEntity<>("No fue posible subir la imagen a ImgBB",
                    HttpStatus.BAD_GATEWAY);
        }
    }

    // ELIMINAR IMAGEN DE BLOG
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar imagen de blog",description = "Elimina el registro de la imagen asociado a un blog")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Imagen eliminada correctamente")
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede eliminar imagenes")
    @ApiResponse(responseCode = "404", description = "Imagen no encontrada")
    public ResponseEntity<?> eliminarImagen(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            String mensaje = imagenBlogService.eliminarImagen(
                    id, esAdministrador(authentication));
            return new ResponseEntity<>(mensaje, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    private boolean esAdministrador(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().contains(
                        new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
    }
}
