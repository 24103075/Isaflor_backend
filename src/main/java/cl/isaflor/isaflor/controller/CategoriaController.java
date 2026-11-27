
package cl.isaflor.isaflor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.isaflor.isaflor.dto.CategoriaDTO;
import cl.isaflor.isaflor.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorias", description = "Operaciones para la consulta de categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    // Constructor
    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // LISTAR CATEGORIAS
    @GetMapping
    @Operation(
        summary = "Listar categorias",
        description = "Obtiene todas las categorias registradas"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Categorias obtenidas correctamente"
    )
    @ApiResponse(
        responseCode = "204",
        description = "No existen categorias registradas"
    )
    public ResponseEntity<List<CategoriaDTO>> listar() {

        List<CategoriaDTO> categorias =
                categoriaService.listarCategorias();

        if (categorias.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(categorias, HttpStatus.OK);
    }
}
