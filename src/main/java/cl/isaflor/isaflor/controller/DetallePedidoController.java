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

import cl.isaflor.isaflor.dto.ActualizarDetallePedidoRequest;
import cl.isaflor.isaflor.dto.CrearDetallePedidoRequest;
import cl.isaflor.isaflor.dto.DetallePedidoDTO;
import cl.isaflor.isaflor.service.DetallePedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/detalles-pedido")
@Tag(name = "Detalles de pedido", description = "Operaciones para gestionar los productos de un pedido")
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;

    public DetallePedidoController(DetallePedidoService detallePedidoService) {
        this.detallePedidoService = detallePedidoService;
    }

    @GetMapping
    @Operation(summary = "Listar detalles de pedido",
            description = "El cliente obtiene los detalles de sus pedidos; el administrador obtiene todos")
    @ApiResponse(responseCode = "200", description = "Detalles obtenidos correctamente")
    @ApiResponse(responseCode = "204", description = "No existen detalles")
    public ResponseEntity<List<DetallePedidoDTO>> listar(Authentication authentication) {
        List<DetallePedidoDTO> detalles = detallePedidoService.listarDetalles(
                authentication.getName(), esAdministrador(authentication));
        if (detalles.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(detalles, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar detalle de pedido por ID")
    @ApiResponse(responseCode = "200", description = "Detalle encontrado")
    @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    @ApiResponse(responseCode = "403", description = "No autorizado para acceder al pedido")
    public ResponseEntity<DetallePedidoDTO> buscarPorId(
            @PathVariable Integer id,
            Authentication authentication) {
        return ResponseEntity.ok(detallePedidoService.buscarPorId(
                id, authentication.getName(), esAdministrador(authentication)));
    }

    @PostMapping
    @Operation(summary = "Agregar producto a un pedido",
            description = "El precio unitario se obtiene del producto y el subtotal se calcula automáticamente")
    @ApiResponse(responseCode = "201", description = "Detalle creado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "403", description = "No autorizado para modificar el pedido")
    @ApiResponse(responseCode = "404", description = "Pedido o producto no encontrado")
    @ApiResponse(responseCode = "409", description = "El pedido no permite modificar detalles")
    public ResponseEntity<DetallePedidoDTO> crear(
            @Valid @RequestBody CrearDetallePedidoRequest request,
            Authentication authentication) {
        DetallePedidoDTO creado = detallePedidoService.crearDetalle(
                request, authentication.getName(), esAdministrador(authentication));
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cantidad de un detalle de pedido",
            description = "Recalcula el subtotal y el total del pedido usando el precio histórico")
    @ApiResponse(responseCode = "200", description = "Detalle actualizado correctamente")
    @ApiResponse(responseCode = "400", description = "Cantidad invalida")
    @ApiResponse(responseCode = "403", description = "No autorizado para modificar el pedido")
    @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    @ApiResponse(responseCode = "409", description = "El pedido no permite modificar detalles")
    public ResponseEntity<DetallePedidoDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarDetallePedidoRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(detallePedidoService.actualizarCantidad(
                id, request, authentication.getName(), esAdministrador(authentication)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un detalle de pedido")
    @ApiResponse(responseCode = "200", description = "Detalle eliminado correctamente")
    @ApiResponse(responseCode = "403", description = "No autorizado para modificar el pedido")
    @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    @ApiResponse(responseCode = "409", description = "El pedido no permite modificar detalles")
    public ResponseEntity<String> eliminar(
            @PathVariable Integer id,
            Authentication authentication) {
        return ResponseEntity.ok(detallePedidoService.eliminarDetalle(
                id, authentication.getName(), esAdministrador(authentication)));
    }

    private boolean esAdministrador(Authentication authentication) {
        return authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
    }
}
