package cl.isaflor.isaflor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.isaflor.isaflor.dto.BlogEstadoRequestDTO;
import cl.isaflor.isaflor.dto.BlogRequestDTO;
import cl.isaflor.isaflor.dto.BlogResponseDTO;
import cl.isaflor.isaflor.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/blogs")
@Tag(name = "Blogs", description = "Operaciones para la gestion de publicaciones")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    // LISTAR BLOGS
    @GetMapping
    @Operation(summary = "Listar blogs",
            description = "Los visitantes ven publicados y los administradores todos los estados")
    @ApiResponse(responseCode = "200", description = "Blogs obtenidos correctamente")
    @ApiResponse(responseCode = "204", description = "No existen blogs")
    public ResponseEntity<List<BlogResponseDTO>> listar(Authentication authentication) {
        List<BlogResponseDTO> blogs =
                blogService.listarBlogs(esAdministrador(authentication));

        if (blogs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(blogs, HttpStatus.OK);
    }

    // BUSCAR BLOG POR ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar blog por ID",
            description = "Obtiene un blog publicado; el administrador también puede consultar borradores")
    @ApiResponse(responseCode = "200", description = "Blog encontrado")
    @ApiResponse(responseCode = "404", description = "Blog no encontrado")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            BlogResponseDTO blog = blogService.buscarPorId(id, esAdministrador(authentication));
            return new ResponseEntity<>(blog, HttpStatus.OK);

        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    // CREAR BLOG
    @PostMapping
    @Operation(summary = "Crear blog",
            description = "Crea un borrador y asigna el autor desde el usuario autenticado")
    @ApiResponse(responseCode = "201", description = "Blog creado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede crear blogs")
    public ResponseEntity<?> crear(
            @Valid @RequestBody BlogRequestDTO datos,
            Authentication authentication) {
        try {
            String correo = authentication == null ? null : authentication.getName();
            BlogResponseDTO creado = blogService.crear(
                    datos, correo, esAdministrador(authentication));
            return new ResponseEntity<>(creado, HttpStatus.CREATED);

        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    // ACTUALIZAR BLOG
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar blog",
            description = "Actualiza el titulo y contenido sin cambiar estado ni autor")
    @ApiResponse(responseCode = "200", description = "Blog actualizado correctamente")
    @ApiResponse(responseCode = "404", description = "Blog no encontrado")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede actualizar blogs")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody BlogRequestDTO datos,
            Authentication authentication) {
        try {
            BlogResponseDTO actualizado = blogService.actualizar(
                    id, datos, esAdministrador(authentication));
            return new ResponseEntity<>(actualizado, HttpStatus.OK);

        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    // CAMBIAR ESTADO
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado del blog",
            description = "Publica o devuelve a borrador un blog")
    @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente")
    @ApiResponse(responseCode = "400", description = "Estado no permitido")
    @ApiResponse(responseCode = "404", description = "Blog no encontrado")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede cambiar el estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody BlogEstadoRequestDTO datos,
            Authentication authentication) {
        try {
            BlogResponseDTO actualizado = blogService.cambiarEstado(
                    id, datos, esAdministrador(authentication));
            return new ResponseEntity<>(actualizado, HttpStatus.OK);

        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    // ELIMINAR BLOG
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar blog",
            description = "Solo permite eliminar blogs sin imagenes ni videos asociados")
    @ApiResponse(responseCode = "204", description = "Blog eliminado correctamente")
    @ApiResponse(responseCode = "404", description = "Blog no encontrado")
    @ApiResponse(responseCode = "409", description = "El blog tiene contenido asociado")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede eliminar blogs")
    public ResponseEntity<?> eliminar(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            blogService.eliminar(id, esAdministrador(authentication));
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

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
