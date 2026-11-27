
package cl.isaflor.isaflor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.isaflor.isaflor.model.Comuna;
import cl.isaflor.isaflor.service.ComunaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/comunas")
@Tag(name = "Comunas", description = "Operaciones para la gestion de comunas")
public class ComunaController {

    private final ComunaService comunaService;

    public ComunaController(ComunaService comunaService) {
        this.comunaService = comunaService;
    }

    // LISTAR COMUNAS
    @GetMapping
    @Operation(summary = "Listar comunas",
            description = "Obtiene todas las comunas registradas")
    @ApiResponse(responseCode = "200", description = "Comunas obtenidas correctamente")
    @ApiResponse(responseCode = "204", description = "No existen comunas registradas")
    public ResponseEntity<List<Comuna>> listarComunas() {

        List<Comuna> comunas = comunaService.listarComunas();

        if (comunas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(comunas, HttpStatus.OK);
    }

    // BUSCAR COMUNA POR ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar comuna por ID",
            description = "Obtiene una comuna segun su identificador")
    @ApiResponse(responseCode = "200", description = "Comuna encontrada")
    @ApiResponse(responseCode = "404", description = "Comuna no encontrada")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {

        try {
            Comuna comuna = comunaService.buscarPorId(id);
            return new ResponseEntity<>(comuna, HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // GUARDAR COMUNA
    @PostMapping
    @Operation(summary = "Crear comuna",
            description = "Registra una nueva comuna")
    @ApiResponse(responseCode = "201", description = "Comuna creada correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    public ResponseEntity<?> guardarComuna(@Valid @RequestBody Comuna comuna) {

        try {
            Comuna comunaGuardada = comunaService.guardarComuna(comuna);
            return new ResponseEntity<>(comunaGuardada, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error al guardar: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ACTUALIZAR COMUNA
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar comuna",description = "Actualiza los datos de una comuna existente")
    @ApiResponse(responseCode = "200", description = "Comuna actualizada correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "404", description = "Comuna no encontrada")
    public ResponseEntity<?> actualizarComuna(
            @PathVariable Integer id,
            @Valid @RequestBody Comuna comuna) {

        try {
            Comuna actualizada = comunaService.actualizarComuna(id, comuna);
            return new ResponseEntity<>(actualizada, HttpStatus.OK);

        } catch (RuntimeException e) {

            if (e.getMessage() != null &&
                    e.getMessage().startsWith("No se encontro la comuna")) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(
                    "Error al actualizar: " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    // ELIMINAR COMUNA
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar comuna",description = "Elimina una comuna segun su identificador")
    @ApiResponse(responseCode = "200", description = "Comuna eliminada correctamente")
    @ApiResponse(responseCode = "404", description = "Comuna no encontrada")
    public ResponseEntity<?> eliminarComuna(@PathVariable Integer id) {

        try {
            String mensaje = comunaService.eliminarComuna(id);
            return new ResponseEntity<>(mensaje, HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error al eliminar: " + e.getMessage(),HttpStatus.NOT_FOUND);
        }
    }
}
