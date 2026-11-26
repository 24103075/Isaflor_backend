package cl.isaflor.isaflor.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cl.isaflor.isaflor.dto.ProductoDTO;
import cl.isaflor.isaflor.model.Producto;
import cl.isaflor.isaflor.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description ="Operaciones para la gestion de productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // LISTAR PRODUCTOS
    @GetMapping
    @Operation(summary = "Listar productos",
            description = "Obtiene todos los productos registrados")
    @ApiResponse(responseCode = "200", description = "Productos obtenidos correctamente")
    @ApiResponse(responseCode = "204", description = "No existen productos registrados")
    public ResponseEntity<List<ProductoDTO>> listar() {

        List<ProductoDTO> productos = productoService.listarProductos();

        if (productos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(productos, HttpStatus.OK);
    }

    // BUSCAR PRODUCTO POR ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar producto por ID",description = "Obtiene un producto segun su identificador")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {

        try {
            ProductoDTO producto = productoService.buscarPorId(id);
            return new ResponseEntity<>(producto, HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // GUARDAR PRODUCTO
    @PostMapping
    @Operation(summary = "Crear producto",description = "Registra un nuevo producto")
    @ApiResponse(responseCode = "201", description = "Producto creado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "409", description = "Codigo duplicado")
    public ResponseEntity<?> guardar(@Valid @RequestBody Producto producto) {

        try {
            ProductoDTO productoGuardado = productoService.guardarProducto(producto);
            return new ResponseEntity<>(productoGuardado, HttpStatus.CREATED);

        } catch (RuntimeException e) {

            return new ResponseEntity<>("Error al guardar: " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    // ACTUALIZAR PRODUCTO
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto",description = "Actualiza los datos de un producto existente")
    @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @Valid @RequestBody Producto producto) {

        try {
            ProductoDTO actualizado = productoService.actualizarProducto(id, producto);
            return new ResponseEntity<>(actualizado, HttpStatus.OK);

        } catch (RuntimeException e) {

            return new ResponseEntity<>("Error al actualizar: " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    // ELIMINAR PRODUCTO
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto",description = "Elimina un producto segun su identificador")
    @ApiResponse(responseCode = "200", description = "Producto eliminado correctamente")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {

        try {
            String mensaje = productoService.eliminarProducto(id);
            return new ResponseEntity<>(mensaje, HttpStatus.OK);

        } catch (RuntimeException e) {

            return new ResponseEntity<>("Error al eliminar: " + e.getMessage(),HttpStatus.NOT_FOUND);
        }
    }
    
}
