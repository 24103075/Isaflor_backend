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

import cl.isaflor.isaflor.model.MetodoPago;
import cl.isaflor.isaflor.service.MetodoPagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/metodos-pago")
@Tag(name = "Metodos de pago", description = "Operaciones para gestionar los metodos de pago")
public class MetodoPagoController {

    private final MetodoPagoService metodoPagoService;

    public MetodoPagoController(MetodoPagoService metodoPagoService) {
        this.metodoPagoService = metodoPagoService;
    }

    // LISTAR METODOS DE PAGO
    @GetMapping
    @Operation(summary = "Listar metodos de pago",description = "Los visitantes ven metodos activos y los administradores todos")
    @ApiResponse(responseCode = "200", description = "Metodos obtenidos correctamente")
    @ApiResponse(responseCode = "204", description = "No existen metodos de pago")
    public ResponseEntity<List<MetodoPago>> listar(Authentication authentication) {
        List<MetodoPago> metodos =
                metodoPagoService.listarMetodosPago(esAdministrador(authentication));

        if (metodos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(metodos, HttpStatus.OK);
    }

    // BUSCAR METODO DE PAGO POR ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar metodo de pago por ID")
    @ApiResponse(responseCode = "200", description = "Metodo de pago encontrado")
    @ApiResponse(responseCode = "404", description = "Metodo de pago no encontrado")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            MetodoPago metodo = metodoPagoService.buscarPorId(id, esAdministrador(authentication));
            return new ResponseEntity<>(metodo, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    // CREAR METODO DE PAGO
    @PostMapping
    @Operation(summary = "Crear metodo de pago",description = "Registra una nueva opcion de pago")
    @ApiResponse(responseCode = "201", description = "Metodo de pago creado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    public ResponseEntity<?> guardar(@Valid @RequestBody MetodoPago metodoPago) {
        try {
            MetodoPago guardado = metodoPagoService.guardarMetodoPago(metodoPago);
            return new ResponseEntity<>(guardado, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Error al guardar: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ACTUALIZAR METODO DE PAGO
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar metodo de pago",description = "Actualiza los datos o el estado de un metodo existente")
    @ApiResponse(responseCode = "200", description = "Metodo actualizado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "404", description = "Metodo no encontrado")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody MetodoPago metodoPago) {
        try {
            MetodoPago actualizado =
                    metodoPagoService.actualizarMetodoPago(id, metodoPago);
            return new ResponseEntity<>(actualizado, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Error al actualizar: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ELIMINAR METODO DE PAGO
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar metodo de pago",description = "Elimina un metodo que no este asociado a pagos")
    @ApiResponse(responseCode = "200", description = "Metodo eliminado correctamente")
    @ApiResponse(responseCode = "404", description = "Metodo no encontrado")
    @ApiResponse(responseCode = "409", description = "Metodo asociado a pagos")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            String mensaje = metodoPagoService.eliminarMetodoPago(id);
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
