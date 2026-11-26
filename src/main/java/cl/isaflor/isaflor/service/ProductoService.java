package cl.isaflor.isaflor.service;

import java.util.List;
import org.springframework.stereotype.Service;
import cl.isaflor.isaflor.dto.ProductoDTO;
import cl.isaflor.isaflor.model.Categoria;
import cl.isaflor.isaflor.model.Producto;
import cl.isaflor.isaflor.repository.CategoriaRepository;
import cl.isaflor.isaflor.repository.ProductoRepository;

@Service 
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoValidaciones productoValidaciones;
    private final CategoriaRepository categoriaRepository;

    // constructor 
    public ProductoService(
            ProductoRepository productoRepository, 
            ProductoValidaciones productoValidaciones,      
            CategoriaRepository categoriaRepository){
                            
            this.productoRepository = productoRepository;
            this.productoValidaciones = productoValidaciones;
            this.categoriaRepository = categoriaRepository;
            }
    
    //LISTAR PRODUCTO
    public List<ProductoDTO> listarProductos() {
        return productoRepository.findAll().stream().map(productoValidaciones::convertirADTO).toList();
    }

    //BUSCAR PRODUCTO POR ID
     public ProductoDTO buscarPorId(Integer id) {

        try {
            Producto producto = productoRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("No se encontro el producto con ID: " + id));

            return productoValidaciones.convertirADTO(producto);

        } catch (RuntimeException e) {
            throw e;
        }
    }

    //GUARDAR PRODUCTO
    public ProductoDTO guardarProducto(Producto producto) {

        try {
            if (!productoValidaciones.validarNullVacio(producto)) {
                throw new RuntimeException( "El codigo,nombre y descripcion son obligatorios");
            }

            producto.setCodigoProducto(producto.getCodigoProducto().trim());
            producto.setNombreProducto(producto.getNombreProducto().trim());
            producto.setDescripcion(producto.getDescripcion().trim());

            if (productoRepository.existsByCodigoProductoIgnoreCase(producto.getCodigoProducto())) {
                throw new RuntimeException("Ya existe un producto con ese codigo");
            }

            if (producto.getCategoria() == null || producto.getCategoria().getIdCategoria() == null) {
                throw new RuntimeException("Debe seleccionar una categoria");
            }

            Categoria categoria = categoriaRepository.findById(producto.getCategoria().getIdCategoria())
                                    .orElseThrow(() -> new RuntimeException("La categoria no existe"));

            producto.setCategoria(categoria);

            Producto productoGuardado = productoRepository.save(producto);

            return productoValidaciones.convertirADTO(productoGuardado);

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el producto: " + e.getMessage(), e);
        }
    }
    
    //ACTUALIZAR PRODUCTO
    public ProductoDTO actualizarProducto(Integer id, Producto productoNuevo) {

        try {
            Producto productoExistente = productoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("No se encontro el producto con ID: " + id));

            if (!productoValidaciones.validarNullVacio(productoNuevo)) {
                throw new RuntimeException("El cOdigo,nombre y descripciOn son obligatorios");
            }

            String nuevoCodigo = productoNuevo.getCodigoProducto().trim();

            if (!productoExistente.getCodigoProducto().equalsIgnoreCase(nuevoCodigo)
                    && productoRepository.existsByCodigoProductoIgnoreCase(nuevoCodigo)) {

                throw new RuntimeException("Ya existe otro producto con ese codigo");
            }

            if (productoNuevo.getCategoria() == null || productoNuevo.getCategoria().getIdCategoria() == null) {

                throw new RuntimeException("Debe seleccionar una categoria");
            }

            Categoria categoria = categoriaRepository.findById(
                    productoNuevo.getCategoria().getIdCategoria())
                    .orElseThrow(() -> new RuntimeException( "La categoria no existe"));

            productoExistente.setCodigoProducto(nuevoCodigo);
            productoExistente.setNombreProducto(
            productoNuevo.getNombreProducto().trim());
            productoExistente.setDescripcion(
            productoNuevo.getDescripcion().trim());
            productoExistente.setPrecioProducto(productoNuevo.getPrecioProducto());
            productoExistente.setStockProducto(productoNuevo.getStockProducto());
            productoExistente.setStockCritico(productoNuevo.getStockCritico());
            productoExistente.setPesoProducto(productoNuevo.getPesoProducto());
            productoExistente.setPesoVolumetrico(productoNuevo.getPesoVolumetrico());
            productoExistente.setCategoria(categoria);

            Producto actualizado = productoRepository.save(productoExistente);

            return productoValidaciones.convertirADTO(actualizado);

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el producto: " + e.getMessage(), e);
        }
    }

    public String eliminarProducto(Integer id) {

        try {
            Producto producto = productoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("No se encontro el producto con ID: " + id));

            productoRepository.delete(producto);

            return "Producto eliminado correctamente";

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el producto: " + e.getMessage(), e);
        }
    }
}