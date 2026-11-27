package cl.isaflor.isaflor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.VideoRequestDTO;
import cl.isaflor.isaflor.dto.VideoResponseDTO;
import cl.isaflor.isaflor.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/videos")
@Tag(name = "Videos", description = "Operaciones para gestionar videos asociados a blogs")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping
    @Operation(summary = "Listar videos de blogs publicados")
    @ApiResponse(responseCode = "200", description = "Videos obtenidos")
    @ApiResponse(responseCode = "204", description = "No existen videos")
    public ResponseEntity<List<VideoResponseDTO>> listar(Authentication authentication) {
        List<VideoResponseDTO> videos =
                videoService.listarVideos(esAdministrador(authentication));
        if (videos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar video por ID")
    @ApiResponse(responseCode = "200", description = "Video encontrado")
    @ApiResponse(responseCode = "404", description = "Video no encontrado")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            VideoResponseDTO video = videoService.buscarPorId(
                    id, esAdministrador(authentication));
            return new ResponseEntity<>(video, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    @PostMapping
    @Operation(summary = "Asociar un video a un blog")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Video creado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede gestionar videos")
    @ApiResponse(responseCode = "404", description = "Blog no encontrado")
    public ResponseEntity<?> crear(
            @Valid @RequestBody VideoRequestDTO request,
            Authentication authentication) {
        try {
            VideoResponseDTO creado = videoService.crearVideo(
                    request, esAdministrador(authentication));
            return new ResponseEntity<>(creado, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar los datos de un video")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Video actualizado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede gestionar videos")
    @ApiResponse(responseCode = "404", description = "Video o blog no encontrado")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody VideoRequestDTO request,
            Authentication authentication) {
        try {
            VideoResponseDTO actualizado = videoService.actualizarVideo(
                    id, request, esAdministrador(authentication));
            return new ResponseEntity<>(actualizado, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un video")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Video eliminado")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede gestionar videos")
    @ApiResponse(responseCode = "404", description = "Video no encontrado")
    public ResponseEntity<?> eliminar(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            String mensaje = videoService.eliminarVideo(
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
