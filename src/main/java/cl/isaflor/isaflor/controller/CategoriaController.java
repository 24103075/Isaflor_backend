
package cl.isaflor.isaflor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.CategoriaDTO;
import cl.isaflor.isaflor.model.Categoria;
import cl.isaflor.isaflor.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorias", description = "Operaciones para gestionar las categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // LISTAR CATEGORIAS
    @GetMapping
    @Operation(summary = "Listar categorias",
            description = "Obtiene todas las categorias registradas")
    @ApiResponse(responseCode = "200", description = "Categorias obtenidas correctamente")
    @ApiResponse(responseCode = "204", description = "No existen categorias registradas")
    public ResponseEntity<List<CategoriaDTO>> listar() {
        List<CategoriaDTO> categorias = categoriaService.listarCategorias();

        if (categorias.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(categorias, HttpStatus.OK);
    }

    // BUSCAR CATEGORIA POR ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    @ApiResponse(responseCode = "200", description = "Categoria encontrada")
    @ApiResponse(responseCode = "404", description = "Categoria no encontrada")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            CategoriaDTO categoria = categoriaService.buscarPorId(id);
            return new ResponseEntity<>(categoria, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    // CREAR CATEGORIA
    @PostMapping
    @Operation(summary = "Crear categoria",
            description = "Registra una nueva categoria")
    @ApiResponse(responseCode = "201", description = "Categoria creada correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "409", description = "Nombre de categoria duplicado")
    public ResponseEntity<?> guardar(@Valid @RequestBody Categoria categoria) {
        try {
            CategoriaDTO creada = categoriaService.guardarCategoria(categoria);
            return new ResponseEntity<>(creada, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Error al guardar: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ACTUALIZAR CATEGORIA
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoria",
            description = "Actualiza el nombre y la descripcion de una categoria")
    @ApiResponse(responseCode = "200", description = "Categoria actualizada correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "404", description = "Categoria no encontrada")
    @ApiResponse(responseCode = "409", description = "Nombre de categoria duplicado")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody Categoria categoria) {
        try {
            CategoriaDTO actualizada =
                    categoriaService.actualizarCategoria(id, categoria);
            return new ResponseEntity<>(actualizada, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Error al actualizar: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ELIMINAR CATEGORIA
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoria",
            description = "Elimina una categoria sin productos asociados")
    @ApiResponse(responseCode = "200", description = "Categoria eliminada correctamente")
    @ApiResponse(responseCode = "404", description = "Categoria no encontrada")
    @ApiResponse(responseCode = "409", description = "Categoria asociada a productos")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            String mensaje = categoriaService.eliminarCategoria(id);
            return new ResponseEntity<>(mensaje, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }
}
