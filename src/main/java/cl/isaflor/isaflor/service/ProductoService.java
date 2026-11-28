
package cl.isaflor.isaflor.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.isaflor.isaflor.dto.OfertaDTO;
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


    // CONSTRUCTOR

    public ProductoService(
            ProductoRepository productoRepository,
            ProductoValidaciones productoValidaciones,
            CategoriaRepository categoriaRepository) {

        this.productoRepository = productoRepository;
        this.productoValidaciones = productoValidaciones;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductos() {

        return productoRepository.findAll()
                .stream()
                .map(productoValidaciones::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoDTO buscarPorId(Integer id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontro el producto con ID: " + id));

        return productoValidaciones.convertirADTO(producto);
    }


    // =========================================
    // GUARDAR PRODUCTO
    // =========================================

    @Transactional
    public ProductoDTO guardarProducto(Producto producto) {

        // Validar datos obligatorios

        if (!productoValidaciones.validarNullVacio(producto)) {

            throw new RuntimeException(
                    "El codigo, nombre y descripcion son obligatorios");
        }


        // Eliminar espacios innecesarios

        producto.setCodigoProducto(
                producto.getCodigoProducto().trim());

        producto.setNombreProducto(
                producto.getNombreProducto().trim());

        producto.setDescripcion(
                producto.getDescripcion().trim());


        // Validar codigo duplicado

        if (productoRepository.existsByCodigoProductoIgnoreCase(
                producto.getCodigoProducto())) {

            throw new RuntimeException(
                    "Ya existe un producto con ese codigo");
        }


        // Validar categoria

        if (producto.getCategoria() == null ||
                producto.getCategoria().getIdCategoria() == null) {

            throw new RuntimeException(
                    "Debe seleccionar una categoria");
        }

        Categoria categoria = categoriaRepository.findById(
                producto.getCategoria().getIdCategoria())
                .orElseThrow(() -> new RuntimeException(
                        "La categoria no existe"));

        producto.setCategoria(categoria);


        // Garantizar que se cree un producto nuevo

        producto.setIdProducto(null);


        // Validar oferta

        productoValidaciones.validarOferta(producto);


        // Guardar producto

        Producto productoGuardado =
                productoRepository.save(producto);

        return productoValidaciones.convertirADTO(
                productoGuardado);
    }


    // =========================================
    // ACTUALIZAR PRODUCTO
    // =========================================

    @Transactional
    public ProductoDTO actualizarProducto(
            Integer id, Producto productoNuevo) {

        Producto productoExistente =
                productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontro el producto con ID: " + id));


        // Validar datos obligatorios

        if (!productoValidaciones.validarNullVacio(productoNuevo)) {

            throw new RuntimeException(
                    "El codigo, nombre y descripcion son obligatorios");
        }


        // Validar codigo duplicado

        String nuevoCodigo =
                productoNuevo.getCodigoProducto().trim();

        if (!productoExistente.getCodigoProducto()
                .equalsIgnoreCase(nuevoCodigo)

                && productoRepository
                .existsByCodigoProductoIgnoreCase(nuevoCodigo)) {

            throw new RuntimeException(
                    "Ya existe otro producto con ese codigo");
        }


        // Validar categoria

        if (productoNuevo.getCategoria() == null ||
                productoNuevo.getCategoria().getIdCategoria() == null) {

            throw new RuntimeException(
                    "Debe seleccionar una categoria");
        }

        Categoria categoria = categoriaRepository.findById(
                productoNuevo.getCategoria().getIdCategoria())
                .orElseThrow(() -> new RuntimeException(
                        "La categoria no existe"));


        // Actualizar datos generales

        productoExistente.setCodigoProducto(nuevoCodigo);

        productoExistente.setNombreProducto(
                productoNuevo.getNombreProducto().trim());

        productoExistente.setDescripcion(
                productoNuevo.getDescripcion().trim());

        productoExistente.setPrecioProducto(
                productoNuevo.getPrecioProducto());


        // Actualizar inventario

        productoExistente.setStockProducto(
                productoNuevo.getStockProducto());

        productoExistente.setStockCritico(
                productoNuevo.getStockCritico());


        // Actualizar pesos

        productoExistente.setPesoProducto(
                productoNuevo.getPesoProducto());

        productoExistente.setPesoVolumetrico(
                productoNuevo.getPesoVolumetrico());


        // Actualizar categoria

        productoExistente.setCategoria(categoria);


        // Conservar la oferta existente y comprobar
        // que siga siendo valida con el precio actualizado

        productoValidaciones.validarOferta(productoExistente);


        // Guardar cambios

        Producto actualizado =
                productoRepository.save(productoExistente);

        return productoValidaciones.convertirADTO(actualizado);
    }


    // =========================================
    // GESTIONAR OFERTA
    // =========================================

    @Transactional
    public ProductoDTO actualizarOferta(
            Integer id, OfertaDTO oferta) {

        // Buscar producto

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "No se encontro el producto con ID: " + id));


        // Validar estado de la oferta

        if (oferta == null || oferta.getEnOferta() == null) {

            throw new IllegalArgumentException(
                    "Debe indicar el estado de la oferta");
        }


        // Actualizar estado y precio de la oferta

        producto.setEnOferta(oferta.getEnOferta());

        producto.setPrecioOferta(oferta.getPrecioOferta());


        // Validar reglas de negocio

        productoValidaciones.validarOferta(producto);


        // Guardar cambios en Neon

        Producto actualizado =
                productoRepository.save(producto);

        return productoValidaciones.convertirADTO(actualizado);
    }


    // =========================================
    // ELIMINAR PRODUCTO
    // =========================================

    public String eliminarProducto(Integer id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontro el producto con ID: " + id));

        productoRepository.delete(producto);

        return "Producto eliminado correctamente";
    }
}
