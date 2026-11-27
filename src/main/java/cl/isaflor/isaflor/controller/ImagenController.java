
package cl.isaflor.isaflor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cl.isaflor.isaflor.model.Imagen;
import cl.isaflor.isaflor.service.ImagenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/imagenes")
@Tag(name = "Imagenes", description = "Operaciones para la gestion de imagenes")
public class ImagenController {

    private final ImagenService imagenService;

    public ImagenController(ImagenService imagenService) {
        this.imagenService = imagenService;
    }

    // LISTAR IMAGENES
    @GetMapping
    @Operation(summary = "Listar imagenes",description = "Obtiene todas las imagenes registradas")
    @ApiResponse(responseCode = "200", description = "Imagenes obtenidas correctamente")
    @ApiResponse(responseCode = "204", description = "No existen imagenes registradas")
    public ResponseEntity<List<Imagen>> listarImagenes() {

        List<Imagen> imagenes = imagenService.listarImagenes();

        if (imagenes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(imagenes, HttpStatus.OK);
    }

    // BUSCAR IMAGEN POR ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar imagen por ID",description = "Obtiene una imagen segun su identificador")
    @ApiResponse(responseCode = "200", description = "Imagen encontrada")
    @ApiResponse(responseCode = "404", description = "Imagen no encontrada")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {

        try {
            Imagen imagen = imagenService.buscarPorId(id);
            return new ResponseEntity<>(imagen, HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // SUBIR IMAGEN
    @PostMapping(value = "/subir/{idProducto}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir imagen",description = "Sube una imagen a ImgBB y la asocia a un producto")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Imagen subida correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    public ResponseEntity<?> subirImagen(@PathVariable Integer idProducto,@RequestParam("archivo") MultipartFile archivo,
            @RequestParam(required = false) String descripcion) {

        try {
            Imagen imagen = imagenService.guardarImagen(idProducto, archivo, descripcion);

            return new ResponseEntity<>(imagen, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error al subir la imagen: " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    // ELIMINAR IMAGEN
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar imagen",description = "Elimina el registro de una imagen")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Imagen eliminada correctamente")
    @ApiResponse(responseCode = "404", description = "Imagen no encontrada")
    public ResponseEntity<?> eliminarImagen(@PathVariable Integer id) {

        try {
            String mensaje = imagenService.eliminarImagen(id);
            return new ResponseEntity<>(mensaje, HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error al eliminar: " + e.getMessage(),HttpStatus.NOT_FOUND);
        }
    }
}
