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

import cl.isaflor.isaflor.model.MetodoEnvio;
import cl.isaflor.isaflor.service.MetodoEnvioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/metodos-envio")
@Tag(name = "Metodos de envio", description = "Operaciones para gestionar los metodos de envio")
public class MetodoEnvioController {

    private final MetodoEnvioService metodoEnvioService;

    public MetodoEnvioController(MetodoEnvioService metodoEnvioService) {
        this.metodoEnvioService = metodoEnvioService;
    }

    // LISTAR METODOS DE ENVIO
    @GetMapping
    @Operation(summary = "Listar metodos de envio",description = "Obtiene todos los metodos de envio disponibles")
    @ApiResponse(responseCode = "200", description = "Metodos obtenidos correctamente")
    @ApiResponse(responseCode = "204", description = "No existen metodos de envio")
    public ResponseEntity<List<MetodoEnvio>> listar() {
        List<MetodoEnvio> metodos = metodoEnvioService.listarMetodosEnvio();

        if (metodos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(metodos, HttpStatus.OK);
    }

    // BUSCAR METODO DE ENVIO POR ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar metodo de envio por ID")
    @ApiResponse(responseCode = "200", description = "Metodo de envio encontrado")
    @ApiResponse(responseCode = "404", description = "Metodo de envio no encontrado")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return new ResponseEntity<>(metodoEnvioService.buscarPorId(id), HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    // CREAR METODO DE ENVIO
    @PostMapping
    @Operation(summary = "Crear metodo de envio",description = "Registra una nueva opcion de envio")
    @ApiResponse(responseCode = "201", description = "Metodo de envio creado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    public ResponseEntity<?> guardar(@Valid @RequestBody MetodoEnvio metodoEnvio) {
        try {
            MetodoEnvio guardado = metodoEnvioService.guardarMetodoEnvio(metodoEnvio);
            return new ResponseEntity<>(guardado, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Error al guardar: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ACTUALIZAR METODO DE ENVIO
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar metodo de envio",description = "Actualiza los datos de un metodo existente")
    @ApiResponse(responseCode = "200", description = "Metodo actualizado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "404", description = "Metodo no encontrado")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody MetodoEnvio metodoEnvio) {
        try {
            MetodoEnvio actualizado =
                    metodoEnvioService.actualizarMetodoEnvio(id, metodoEnvio);
            return new ResponseEntity<>(actualizado, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Error al actualizar: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ELIMINAR METODO DE ENVIO
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar metodo de envio",description = "Elimina un metodo que no este asociado a pedidos")
    @ApiResponse(responseCode = "200", description = "Metodo eliminado correctamente")
    @ApiResponse(responseCode = "404", description = "Metodo no encontrado")
    @ApiResponse(responseCode = "409", description = "Metodo asociado a pedidos")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            String mensaje = metodoEnvioService.eliminarMetodoEnvio(id);
            return new ResponseEntity<>(mensaje, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }
}
