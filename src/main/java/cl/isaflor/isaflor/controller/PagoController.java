package cl.isaflor.isaflor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.ActualizarEstadoPagoRequest;
import cl.isaflor.isaflor.dto.CrearPagoRequest;
import cl.isaflor.isaflor.dto.PagoDTO;
import cl.isaflor.isaflor.dto.PagoEstadoHistorialDTO;
import cl.isaflor.isaflor.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/pagos")
@Tag(name = "Pagos", description = "Operaciones para consultar y gestionar pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    @Operation(summary = "Listar pagos")
    @ApiResponse(responseCode = "200", description = "Pagos obtenidos")
    @ApiResponse(responseCode = "204", description = "No existen pagos")
    public ResponseEntity<List<PagoDTO>> listar(Authentication authentication) {
        List<PagoDTO> pagos = pagoService.listarPagos(
                authentication.getName(), esAdministrador(authentication));
        if (pagos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(pagos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pago por ID")
    @ApiResponse(responseCode = "200", description = "Pago encontrado")
    @ApiResponse(responseCode = "403", description = "No autorizado para consultar el pago")
    @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            PagoDTO pago = pagoService.buscarPorId(
                    id, authentication.getName(), esAdministrador(authentication));
            return new ResponseEntity<>(pago, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Consultar historial de estados del pago")
    @ApiResponse(responseCode = "200", description = "Historial obtenido")
    @ApiResponse(responseCode = "403", description = "No autorizado para consultar el pago")
    @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    public ResponseEntity<List<PagoEstadoHistorialDTO>> listarHistorial(
            @PathVariable Integer id,
            Authentication authentication) {
        List<PagoEstadoHistorialDTO> historial = pagoService.listarHistorial(
                id, authentication.getName(), esAdministrador(authentication));
        if (historial.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(historial, HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Crear pago para un pedido propio")
    @ApiResponse(responseCode = "201", description = "Pago creado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "403", description = "El pedido no pertenece al cliente")
    @ApiResponse(responseCode = "404", description = "Pedido o metodo de pago no encontrado")
    @ApiResponse(responseCode = "409", description = "El pedido no admite otro pago")
    public ResponseEntity<?> crear(
            @Valid @RequestBody CrearPagoRequest request,
            Authentication authentication) {
        try {
            PagoDTO pago = pagoService.crearPago(request, authentication.getName());
            return new ResponseEntity<>(pago, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    "Error al crear el pago: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado del pago")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Estado o motivo invalido")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede cambiar estados")
    @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    @ApiResponse(responseCode = "409", description = "Transicion no permitida")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarEstadoPagoRequest request,
            Authentication authentication) {
        try {
            PagoDTO pago = pagoService.cambiarEstado(
                    id,
                    request,
                    authentication.getName(),
                    esAdministrador(authentication));
            return new ResponseEntity<>(pago, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    "Error al actualizar el estado: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping({"", "/{id}", "/{id}/**"})
    @Operation(summary = "Rechazar cualquier intento de eliminar un pago")
    @ApiResponse(responseCode = "409", description = "Los pagos no se pueden eliminar")
    public ResponseEntity<String> eliminar() {
        return new ResponseEntity<>(
                "Los pagos no se pueden eliminar; deben conservarse para trazabilidad",
                HttpStatus.CONFLICT);
    }

    private boolean esAdministrador(Authentication authentication) {
        return authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
    }
}
