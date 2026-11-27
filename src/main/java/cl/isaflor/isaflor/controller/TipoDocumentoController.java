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

import cl.isaflor.isaflor.dto.TipoDocumentoDTO;
import cl.isaflor.isaflor.model.TipoDocumento;
import cl.isaflor.isaflor.service.TipoDocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tipos-documento")
@Tag(name = "Tipos de documento",
        description = "Operaciones para gestionar tipos de documento")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    public TipoDocumentoController(TipoDocumentoService tipoDocumentoService) {
        this.tipoDocumentoService = tipoDocumentoService;
    }

    @GetMapping
    @Operation(summary = "Listar tipos de documento")
    @ApiResponse(responseCode = "200", description = "Tipos de documento obtenidos")
    @ApiResponse(responseCode = "204", description = "No existen tipos de documento")
    public ResponseEntity<List<TipoDocumentoDTO>> listar() {
        List<TipoDocumentoDTO> tiposDocumento =
                tipoDocumentoService.listarTiposDocumento();

        if (tiposDocumento.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tiposDocumento, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tipo de documento por ID")
    @ApiResponse(responseCode = "200", description = "Tipo de documento encontrado")
    @ApiResponse(responseCode = "404", description = "Tipo de documento no encontrado")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return new ResponseEntity<>(
                    tipoDocumentoService.buscarPorId(id), HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    @PostMapping
    @Operation(summary = "Crear tipo de documento")
    @ApiResponse(responseCode = "201", description = "Tipo de documento creado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "409", description = "Nombre duplicado")
    public ResponseEntity<?> guardar(@Valid @RequestBody TipoDocumento datos) {
        try {
            TipoDocumentoDTO creado =
                    tipoDocumentoService.guardarTipoDocumento(datos);
            return new ResponseEntity<>(creado, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    "Error al guardar: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de documento")
    @ApiResponse(responseCode = "200", description = "Tipo de documento actualizado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "404", description = "Tipo de documento no encontrado")
    @ApiResponse(responseCode = "409", description = "Nombre duplicado")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody TipoDocumento datos) {
        try {
            TipoDocumentoDTO actualizado =
                    tipoDocumentoService.actualizarTipoDocumento(id, datos);
            return new ResponseEntity<>(actualizado, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    "Error al actualizar: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de documento")
    @ApiResponse(responseCode = "200", description = "Tipo de documento eliminado")
    @ApiResponse(responseCode = "404", description = "Tipo de documento no encontrado")
    @ApiResponse(responseCode = "409", description = "Tipo asociado a pedidos")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            return new ResponseEntity<>(
                    tipoDocumentoService.eliminarTipoDocumento(id), HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }
}
