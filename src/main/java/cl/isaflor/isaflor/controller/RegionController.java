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

import cl.isaflor.isaflor.dto.RegionDTO;
import cl.isaflor.isaflor.model.Region;
import cl.isaflor.isaflor.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/regiones")
@Tag(name = "Regiones", description = "Operaciones para gestionar regiones")
public class RegionController {

    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @GetMapping
    @Operation(summary = "Listar regiones")
    @ApiResponse(responseCode = "200", description = "Regiones obtenidas")
    @ApiResponse(responseCode = "204", description = "No existen regiones")
    public ResponseEntity<List<RegionDTO>> listar() {
        List<RegionDTO> regiones = regionService.listarRegiones();

        if (regiones.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(regiones, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar region por ID")
    @ApiResponse(responseCode = "200", description = "Region encontrada")
    @ApiResponse(responseCode = "404", description = "Region no encontrada")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return new ResponseEntity<>(regionService.buscarPorId(id), HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    @PostMapping
    @Operation(summary = "Crear region")
    @ApiResponse(responseCode = "201", description = "Region creada")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "409", description = "Nombre duplicado")
    public ResponseEntity<?> guardar(@Valid @RequestBody Region datos) {
        try {
            RegionDTO creada = regionService.guardarRegion(datos);
            return new ResponseEntity<>(creada, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    "Error al guardar: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar region")
    @ApiResponse(responseCode = "200", description = "Region actualizada")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "404", description = "Region no encontrada")
    @ApiResponse(responseCode = "409", description = "Nombre duplicado")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody Region datos) {
        try {
            RegionDTO actualizada = regionService.actualizarRegion(id, datos);
            return new ResponseEntity<>(actualizada, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    "Error al actualizar: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar region")
    @ApiResponse(responseCode = "200", description = "Region eliminada")
    @ApiResponse(responseCode = "404", description = "Region no encontrada")
    @ApiResponse(responseCode = "409", description = "Region asociada a comunas")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            return new ResponseEntity<>(regionService.eliminarRegion(id), HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }
}
