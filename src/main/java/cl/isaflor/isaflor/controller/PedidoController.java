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

import cl.isaflor.isaflor.dto.ActualizarEstadoPedidoRequest;
import cl.isaflor.isaflor.dto.CrearPedidoRequest;
import cl.isaflor.isaflor.dto.PedidoDTO;
import cl.isaflor.isaflor.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Operaciones para gestionar pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    @Operation(summary = "Listar pedidos")
    @ApiResponse(responseCode = "200", description = "Pedidos obtenidos")
    @ApiResponse(responseCode = "204", description = "No existen pedidos")
    public ResponseEntity<List<PedidoDTO>> listar(Authentication authentication) {
        List<PedidoDTO> pedidos = pedidoService.listarPedidos(
                authentication.getName(), esAdministrador(authentication));

        if (pedidos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(pedidos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID")
    @ApiResponse(responseCode = "200", description = "Pedido encontrado")
    @ApiResponse(responseCode = "403", description = "No autorizado para acceder al pedido")
    @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            PedidoDTO pedido = pedidoService.buscarPorId(
                    id, authentication.getName(), esAdministrador(authentication));
            return new ResponseEntity<>(pedido, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    @PostMapping
    @Operation(summary = "Crear pedido para el cliente autenticado")
    @ApiResponse(responseCode = "201", description = "Pedido creado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "404", description = "Metodo de envio o tipo de documento no encontrado")
    public ResponseEntity<?> crear(
            @Valid @RequestBody CrearPedidoRequest request,
            Authentication authentication) {
        try {
            PedidoDTO pedido = pedidoService.crearPedido(
                    request, authentication.getName());
            return new ResponseEntity<>(pedido, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    "Error al crear el pedido: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de un pedido")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Estado invalido")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede cambiar el estado")
    @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    @ApiResponse(responseCode = "409", description = "Transicion de estado no permitida")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarEstadoPedidoRequest request,
            Authentication authentication) {
        try {
            PedidoDTO pedido = pedidoService.cambiarEstado(
                    id, request, esAdministrador(authentication));
            return new ResponseEntity<>(pedido, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    "Error al actualizar el estado: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pedido sin detalles ni pagos")
    @ApiResponse(responseCode = "200", description = "Pedido eliminado")
    @ApiResponse(responseCode = "403", description = "No autorizado para eliminar el pedido")
    @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    @ApiResponse(responseCode = "409", description = "Pedido con detalles o pago asociado")
    public ResponseEntity<?> eliminar(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            String mensaje = pedidoService.eliminarPedido(
                    id, authentication.getName(), esAdministrador(authentication));
            return new ResponseEntity<>(mensaje, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    private boolean esAdministrador(Authentication authentication) {
        return authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
    }
}
